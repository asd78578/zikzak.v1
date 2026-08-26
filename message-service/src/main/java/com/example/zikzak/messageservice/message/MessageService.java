package com.example.zikzak.messageservice.message;

import com.example.zikzak.messageservice.chat.ChatMembershipClient;
import com.example.zikzak.messageservice.message.dto.MessageResponse;
import com.example.zikzak.messageservice.message.dto.SendMessageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageRepository repository;
    private final ChatMembershipClient chatMembershipClient;

    public MessageService(
            MessageRepository repository,
            ChatMembershipClient chatMembershipClient
    ) {
        this.repository = repository;
        this.chatMembershipClient = chatMembershipClient;
    }

    @Transactional
    public MessageResponse send(
            Long chatId,
            Long senderAccountId,
            String authorizationHeader,
            SendMessageRequest request
    ) {
        chatMembershipClient.verifyMembership(
                chatId,
                authorizationHeader
        );

        Message message = new Message(
                chatId,
                senderAccountId,
                request.content().trim()
        );

        return toResponse(repository.saveAndFlush(message));
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> findHistory(
            Long chatId,
            String authorizationHeader,
            int page,
            int size
    ) {
        chatMembershipClient.verifyMembership(
                chatId,
                authorizationHeader
        );

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("createdAt"),
                        Sort.Order.asc("id")
                )
        );

        return repository.findByChatId(chatId, pageRequest)
                .map(this::toResponse);
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getChatId(),
                message.getSenderAccountId(),
                message.getContent(),
                message.getStatus(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }
}