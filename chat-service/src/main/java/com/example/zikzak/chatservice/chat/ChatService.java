package com.example.zikzak.chatservice.chat;

import com.example.zikzak.chatservice.chat.dto.ChatResponse;
import com.example.zikzak.chatservice.chat.exception.InvalidChatParticipantException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Transactional
    public ChatResponse createDirectChat(
            Long currentAccountId,
            Long participantAccountId
    ) {
        if (currentAccountId.equals(participantAccountId)) {
            throw new InvalidChatParticipantException();
        }

        String directKey = createDirectKey(
                currentAccountId,
                participantAccountId
        );

        Chat chat = chatRepository.findByDirectKey(directKey)
                .orElseGet(() -> {
                    Chat newChat = new Chat(directKey);
                    newChat.addMember(currentAccountId);
                    newChat.addMember(participantAccountId);
                    return chatRepository.saveAndFlush(newChat);
                });

        return ChatResponse.from(chat);
    }

    @Transactional(readOnly = true)
    public List<ChatResponse> findChats(Long currentAccountId) {
        return chatRepository
                .findDistinctByMembersAccountIdOrderByUpdatedAtDesc(
                        currentAccountId
                )
                .stream()
                .map(ChatResponse::from)
                .toList();
    }

    private String createDirectKey(
            Long firstAccountId,
            Long secondAccountId
    ) {
        long smaller = Math.min(firstAccountId, secondAccountId);
        long larger = Math.max(firstAccountId, secondAccountId);
        return smaller + ":" + larger;
    }
}