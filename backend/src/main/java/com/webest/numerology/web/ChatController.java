package com.webest.numerology.web;

import com.webest.numerology.security.UserPrincipal;
import com.webest.numerology.service.NumerologyChatService;
import com.webest.numerology.web.dto.ChatRequest;
import com.webest.numerology.web.dto.ChatResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final NumerologyChatService chatService;

    public ChatController(NumerologyChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ChatRequest request) {
        var result = chatService.chat(principal.getUserId(), request);
        return new ChatResponse(result.getConversationId(), result.getOutput());
    }
}
