package com.example.zikzak.messageservice.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByChatId(
            Long chatId,
            Pageable pageable
    );

    Optional<Message> findByIdAndChatId(
            Long id,
            Long chatId
    );
}