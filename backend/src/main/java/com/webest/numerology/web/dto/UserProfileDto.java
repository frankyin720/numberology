package com.webest.numerology.web.dto;

import com.webest.numerology.repository.entity.UserProfileEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    @NotBlank
    private String fullName;

    @NotNull
    private UserProfileEntity.Gender gender;

    @NotBlank
    private String birthDate;

    @NotNull
    private UserProfileEntity.CalendarType calendarType;

    @NotBlank
    private String birthTime;

    @NotBlank
    private String birthPlace;

    @NotBlank
    private String currentResidence;

    @NotBlank
    private String timezone;
}
