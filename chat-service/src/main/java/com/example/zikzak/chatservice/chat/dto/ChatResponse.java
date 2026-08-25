package com.example.zikzak.chatservice.chat.dto;

import com.example.zikzak.chatservice.chat.Chat;
import com.example.zikzak.chatservice.chat.ChatMember;
import com.example.zikzak.chatservice.chat.ChatType;

import java.time.OffsetDateTime;
import java.util.List;

public record ChatResponse(
        Long id,
        ChatType type,
        List<Long> memberAccountIds,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static ChatResponse from(Chat chat) {
        List<Long> memberAccountIds = chat.getMembers()
                .stream()
                .map(ChatMember::getAccountId)
                .sorted()
                .toList();

        return new ChatResponse(
                chat.getId(),
                chat.getType(),
                memberAccountIds,
                chat.getCreatedAt(),
                chat.getUpdatedAt()
        );
    }
}
