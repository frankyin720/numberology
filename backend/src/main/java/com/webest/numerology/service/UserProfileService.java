package com.webest.numerology.service;

import com.webest.numerology.domain.BirthTimeSlot;
import com.webest.numerology.repository.UserProfileRepository;
import com.webest.numerology.repository.UserRepository;
import com.webest.numerology.repository.entity.UserEntity;
import com.webest.numerology.repository.entity.UserProfileEntity;
import com.webest.numerology.web.dto.ProfileStatusResponse;
import com.webest.numerology.web.dto.UserProfileDto;
import com.webest.numerology.web.exception.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;

    public UserProfileService(UserProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ProfileStatusResponse getStatus(Long userId) {
        UserProfileEntity profile = findOrEmpty(userId);
        return ProfileStatusResponse.builder()
                .complete(isComplete(profile))
                .profile(toDto(profile))
                .build();
    }

    @Transactional(readOnly = true)
    public String formatForLlm(Long userId) {
        UserProfileEntity profile = findOrEmpty(userId);
        return buildProfileToolPayload(profile);
    }

    /**
     * 供 getUserProfile 等工具使用：一律列出資料庫中已有欄位，避免僅回「未齊全」而漏掉性別等已存資料。
     */
    public static String buildProfileToolPayload(UserProfileEntity profile) {
        if (profile == null || profile.getId() == null) {
            return """
                    用戶尚未建立個人資料。請引導至「個人資料」填寫：姓名、性別、出生年月日、曆法、出生時辰、出生地、現居地、時區。
                    """;
        }
        boolean complete = isComplete(profile);
        String calendar = profile.getCalendarType() == UserProfileEntity.CalendarType.LUNAR ? "農曆"
                : profile.getCalendarType() == UserProfileEntity.CalendarType.SOLAR ? "陽曆" : "（未填）";
        String header = complete
                ? "當前用戶命理基本資料（資料庫已齊全，以下每一項均已存檔，禁止在對話中再次詢問）"
                : "當前用戶命理基本資料（資料庫部分欄位已存，缺項請引導至個人資料補全）";
        return """
                %s：
                - 姓名：%s
                - 性別：%s
                - 出生日期：%s（%s）
                - 出生時辰：%s
                - 出生地：%s
                - 現居地：%s
                - 時區：%s
                profile_complete=%s
                """.formatted(
                header,
                valueOrMissing(profile.getFullName()),
                genderForLlm(profile.getGender()),
                valueOrMissing(profile.getBirthDate()),
                calendar,
                birthTimeForLlm(profile.getBirthTime()),
                valueOrMissing(profile.getBirthPlace()),
                valueOrMissing(profile.getCurrentResidence()),
                valueOrMissing(profile.getTimezone()),
                complete);
    }

    @Transactional
    public UserProfileDto save(Long userId, UserProfileDto dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "使用者不存在"));
        validate(dto);
        UserProfileEntity profile = profileRepository.findByUser_Id(userId)
                .orElseGet(() -> UserProfileEntity.builder().user(user).build());
        profile.setFullName(dto.getFullName().trim());
        profile.setGender(dto.getGender());
        profile.setBirthDate(dto.getBirthDate().trim());
        profile.setCalendarType(dto.getCalendarType());
        profile.setBirthTime(normalizeBirthTime(dto.getBirthTime()));
        profile.setBirthPlace(dto.getBirthPlace().trim());
        profile.setCurrentResidence(dto.getCurrentResidence().trim());
        profile.setTimezone(dto.getTimezone().trim());
        profile.setUpdatedAt(Instant.now());
        profileRepository.save(profile);
        return toDto(profile);
    }

    private static void validate(UserProfileDto dto) {
        if (dto == null
                || StringUtils.isAnyBlank(
                        dto.getFullName(),
                        dto.getBirthDate(),
                        dto.getBirthTime(),
                        dto.getBirthPlace(),
                        dto.getCurrentResidence(),
                        dto.getTimezone())
                || dto.getCalendarType() == null
                || dto.getGender() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "請填寫完整的命理基本資料");
        }
        if (!BirthTimeSlot.isValidCode(dto.getBirthTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "請選擇有效的出生時辰");
        }
    }

    private static String normalizeBirthTime(String birthTime) {
        return BirthTimeSlot.fromCode(birthTime)
                .map(BirthTimeSlot::getCode)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "請選擇有效的出生時辰"));
    }

    public static boolean isComplete(UserProfileEntity profile) {
        if (profile == null || profile.getId() == null) {
            return false;
        }
        return StringUtils.isNoneBlank(
                        profile.getFullName(),
                        profile.getBirthDate(),
                        profile.getBirthTime(),
                        profile.getBirthPlace(),
                        profile.getCurrentResidence(),
                        profile.getTimezone())
                && profile.getCalendarType() != null
                && profile.getGender() != null
                && BirthTimeSlot.isValidCode(profile.getBirthTime());
    }

    /** 緣分居八字接口：男 0，女 1 */
    @Transactional(readOnly = true)
    public String baziSexCode(Long userId) {
        UserProfileEntity profile = findOrEmpty(userId);
        if (profile.getGender() == UserProfileEntity.Gender.FEMALE) {
            return "1";
        }
        return "0";
    }

    private static String valueOrMissing(String value) {
        return StringUtils.isNotBlank(value) ? value.trim() : "（未填）";
    }

    private static String birthTimeForLlm(String code) {
        if (StringUtils.isBlank(code)) {
            return "（未填）";
        }
        return BirthTimeSlot.isValidCode(code) ? BirthTimeSlot.formatForDisplay(code) : code.trim() + "（請在個人資料改為標準時辰）";
    }

    /** 性別含大運排法說明，避免模型重複追問 */
    private static String genderForLlm(UserProfileEntity.Gender gender) {
        if (gender == UserProfileEntity.Gender.FEMALE) {
            return "女（女命，大運逆排；已存檔，勿再詢問性別）";
        }
        if (gender == UserProfileEntity.Gender.MALE) {
            return "男（男命，大運順排；已存檔，勿再詢問性別）";
        }
        return "（未填，須請用戶在個人資料中選擇男或女）";
    }

    private UserProfileEntity findOrEmpty(Long userId) {
        return profileRepository.findByUser_Id(userId).orElseGet(() -> UserProfileEntity.builder().build());
    }

    private static UserProfileDto toDto(UserProfileEntity profile) {
        if (profile.getUser() == null && profile.getId() == null) {
            return UserProfileDto.builder().build();
        }
        return UserProfileDto.builder()
                .fullName(profile.getFullName())
                .gender(profile.getGender())
                .birthDate(profile.getBirthDate())
                .calendarType(profile.getCalendarType())
                .birthTime(profile.getBirthTime())
                .birthPlace(profile.getBirthPlace())
                .currentResidence(profile.getCurrentResidence())
                .timezone(profile.getTimezone())
                .build();
    }
}
