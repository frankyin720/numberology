package com.webest.numerology.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** 十二時辰標準代碼（與前端一致） */
public enum BirthTimeSlot {
    ZI("ZI", "子時", "23:00–01:00"),
    CHOU("CHOU", "丑時", "01:00–03:00"),
    YIN("YIN", "寅時", "03:00–05:00"),
    MAO("MAO", "卯時", "05:00–07:00"),
    CHEN("CHEN", "辰時", "07:00–09:00"),
    SI("SI", "巳時", "09:00–11:00"),
    WU("WU", "午時", "11:00–13:00"),
    WEI("WEI", "未時", "13:00–15:00"),
    SHEN("SHEN", "申時", "15:00–17:00"),
    YOU("YOU", "酉時", "17:00–19:00"),
    XU("XU", "戌時", "19:00–21:00"),
    HAI("HAI", "亥時", "21:00–23:00"),
    UNKNOWN("UNKNOWN", "不詳", "");

    private static final Map<String, BirthTimeSlot> BY_CODE =
            Arrays.stream(values()).collect(Collectors.toMap(BirthTimeSlot::getCode, s -> s));

    private final String code;
    private final String label;
    private final String range;

    BirthTimeSlot(String code, String label, String range) {
        this.code = code;
        this.label = label;
        this.range = range;
    }

    public String getCode() {
        return code;
    }

    public String display() {
        if (StringUtils.isBlank(range)) {
            return label;
        }
        return label + "（" + range + "）";
    }

    public static Optional<BirthTimeSlot> fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_CODE.get(code.trim().toUpperCase(Locale.ROOT)));
    }

    public static boolean isValidCode(String code) {
        return fromCode(code).isPresent();
    }

    public static String formatForDisplay(String stored) {
        if (StringUtils.isBlank(stored)) {
            return "";
        }
        return fromCode(stored).map(BirthTimeSlot::display).orElse(stored.trim());
    }
}
