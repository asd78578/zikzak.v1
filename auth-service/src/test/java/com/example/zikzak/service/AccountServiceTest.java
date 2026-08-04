package com.example.zikzak.service;

import com.example.zikzak.component.AccountMapper;
import com.example.zikzak.dto.LoginRequest;
import com.example.zikzak.dto.RegisterRequest;
import com.example.zikzak.exception.EmailAlreadyExistsException;
import com.example.zikzak.exception.InvalidCredentialsException;
import com.example.zikzak.user.Account;
import com.example.zikzak.user.AccountRepository;
import com.example.zikzak.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    private PasswordEncoder passwordEncoder;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        accountService = new AccountService(
                accountRepository,
                accountMapper,
                passwordEncoder
        );
    }

    @Test
    void shouldRegisterAccount() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "  USER@ZIKZAK.RU  ",
                "secret123"
        );

        when(accountRepository.existsByEmail("user@zikzak.ru"))
                .thenReturn(false);

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        accountService.register(request);

        // Assert
        ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).save(captor.capture());

        Account savedAccount = captor.getValue();

        assertThat(savedAccount.getEmail())
                .isEqualTo("user@zikzak.ru");

        assertThat(savedAccount.getRole())
                .isEqualTo(Role.USER);

        assertThat(savedAccount.getPasswordHash())
                .isNotEqualTo("secret123");

        assertThat(passwordEncoder.matches(
                "secret123",
                savedAccount.getPasswordHash()
        )).isTrue();

        verify(accountMapper).toResponse(savedAccount);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "USER@ZIKZAK.RU",
                "secret123"
        );

        when(accountRepository.existsByEmail("user@zikzak.ru"))
                .thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> accountService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(accountRepository, never())
                .save(any(Account.class));

        verifyNoInteractions(accountMapper);
    }

    // тест неправильного пароля
    @Test
    void shouldRejectIncorrectPassword() {
        // Arrange
        LoginRequest request = new LoginRequest(
                "USER@ZIKZAK.RU",
                "wrongpassword"
        );

        Account account = new Account();
        account.setEmail("user@zikzak.ru");
        account.setPasswordHash(
                passwordEncoder.encode("secret123")
        );
        account.setRole(Role.USER);

        when(accountRepository.findByEmail("user@zikzak.ru"))
                .thenReturn(Optional.of(account));

        // Act + Assert
        assertThatThrownBy(
                () -> accountService.loginCheck(request)
        )
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");

        verify(accountRepository)
                .findByEmail("user@zikzak.ru");

        verifyNoInteractions(accountMapper);
    }
}
