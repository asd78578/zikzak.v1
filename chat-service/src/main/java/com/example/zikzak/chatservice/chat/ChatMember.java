package com.example.zikzak.chatservice.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "chat_members")
public class ChatMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;

    protected ChatMember() {
    }

    ChatMember(Chat chat, Long accountId) {
        this.chat = chat;
        this.accountId = accountId;
    }

    public Long getId() {
        return id;
    }

    public Chat getChat() {
        return chat;
    }

    public Long getAccountId() {
        return accountId;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }
}
