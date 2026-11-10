package com.webest.numerology.tool;

import org.apache.commons.lang3.StringUtils;
import com.webest.numerology.security.SecurityContextUser;
import com.webest.numerology.service.LlmOracleFallback;
import com.webest.numerology.service.UserProfileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class NumerologyTools {

    private static final Logger log = LoggerFactory.getLogger(NumerologyTools.class);

    private final RestClient http = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LlmOracleFallback llmFallback;
    private final UserProfileService userProfileService;

    @Value("${numerology.yuanfenju.enabled:false}")
    private boolean yuanfenjuEnabled;

    @Value("${numerology.yuanfenju.base-url:https://api.yuanfenju.com/index.php/v1}")
    private String yuanfenjuBaseUrl;

    @Value("${numerology.yuanfenju.api-key:}")
    private String yuanfenjuApiKey;

    @Value("${numerology.yuanfenju.bazi-path:/Bazi/cesuan}")
    private String yuanfenjuBaziPath;

    @Value("${numerology.yuanfenju.zhanbu-path:/Zhanbu/meiri}")
    private String yuanfenjuZhanbuPath;

    @Value("${numerology.yuanfenju.zhougong-path:/Gongju/zhougong}")
    private String yuanfenjuZhougongPath;

    @Value("${numerology.serp.enabled:false}")
    private boolean serpEnabled;

    @Value("${numerology.serp.base-url:https://serpapi.com/search.json}")
    private String serpBaseUrl;

    @Value("${numerology.serp.api-key:}")
    private String serpApiKey;

    public NumerologyTools(LlmOracleFallback llmFallback, UserProfileService userProfileService) {
        this.llmFallback = llmFallback;
        this.userProfileService = userProfileService;
    }

    @Tool(description = """
            取得當前登入用戶在資料庫中的全部命理資料：姓名、性別（含男命/女命與大運順逆）、出生日期與曆法、\
            出生時辰、出生地、現居地、時區，以及 profile_complete 是否齊全。論命前必調用；若性別已為男/女則不得再問用戶性別。""")
    public String getUserProfile() {
        Long userId = SecurityContextUser.requireUserId();
        String payload = userProfileService.formatForLlm(userId);
        if (log.isDebugEnabled()) {
            log.debug("getUserProfile userId={} payload:\n{}", userId, payload);
        }
        return payload;
    }

    /** 路径若以 http 开头则视为完整 URL，否则拼在 yuanfenju base-url 后。 */
    private String resolveUrl(String baseUrl, String pathOrAbsolute) {
        if (pathOrAbsolute != null
                && (pathOrAbsolute.startsWith("http://") || pathOrAbsolute.startsWith("https://"))) {
            return pathOrAbsolute;
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = pathOrAbsolute.startsWith("/") ? pathOrAbsolute : "/" + pathOrAbsolute;
        return base + path;
    }

    private boolean useYuanfenjuApi() {
        return yuanfenjuEnabled && StringUtils.isNotBlank(yuanfenjuApiKey);
    }

    private boolean useSerpApi() {
        return serpEnabled && StringUtils.isNotBlank(serpApiKey);
    }

    @Tool(description = "當你不知道答案、需要查實時資訊、新聞、定義、未知知識時使用；無需先調用 getUserProfile。")
    public String serpSearch(@ToolParam(description = "搜索關鍵詞") String query) {
        if (!useSerpApi()) {
            return llmFallback.answer("用戶需要查實時或常識性資訊，請簡要回答", query);
        }
        try {
            String body = http.get()
                    .uri(serpBaseUrl + "?q={q}&api_key={key}", query, serpApiKey)
                    .retrieve()
                    .body(String.class);
            return body != null ? body : llmFallback.answer("搜索無結果，請根據常識回答", query);
        } catch (Exception e) {
            log.warn("SerpAPI failed, use LLM: {}", e.getMessage());
            return llmFallback.answer("聯網搜索失敗，請根據常識簡答", query);
        }
    }

    @Tool(description = "專門查「辦公室風水」「風水常識」相關內容。須先調用 getUserProfile，並結合用戶現居地與時區作答。")
    public String getInfoFromLocalDb(@ToolParam(description = "用戶問題") String query) {
        return llmFallback.answer("請以風水命理常識回答辦公室或居家風水相關問題", query);
    }

    @Tool(description = "用戶要算八字、排八字、論五行、論命、看命盤時使用。須先調用 getUserProfile，確認生辰資料齊全後再排盤。")
    public String baZiCeSuan(@ToolParam(description = "用戶原始描述") String query) {
        String profileHint = userProfileService.formatForLlm(SecurityContextUser.requireUserId());
        if (!useYuanfenjuApi()) {
            return llmFallback.answer(
                    "用戶要排八字或論五行。用戶存檔資料：\n" + profileHint + "\n用戶問題：" + query,
                    query);
        }
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("api_key", yuanfenjuApiKey);
            form.add("name", extractNameFromProfile(profileHint));
            form.add("sex", userProfileService.baziSexCode(SecurityContextUser.requireUserId()));
            form.add("type", "1");
            String text = http.post()
                    .uri(resolveUrl(yuanfenjuBaseUrl, yuanfenjuBaziPath))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(String.class);
            if (text != null) {
                JsonNode root = objectMapper.readTree(text);
                JsonNode bazi = root.path("data").path("bazi_info").path("bazi");
                if (!bazi.isMissingNode()) {
                    return "八字為：" + bazi.asText();
                }
            }
        } catch (Exception e) {
            log.warn("Yuanfenju bazi failed, use LLM: {}", e.getMessage());
        }
        return "【排盤備註】已依用戶存檔性別與生辰推演（詳見存檔）。\n"
                + llmFallback.answer(
                        "緣分居排盤接口不可用。用戶存檔：\n" + profileHint,
                        query);
    }

    private static String extractNameFromProfile(String profileHint) {
        for (String line : profileHint.split("\n")) {
            if (line.contains("姓名：")) {
                return line.substring(line.indexOf("姓名：") + "姓名：".length()).trim();
            }
        }
        return "待填";
    }

    @Tool(description = "用戶想要占卜、求籤、搖卦、問運勢時使用。須先調用 getUserProfile，以便結合命主資料解讀。")
    public String yaoYiGua() {
        if (!useYuanfenjuApi()) {
            return llmFallback.answer("用戶求今日運勢占卜，請給一卦象風格的簡短指引", "今日問卦");
        }
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("api_key", yuanfenjuApiKey);
            String text = http.post()
                    .uri(resolveUrl(yuanfenjuBaseUrl, yuanfenjuZhanbuPath))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(String.class);
            if (text != null) {
                JsonNode root = objectMapper.readTree(text);
                JsonNode desc = root.path("data").path("description");
                if (!desc.isMissingNode()) {
                    return desc.asText();
                }
            }
        } catch (Exception e) {
            log.warn("Yuanfenju yaoYiGua failed, use LLM: {}", e.getMessage());
        }
        return llmFallback.answer("用戶求今日運勢占卜，請給一卦象風格的簡短指引", "今日問卦");
    }

    @Tool(description = "用戶做夢、想解夢、詢問夢境含義時使用。須先調用 getUserProfile，以便結合命主資料解讀。")
    public String jieMeng(@ToolParam(description = "夢境描述") String query) {
        if (!useYuanfenjuApi()) {
            return llmFallback.answer("請依周公解夢傳統象意解讀以下夢境", query);
        }
        List<String> candidates = List.of(query.replaceAll("[，,。.！!？?、\\s]+", ""), query);
        for (String kw : candidates) {
            if (StringUtils.isBlank(kw)) {
                continue;
            }
            try {
                MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
                form.add("api_key", yuanfenjuApiKey);
                form.add("title_zhougong", kw);
                String text = http.post()
                        .uri(resolveUrl(yuanfenjuBaseUrl, yuanfenjuZhougongPath))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form)
                        .retrieve()
                        .body(String.class);
                if (text == null) {
                    continue;
                }
                JsonNode root = objectMapper.readTree(text);
                if (root.path("errcode").asInt(-1) != 0) {
                    continue;
                }
                JsonNode data = root.path("data");
                if (data.isArray() && !data.isEmpty()) {
                    String msg = data.get(0).path("message").asText(null);
                    if (msg != null) {
                        return msg;
                    }
                }
            } catch (Exception e) {
                log.warn("Yuanfenju jieMeng kw={} failed: {}", kw, e.getMessage());
            }
        }
        return llmFallback.answer("周公解夢庫未命中，請依傳統象意解讀夢境", query);
    }
}
