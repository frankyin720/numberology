package com.webest.numerology.web;

import com.webest.numerology.security.UserPrincipal;
import com.webest.numerology.service.ConversationService;
import com.webest.numerology.service.NumerologyChatService;
import com.webest.numerology.web.dto.ConversationDto;
import com.webest.numerology.web.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.webest.numerology.web.dto.RenameConversationRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final NumerologyChatService chatService;

    public ConversationController(ConversationService conversationService, NumerologyChatService chatService) {
        this.conversationService = conversationService;
        this.chatService = chatService;
    }

    @GetMapping
    public List<ConversationDto> list(@AuthenticationPrincipal UserPrincipal principal) {
        return conversationService.listForUser(principal.getUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationDto create(@AuthenticationPrincipal UserPrincipal principal) {
        return conversationService.create(principal.getUserId());
    }

    @GetMapping("/{conversationId}/messages")
    public MessageResponse messages(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable String conversationId) {
        conversationService.requireOwned(principal.getUserId(), conversationId);
        return chatService.getMessages(conversationId);
    }

    @PatchMapping("/{conversationId}")
    public ConversationDto rename(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String conversationId,
            @Valid @RequestBody RenameConversationRequest request) {
        return conversationService.rename(principal.getUserId(), conversationId, request.getTitle());
    }

    @DeleteMapping("/{conversationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String conversationId) {
        conversationService.delete(principal.getUserId(), conversationId);
    }
}
