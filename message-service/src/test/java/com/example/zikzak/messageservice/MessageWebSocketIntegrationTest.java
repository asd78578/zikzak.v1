package com.example.zikzak.messageservice;

import com.example.zikzak.messageservice.chat.ChatMembershipClient;
import com.example.zikzak.messageservice.message.MessageService;
import com.example.zikzak.messageservice.message.dto.MessageResponse;
import com.example.zikzak.messageservice.message.dto.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

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

        String authorization = bearerToken(100L);

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

        StompHeaders connectHeaders = new StompHeaders();

        connectHeaders.add(
                "Authorization",
                authorization
        );

        StompSession session = stompClient
                .connectAsync(
                        "ws://localhost:" + port + "/ws",
                        new WebSocketHttpHeaders(),
                        connectHeaders,
                        new StompSessionHandlerAdapter() {
                        }
                )
                .get();

        session.subscribe(
                "/topic/chats/10",
                new StompFrameHandler() {

                    @Override
                    public Type getPayloadType(
                            StompHeaders headers
                    ) {
                        return MessageResponse.class;
                    }

                    @Override
                    public void handleFrame(
                            StompHeaders headers,
                            Object payload
                    ) {
                        messages.offer(
                                (MessageResponse) payload
                        );
                    }
                }
        );

        Thread.sleep(300);

        verify(chatMembershipClient)
                .verifyMembership(
                        10L,
                        authorization
                );

        messageService.send(
                10L,
                100L,
                authorization,
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

        assertThat(received.chatId())
                .isEqualTo(10L);

        assertThat(received.senderAccountId())
                .isEqualTo(100L);

        assertThat(received.content())
                .isEqualTo("Hello WebSocket");

        if (session.isConnected()) {
            session.disconnect();
        }

        stompClient.stop();
    }

    @Test
    void shouldCheckMembershipWhenSubscribingToChat()
            throws Exception {

        String authorization = bearerToken(200L);

        doThrow(
                new AccessDeniedException(
                        "Not a chat member"
                )
        ).when(chatMembershipClient)
                .verifyMembership(
                        99L,
                        authorization
                );

        WebSocketStompClient stompClient =
                new WebSocketStompClient(
                        new StandardWebSocketClient()
                );

        MappingJackson2MessageConverter converter =
                new MappingJackson2MessageConverter();

        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);

        StompHeaders connectHeaders = new StompHeaders();

        connectHeaders.add(
                "Authorization",
                authorization
        );

        StompSession session = stompClient
                .connectAsync(
                        "ws://localhost:" + port + "/ws",
                        new WebSocketHttpHeaders(),
                        connectHeaders,
                        new StompSessionHandlerAdapter() {
                        }
                )
                .get();

        session.subscribe(
                "/topic/chats/99",
                new StompFrameHandler() {

                    @Override
                    public Type getPayloadType(
                            StompHeaders headers
                    ) {
                        return MessageResponse.class;
                    }

                    @Override
                    public void handleFrame(
                            StompHeaders headers,
                            Object payload
                    ) {
                    }
                }
        );

        Thread.sleep(500);

        verify(chatMembershipClient)
                .verifyMembership(
                        99L,
                        authorization
                );

        if (session.isConnected()) {
            session.disconnect();
        }

        stompClient.stop();
    }

    private String bearerToken(Long accountId) {
        return "Bearer "
                + TestJwtFactory.createToken(accountId);
    }

    @Test
    void shouldRejectWebSocketConnectWithoutJwt() throws Exception {

        WebSocketStompClient stompClient =
                new WebSocketStompClient(
                        new StandardWebSocketClient()
                );

        StompHeaders connectHeaders = new StompHeaders();

        var future = stompClient.connectAsync(
                "ws://localhost:" + port + "/ws",
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {
                }
        );

        assertThatThrownBy(future::get);

        stompClient.stop();
    }

    @Test
    void shouldRejectWebSocketConnectWithInvalidJwt() throws Exception {

        WebSocketStompClient stompClient =
                new WebSocketStompClient(
                        new StandardWebSocketClient()
                );

        StompHeaders connectHeaders = new StompHeaders();

        connectHeaders.add(
                "Authorization",
                "Bearer invalid-token"
        );

        var future = stompClient.connectAsync(
                "ws://localhost:" + port + "/ws",
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {
                }
        );

        assertThatThrownBy(future::get);

        stompClient.stop();
    }
}