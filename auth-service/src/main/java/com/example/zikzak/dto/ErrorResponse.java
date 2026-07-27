package com.example.zikzak.dto;

public record ErrorResponse(
        String message,
        int status
) {}
