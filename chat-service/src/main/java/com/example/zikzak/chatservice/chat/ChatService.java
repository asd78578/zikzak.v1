package com.example.zikzak.chatservice.chat;

import com.example.zikzak.chatservice.chat.dto.ChatResponse;
import com.example.zikzak.chatservice.chat.exception.InvalidChatParticipantException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    private final ChatMemberRepository memberRepository;

    public ChatService(
            ChatRepository chatRepository,
            ChatMemberRepository memberRepository
    ) {
        this.chatRepository = chatRepository;
        this.memberRepository = memberRepository;
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

    @Transactional(readOnly = true)
    public boolean isMember(
            Long chatId,
            Long accountId
    ) {
        return memberRepository.existsByChatIdAndAccountId(
                chatId,
                accountId
        );
    }
}