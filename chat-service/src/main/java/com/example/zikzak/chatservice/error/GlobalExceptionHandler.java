package com.example.zikzak.chatservice.error;

import com.example.zikzak.chatservice.chat.exception.InvalidChatParticipantException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidChatParticipantException.class)
    ProblemDetail handleInvalidChatParticipant(
            InvalidChatParticipantException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Invalid chat participant");
        return problem;
    }
}
