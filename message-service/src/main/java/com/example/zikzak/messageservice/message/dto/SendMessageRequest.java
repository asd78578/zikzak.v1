package com.example.zikzak.messageservice.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(

        @NotBlank(message = "Message content must not be blank")
        @Size(
                max = 4000,
                message = "Message content must not exceed 4000 characters"
        )
        String content
) {
}