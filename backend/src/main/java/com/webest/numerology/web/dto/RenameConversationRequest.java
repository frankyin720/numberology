package com.webest.numerology.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RenameConversationRequest {

    @NotBlank(message = "請輸入會話名稱")
    @Size(max = 120, message = "名稱最多 120 字")
    private String title;
}
