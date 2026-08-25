package com.example.zikzak.chatservice.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByDirectKey(String directKey);

    List<Chat> findDistinctByMembersAccountIdOrderByUpdatedAtDesc(
            Long accountId
    );
}