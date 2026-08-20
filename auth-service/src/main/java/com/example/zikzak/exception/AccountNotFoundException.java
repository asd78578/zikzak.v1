package com.example.zikzak.exception;

import com.example.zikzak.dto.AccountResponse;
import com.example.zikzak.exception.AccountNotFoundException;
import com.example.zikzak.user.Account;
import jakarta.transaction.Transactional;


public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long accountId) {
        super("Account not found: " + accountId);
    }

}
