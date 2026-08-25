package com.example.zikzak.chatservice.chat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMemberRepository
        extends JpaRepository<ChatMember, Long> {

    boolean existsByChatIdAndAccountId(
            Long chatId,
            Long accountId
    );
}
