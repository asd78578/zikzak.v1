package com.example.zikzak.chatservice.event;

import com.example.zikzak.chatservice.chat.Chat;
import com.example.zikzak.chatservice.chat.ChatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatMessageEventHandler {

    private final ChatRepository chatRepository;

    public ChatMessageEventHandler(
            ChatRepository chatRepository
    ) {
        this.chatRepository = chatRepository;
    }

    @Transactional
    public void handle(MessageEvent event) {
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
    }
}
