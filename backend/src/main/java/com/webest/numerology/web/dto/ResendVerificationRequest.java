package com.webest.numerology.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendVerificationRequest {

    @NotBlank(message = "請輸入信箱")
    @Email(message = "信箱格式不正確")
    private String email;
}
