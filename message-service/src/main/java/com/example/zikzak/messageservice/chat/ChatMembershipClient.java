package com.example.zikzak.messageservice.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ChatMembershipClient {

    private final RestClient restClient;

    public ChatMembershipClient(
            RestClient.Builder builder,
            @Value("${services.chat.url}") String chatServiceUrl
    ) {
        this.restClient = builder
                .baseUrl(chatServiceUrl)
                .build();
    }

    public void verifyMembership(
            Long chatId,
            String authorizationHeader
    ) {
        ChatMembershipResponse response = restClient.get()
                .uri(
                        "/api/v1/chats/{chatId}/membership/me",
                        chatId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .body(ChatMembershipResponse.class);

        if (response == null || !response.member()) {
            throw new AccessDeniedException(
                    "Current account is not a member of chat " + chatId
            );
        }
    }
}