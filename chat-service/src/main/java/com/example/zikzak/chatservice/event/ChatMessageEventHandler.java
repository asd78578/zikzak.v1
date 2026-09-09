package com.example.zikzak.chatservice.event;

import com.example.zikzak.chatservice.chat.Chat;
import com.example.zikzak.chatservice.chat.ChatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ChatMessageEventHandler {

    private final ChatRepository chatRepository;
    private final ProcessedMessageEventRepository processedMessageEventRepository;
    private final KafkaMetrics kafkaMetrics;

    public ChatMessageEventHandler(
            ChatRepository chatRepository,
            ProcessedMessageEventRepository processedMessageEventRepository,
            KafkaMetrics kafkaMetrics
    ) {
        this.chatRepository = chatRepository;
        this.processedMessageEventRepository = processedMessageEventRepository;
        this.kafkaMetrics = kafkaMetrics;
    }

    @Transactional
    public MessageEventHandlingResult handle(MessageEvent event) {

        if (processedMessageEventRepository.existsById(event.eventId())) {
            kafkaMetrics.incrementDuplicate();
            return MessageEventHandlingResult.DUPLICATE;
        }

        Chat chat = chatRepository.findById(event.chatId())
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Chat with id "
                                        + event.chatId()
                                        + " was not found"
                        )
                );

        switch (event.type()) {
            case MESSAGE_SENT ->
                    chat.applySentMessage(
                            event.messageId(),
                            event.content(),
                            event.occurredAt()
                    );

            case MESSAGE_EDITED ->
                    chat.applyEditedMessage(
                            event.messageId(),
                            event.content()
                    );

            case MESSAGE_DELETED ->
                    chat.applyDeletedMessage(
                            event.messageId()
                    );
        }

        processedMessageEventRepository.save(
                new ProcessedMessageEvent(
                        event.eventId(),
                        OffsetDateTime.now()
                )
        );

        return MessageEventHandlingResult.PROCESSED;
    }
}