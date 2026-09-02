package com.example.zikzak.messageservice;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.example.zikzak.messageservice.event.MessageEventPublisher;
import org.springframework.boot.test.mock.mockito.MockBean;

@Testcontainers
public abstract class PostgresContainerTest {

    private static final String TEST_JWT_SECRET =
            "0123456789012345678901234567890123456789012345678901234567890123";

    @MockBean
    protected MessageEventPublisher eventPublisher;
    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("zikzak_messages_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );
        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );
        registry.add(
                "security.jwt.secret",
                () -> TEST_JWT_SECRET
        );
    }
}