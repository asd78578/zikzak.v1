package com.example.zikzak.exception;

import com.example.zikzak.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {

        ErrorResponse response = new ErrorResponse(ex.getMessage(), 404);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);

//        return ResponseEntity.status(404).body(ex.getMessage());
    }
}