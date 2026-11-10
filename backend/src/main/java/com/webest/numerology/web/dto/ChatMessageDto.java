package com.webest.numerology.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {

    /** 固定小寫：{@code user} | {@code assistant}，見 {@link com.webest.numerology.util.ChatMessageRoles} */
    private String role;
    private String content;
}
