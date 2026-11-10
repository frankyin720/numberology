package com.webest.numerology.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "請輸入目前密碼")
    private String currentPassword;

    @NotBlank(message = "請輸入新密碼")
    @Size(min = 8, message = "密碼至少 8 個字元")
    private String newPassword;
}
