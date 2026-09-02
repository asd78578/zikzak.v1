package com.example.zikzak.messageservice.message;

import com.example.zikzak.messageservice.chat.ChatMembershipClient;
import com.example.zikzak.messageservice.error.MessageAccessDeniedException;
import com.example.zikzak.messageservice.error.MessageNotFoundException;
import com.example.zikzak.messageservice.event.MessageEventPublisher;
import com.example.zikzak.messageservice.event.MessageEventType;
import com.example.zikzak.messageservice.message.dto.EditMessageRequest;
import com.example.zikzak.messageservice.message.dto.MessageResponse;
import com.example.zikzak.messageservice.message.dto.SendMessageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageRepository repository;
    private final ChatMembershipClient chatMembershipClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageEventPublisher eventPublisher;

    public MessageService(
            MessageRepository repository,
            ChatMembershipClient chatMembershipClient,
            SimpMessagingTemplate messagingTemplate,
            MessageEventPublisher eventPublisher
    ) {
        this.repository = repository;
        this.chatMembershipClient = chatMembershipClient;
        this.messagingTemplate = messagingTemplate;
        this.eventPublisher = eventPublisher;
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

        Message savedMessage = repository.saveAndFlush(message);
        MessageResponse response = toResponse(savedMessage);

        eventPublisher.publish(
                MessageEventType.MESSAGE_SENT,
                savedMessage
        );

        messagingTemplate.convertAndSend(
                "/topic/chats/" + chatId,
                response
        );

        return response;
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
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        return repository.findByChatId(chatId, pageRequest)
                .map(this::toResponse);
    }

    @Transactional
    public MessageResponse edit(
            Long chatId,
            Long messageId,
            Long currentAccountId,
            String authorizationHeader,
            EditMessageRequest request
    ) {
        chatMembershipClient.verifyMembership(
                chatId,
                authorizationHeader
        );

        Message message = findMessage(chatId, messageId);
        verifyOwner(message, currentAccountId);

        message.editContent(request.content().trim());

        Message savedMessage = repository.saveAndFlush(message);

        eventPublisher.publish(
                MessageEventType.MESSAGE_EDITED,
                savedMessage
        );

        return toResponse(savedMessage);
    }

    @Transactional
    public void delete(
            Long chatId,
            Long messageId,
            Long currentAccountId,
            String authorizationHeader
    ) {
        chatMembershipClient.verifyMembership(
                chatId,
                authorizationHeader
        );

        Message message = findMessage(chatId, messageId);
        verifyOwner(message, currentAccountId);

        message.softDelete();
        Message savedMessage = repository.saveAndFlush(message);

        eventPublisher.publish(
                MessageEventType.MESSAGE_DELETED,
                savedMessage
        );
    }

    private Message findMessage(
            Long chatId,
            Long messageId
    ) {
        return repository.findByIdAndChatId(messageId, chatId)
                .orElseThrow(
                        () -> new MessageNotFoundException(messageId)
                );
    }

    private void verifyOwner(
            Message message,
            Long currentAccountId
    ) {
        if (!message.getSenderAccountId().equals(currentAccountId)) {
            throw new MessageAccessDeniedException();
        }
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