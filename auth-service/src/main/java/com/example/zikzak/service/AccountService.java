package com.example.zikzak.service;

import com.example.zikzak.component.AccountMapper;
import com.example.zikzak.dto.AccountResponse;
import com.example.zikzak.dto.LoginRequest;
import com.example.zikzak.dto.RegisterRequest;
import com.example.zikzak.exception.EmailAlreadyExistsException;
import com.example.zikzak.exception.InvalidCredentialsException;
import com.example.zikzak.user.Account;
import com.example.zikzak.user.AccountRepository;
import com.example.zikzak.user.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;

    public AccountService(
            AccountRepository accountRepository,
            AccountMapper accountMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public AccountResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (accountRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        Account account = new Account();
        account.setEmail(email);
        account.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        account.setRole(Role.USER);

        Account savedAccount = accountRepository.save(account);

        return accountMapper.toResponse(savedAccount);
    }

    public Account loginCheck(LoginRequest request) {
        String email = normalizeEmail(request.email());

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                request.password(),
                account.getPasswordHash()
        )) {
            throw new InvalidCredentialsException();
        }

        return account;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}