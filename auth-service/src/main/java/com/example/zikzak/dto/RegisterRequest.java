package com.example.zikzak.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email не должен быть пустым")
        @Email(message = "Некорректный формат email")
        @Size(
                max = 255,
                message = "Email не должен превышать 255 символов"
        )
        String email,

        @NotBlank(message = "Пароль не должен быть пустым")
        @Size(
                min = 8,
                max = 64,
                message = "Пароль должен содержать от 8 до 64 символов"
        )
        String password
) {
}
