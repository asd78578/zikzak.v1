package com.example.zikzak.chatservice.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageEvent(
        UUID eventId,
        MessageEventType type,
        Long messageId,
        Long chatId,
        Long senderAccountId,
        String content,
        String status,
        OffsetDateTime occurredAt
) {
}
