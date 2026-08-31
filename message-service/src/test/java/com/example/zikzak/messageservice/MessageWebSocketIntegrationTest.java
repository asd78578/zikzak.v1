package com.example.zikzak.messageservice;

import com.example.zikzak.messageservice.chat.ChatMembershipClient;
import com.example.zikzak.messageservice.message.MessageService;
import com.example.zikzak.messageservice.message.dto.MessageResponse;
import com.example.zikzak.messageservice.message.dto.SendMessageRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class MessageWebSocketIntegrationTest
        extends PostgresContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatMembershipClient chatMembershipClient;

    @Test
    void shouldReceiveMessageThroughWebSocket() throws Exception {

        BlockingQueue<MessageResponse> messages =
                new ArrayBlockingQueue<>(1);

        WebSocketStompClient stompClient =
                new WebSocketStompClient(
                        new StandardWebSocketClient()
                );

        MappingJackson2MessageConverter converter =
                new MappingJackson2MessageConverter();

        converter.setObjectMapper(objectMapper);

        stompClient.setMessageConverter(converter);

        StompSession session = stompClient
                .connectAsync(
                        "ws://localhost:" + port + "/ws",
                        new WebSocketHttpHeaders(),
                        new StompSessionHandlerAdapter() {
                        }
                )
                .get();

        session.subscribe(
                "/topic/chats/10",
                new StompFrameHandler() {

                    @Override
                    public Type getPayloadType(
                            org.springframework.messaging.simp.stomp
                                    .StompHeaders headers
                    ) {
                        return MessageResponse.class;
                    }

                    @Override
                    public void handleFrame(
                            org.springframework.messaging.simp.stomp
                                    .StompHeaders headers,
                            Object payload
                    ) {
                        messages.offer(
                                (MessageResponse) payload
                        );
                    }
                }
        );

        Thread.sleep(300);

        messageService.send(
                10L,
                100L,
                "Bearer test-token",
                new SendMessageRequest(
                        "Hello WebSocket"
                )
        );

        MessageResponse received =
                messages.poll(
                        5,
                        TimeUnit.SECONDS
                );

        assertThat(received).isNotNull();
        assertThat(received.chatId()).isEqualTo(10L);
        assertThat(received.senderAccountId())
                .isEqualTo(100L);
        assertThat(received.content())
                .isEqualTo("Hello WebSocket");

        session.disconnect();
        stompClient.stop();
    }
}
