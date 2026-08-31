package com.example.zikzak.messageservice.message.controller;

import com.example.zikzak.messageservice.message.MessageService;
import com.example.zikzak.messageservice.message.dto.EditMessageRequest;
import com.example.zikzak.messageservice.message.dto.MessageResponse;
import com.example.zikzak.messageservice.message.dto.SendMessageRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chats/{chatId}/messages")
@Validated
public class MessageController {

    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(
            @PathVariable
            @Positive
            Long chatId,

            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,

            @Valid
            @RequestBody
            SendMessageRequest request,

            Authentication authentication
    ) {
        Long senderAccountId = (Long) authentication.getPrincipal();

        return service.send(
                chatId,
                senderAccountId,
                authorizationHeader,
                request
        );
    }

    @GetMapping
    public Page<MessageResponse> history(
            @PathVariable
            @Positive
            Long chatId,

            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "50")
            @Min(1)
            @Max(100)
            int size
    ) {
        return service.findHistory(
                chatId,
                authorizationHeader,
                page,
                size
        );
    }

    @PutMapping("/{messageId}")
    public MessageResponse edit(
            @PathVariable
            @Positive
            Long chatId,

            @PathVariable
            @Positive
            Long messageId,

            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,

            @Valid
            @RequestBody
            EditMessageRequest request,

            Authentication authentication
    ) {
        Long currentAccountId = (Long) authentication.getPrincipal();

        return service.edit(
                chatId,
                messageId,
                currentAccountId,
                authorizationHeader,
                request
        );
    }

    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable
            @Positive
            Long chatId,

            @PathVariable
            @Positive
            Long messageId,

            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,

            Authentication authentication
    ) {
        Long currentAccountId = (Long) authentication.getPrincipal();

        service.delete(
                chatId,
                messageId,
                currentAccountId,
                authorizationHeader
        );
    }
}