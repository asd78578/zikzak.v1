package com.example.zikzak.messageservice.config;

import com.example.zikzak.messageservice.chat.ChatMembershipClient;
import com.example.zikzak.messageservice.security.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_ATTRIBUTE = "authorization";
    private static final String CHAT_TOPIC_PREFIX = "/topic/chats/";

    private final JwtService jwtService;
    private final ChatMembershipClient chatMembershipClient;

    public WebSocketAuthInterceptor(
            JwtService jwtService,
            ChatMembershipClient chatMembershipClient
    ) {
        this.jwtService = jwtService;
        this.chatMembershipClient = chatMembershipClient;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            verifySubscription(accessor);
        }

        return message;
    }

    private void authenticate(
            StompHeaderAccessor accessor
    ) {
        String authorization =
                accessor.getFirstNativeHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException(
                    "Missing WebSocket Authorization header"
            );
        }

        String token =
                authorization.substring(BEARER_PREFIX.length());

        if (!jwtService.isTokenValid(token)) {
            throw new IllegalArgumentException(
                    "Invalid WebSocket JWT"
            );
        }

        Long accountId =
                jwtService.extractUserId(token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        accountId,
                        null,
                        List.of()
                );

        accessor.setUser(authentication);

        if (accessor.getSessionAttributes() != null) {
            accessor.getSessionAttributes().put(
                    AUTHORIZATION_ATTRIBUTE,
                    authorization
            );
        }
    }

    private void verifySubscription(
            StompHeaderAccessor accessor
    ) {
        String destination = accessor.getDestination();

        if (destination == null
                || !destination.startsWith(CHAT_TOPIC_PREFIX)) {
            return;
        }

        String chatIdValue =
                destination.substring(CHAT_TOPIC_PREFIX.length());

        Long chatId;

        try {
            chatId = Long.valueOf(chatIdValue);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid chat destination"
            );
        }

        if (accessor.getSessionAttributes() == null) {
            throw new IllegalStateException(
                    "WebSocket session attributes are missing"
            );
        }

        String authorization =
                (String) accessor.getSessionAttributes()
                        .get(AUTHORIZATION_ATTRIBUTE);

        if (authorization == null) {
            throw new IllegalStateException(
                    "WebSocket authorization is missing"
            );
        }

        chatMembershipClient.verifyMembership(
                chatId,
                authorization
        );
    }
}
