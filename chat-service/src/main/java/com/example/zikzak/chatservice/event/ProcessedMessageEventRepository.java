package com.example.zikzak.chatservice.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedMessageEventRepository
        extends JpaRepository<ProcessedMessageEvent, UUID> {
}
