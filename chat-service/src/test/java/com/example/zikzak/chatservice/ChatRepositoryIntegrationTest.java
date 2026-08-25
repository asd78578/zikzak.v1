package com.example.zikzak.chatservice;

import com.example.zikzak.chatservice.chat.Chat;
import com.example.zikzak.chatservice.chat.ChatMemberRepository;
import com.example.zikzak.chatservice.chat.ChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ChatRepositoryIntegrationTest
        extends com.example.zikzak.chatservice.PostgresContainerTest {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatMemberRepository memberRepository;

    @BeforeEach
    void cleanDatabase() {
        memberRepository.deleteAll();
        chatRepository.deleteAll();
    }

    @Test
    void shouldSaveChatWithTwoMembers() {
        Chat chat = createChat(10L, 20L);

        Chat saved = chatRepository.saveAndFlush(chat);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDirectKey()).isEqualTo("10:20");
        assertThat(saved.getMembers())
                .extracting(member -> member.getAccountId())
                .containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    void shouldFindChatByDirectKey() {
        chatRepository.saveAndFlush(createChat(11L, 21L));

        var found = chatRepository.findByDirectKey("11:21");

        assertThat(found).isPresent();
        assertThat(found.orElseThrow().getMembers()).hasSize(2);
    }

    @Test
    void shouldFindOnlyChatsOfAccount() {
        chatRepository.saveAndFlush(createChat(12L, 22L));
        chatRepository.saveAndFlush(createChat(13L, 23L));

        var chats = chatRepository
                .findDistinctByMembersAccountIdOrderByUpdatedAtDesc(12L);

        assertThat(chats).hasSize(1);
        assertThat(chats.getFirst().getDirectKey())
                .isEqualTo("12:22");
    }

    @Test
    void shouldRejectDuplicateDirectChat() {
        chatRepository.saveAndFlush(createChat(14L, 24L));

        assertThatThrownBy(() ->
                chatRepository.saveAndFlush(createChat(14L, 24L))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    private Chat createChat(Long firstAccountId, Long secondAccountId) {
        String directKey =
                Math.min(firstAccountId, secondAccountId)
                        + ":"
                        + Math.max(firstAccountId, secondAccountId);

        Chat chat = new Chat(directKey);
        chat.addMember(firstAccountId);
        chat.addMember(secondAccountId);
        return chat;
    }
}
