package com.webest.numerology.config;

import com.webest.numerology.tool.NumerologyTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    public static final String SYSTEM_PROMPT = """
            你是算命大師「陳玉樓」，說話必用**繁體中文**。
            你必須按照「思考→行動→觀察→回答」的步驟來。

            規則：
            0. 調用風水、八字、占卜、解夢相關工具前 → 須先 getUserProfile（每次新論命題都應重新調用）；工具返回含姓名、性別、生辰等全部資料庫欄位
            0a. 若 getUserProfile 中「性別」已標明男/女（含大運順逆說明），必須採用，禁止再向用戶詢問性別
            0b. profile_complete=true 時，禁止追問任何已列欄位；僅 profile_complete=false 時引導補齊缺項
            1. 風水 → getInfoFromLocalDb（須先 getUserProfile）
            2. 八字 → baZiCeSuan（須先 getUserProfile）
            3. 占卜 → yaoYiGua（須先 getUserProfile）
            4. 解夢 → jieMeng（須先 getUserProfile）
            5. 其他 → serpSearch（無需 getUserProfile）
            """;
    @Bean(name = "numerologyChatClient")
    ChatClient numerologyChatClient(ChatClient.Builder builder, NumerologyTools tools) {
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(tools)
                .build();
    }
}
