package com.example.zikzak.component;

import com.example.zikzak.dto.AccountResponse;
import com.example.zikzak.user.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getEmail(),
                account.getRole(),
                account.getCreatedAt()
        );
    }
}
