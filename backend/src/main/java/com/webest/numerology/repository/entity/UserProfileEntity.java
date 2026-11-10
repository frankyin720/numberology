package com.webest.numerology.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    /** 姓名或化名 */
    @Column(name = "full_name", length = 64)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 8)
    private Gender gender;

    /** 如 1990-05-01 或農曆描述 */
    @Column(name = "birth_date", length = 64)
    private String birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_type", length = 16)
    private CalendarType calendarType;

    /** 如 14:30 或「不詳」 */
    @Column(name = "birth_time", length = 32)
    private String birthTime;

    @Column(name = "birth_place", length = 120)
    private String birthPlace;

    /** 現居地點（省/市或國家城市） */
    @Column(name = "current_residence", length = 120)
    private String currentResidence;

    /** IANA 時區，如 Asia/Shanghai */
    @Column(name = "timezone", length = 64)
    private String timezone;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum CalendarType {
        SOLAR,
        LUNAR
    }

    public enum Gender {
        MALE,
        FEMALE
    }
}
