package com.webest.numerology.util;

import lombok.experimental.UtilityClass;
import org.springframework.ai.chat.model.ChatResponse;

@UtilityClass
public class ChatUtil {
    /**
     * {@link org.springframework.ai.chat.client.ChatClient.CallResponseSpec} 只能调用一次
     * {@code content()} 或 {@code chatResponse()}，不可两次都用。
     */
    public String getResponseText(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getResult() == null) {
            return null;
        }
        var output = chatResponse.getResult().getOutput();
        return output != null ? output.getText() : null;
    }
}
