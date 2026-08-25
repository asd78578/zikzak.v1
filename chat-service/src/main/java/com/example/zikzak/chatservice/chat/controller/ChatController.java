package com.example.zikzak.chatservice.chat.controller;

import com.example.zikzak.chatservice.chat.ChatService;
import com.example.zikzak.chatservice.chat.dto.ChatResponse;
import com.example.zikzak.chatservice.chat.dto.CreateDirectChatRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> createDirectChat(
            Authentication authentication,
            @Valid @RequestBody CreateDirectChatRequest request
    ) {
        Long currentAccountId =
                (Long) authentication.getPrincipal();

        ChatResponse response = chatService.createDirectChat(
                currentAccountId,
                request.participantAccountId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public List<ChatResponse> findMyChats(
            Authentication authentication
    ) {
        Long currentAccountId =
                (Long) authentication.getPrincipal();

        return chatService.findChats(currentAccountId);
    }
}
