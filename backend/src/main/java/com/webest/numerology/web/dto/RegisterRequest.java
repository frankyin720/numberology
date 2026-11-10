package com.webest.numerology.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "請輸入信箱")
    @Email(message = "信箱格式不正確")
    private String email;

    @NotBlank(message = "請輸入密碼")
    @Size(min = 8, message = "密碼至少 8 個字元")
    private String password;
}
