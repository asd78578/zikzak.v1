package com.example.zikzak.messageservice.event;

import com.example.zikzak.messageservice.message.MessageStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageEvent(
        UUID eventId,
        MessageEventType type,
        Long messageId,
        Long chatId,
        Long senderAccountId,
        String content,
        MessageStatus status,
        OffsetDateTime occurredAt
) {
}