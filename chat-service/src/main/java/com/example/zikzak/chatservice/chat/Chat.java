package com.example.zikzak.chatservice.chat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "chats")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatType type;

    @Column(name = "direct_key", nullable = false, unique = true, length = 100)
    private String directKey;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @Column(name = "last_message_preview", length = 200)
    private String lastMessagePreview;

    @Column(name = "last_message_at")
    private OffsetDateTime lastMessageAt;

    @OneToMany(
            mappedBy = "chat",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ChatMember> members = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Chat() {
    }

    public Chat(String directKey) {
        this.type = ChatType.DIRECT;
        this.directKey = directKey;
    }

    public void addMember(Long accountId) {
        members.add(new ChatMember(this, accountId));
    }

    public void applySentMessage(
            Long messageId,
            String content,
            OffsetDateTime occurredAt
    ) {
        this.lastMessageId = messageId;
        this.lastMessagePreview = createPreview(content);
        this.lastMessageAt = occurredAt;
    }

    public void applyEditedMessage(
            Long messageId,
            String content
    ) {
        if (isLastMessage(messageId)) {
            this.lastMessagePreview = createPreview(content);
        }
    }

    public void applyDeletedMessage(Long messageId) {
        if (isLastMessage(messageId)) {
            this.lastMessagePreview = "Сообщение удалено";
        }
    }

    private boolean isLastMessage(Long messageId) {
        return lastMessageId != null
                && lastMessageId.equals(messageId);
    }

    private String createPreview(String content) {
        if (content == null) {
            return "";
        }

        String normalized = content.strip();

        if (normalized.length() <= 200) {
            return normalized;
        }

        return normalized.substring(0, 197) + "...";
    }

    public Long getId() {
        return id;
    }

    public ChatType getType() {
        return type;
    }

    public String getDirectKey() {
        return directKey;
    }

    public List<ChatMember> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public Long getLastMessageId() {
        return lastMessageId;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public OffsetDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
