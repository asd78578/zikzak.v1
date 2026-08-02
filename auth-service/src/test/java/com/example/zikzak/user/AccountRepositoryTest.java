package com.example.zikzak.user;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldFindAccountByEmail() {
        // Arrange — подготавливаем данные
        Account account = new Account();
        account.setEmail("test@zikzak.ru");
        account.setPasswordHash("encoded-password");
        account.setRole(Role.USER);

        accountRepository.saveAndFlush(account);

        // Act — выполняем проверяемое действие
        Optional<Account> result =
                accountRepository.findByEmail("test@zikzak.ru");

        // Assert — проверяем результат
        assertThat(result).isPresent();
        assertThat(result.get().getEmail())
                .isEqualTo("test@zikzak.ru");
        assertThat(result.get().getRole())
                .isEqualTo(Role.USER);
        assertThat(result.get().getCreatedAt())
                .isNotNull();
    }


    @Test
    void shouldReturnTrueWhenEmailExists() {
        // Arrange
        Account account = new Account();
        account.setEmail("exists@zikzak.ru");
        account.setPasswordHash("encoded-password");
        account.setRole(Role.USER);

        accountRepository.saveAndFlush(account);

        // Act
        boolean exists =
                accountRepository.existsByEmail("exists@zikzak.ru");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        boolean exists =
                accountRepository.existsByEmail("missing@zikzak.ru");

        assertThat(exists).isFalse();
    }

}
