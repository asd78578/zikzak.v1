package com.example.zikzak.chatservice.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateDirectChatRequest(

        @NotNull
        @Positive
        Long participantAccountId
) {
}
