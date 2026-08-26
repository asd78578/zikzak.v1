package com.example.zikzak.messageservice.message.dto;

import com.example.zikzak.messageservice.message.MessageStatus;

import java.time.OffsetDateTime;

public record MessageResponse(
        Long id,
        Long chatId,
        Long senderAccountId,
        String content,
        MessageStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}