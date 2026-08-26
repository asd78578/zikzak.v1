package com.example.zikzak.messageservice;

import com.example.zikzak.messageservice.message.Message;
import com.example.zikzak.messageservice.message.MessageRepository;
import com.example.zikzak.messageservice.message.MessageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MessageRepositoryIntegrationTest extends com.example.zikzak.messageservice.PostgresContainerTest {

    @Autowired
    private MessageRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void shouldSaveMessage() {
        Message saved = repository.saveAndFlush(
                new Message(10L, 100L, "Hello ZikZak")
        );

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getChatId()).isEqualTo(10L);
        assertThat(saved.getSenderAccountId()).isEqualTo(100L);
        assertThat(saved.getContent()).isEqualTo("Hello ZikZak");
        assertThat(saved.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindOnlyMessagesOfRequestedChat() {
        repository.saveAndFlush(
                new Message(20L, 101L, "First message")
        );
        repository.saveAndFlush(
                new Message(20L, 102L, "Second message")
        );
        repository.saveAndFlush(
                new Message(30L, 103L, "Other chat")
        );

        var page = repository.findByChatId(
                20L,
                historyPage()
        );

        assertThat(page.getContent())
                .extracting(Message::getContent)
                .containsExactly(
                        "First message",
                        "Second message"
                );
    }

    @Test
    void shouldReturnEmptyPageForUnknownChat() {
        var page = repository.findByChatId(
                999L,
                historyPage()
        );

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    private PageRequest historyPage() {
        return PageRequest.of(
                0,
                20,
                Sort.by(
                        Sort.Order.asc("createdAt"),
                        Sort.Order.asc("id")
                )
        );
    }
}