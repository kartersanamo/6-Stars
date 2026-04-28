package com.sixstars.controller;

import com.sixstars.model.Role;
import com.sixstars.model.Account;
import com.sixstars.service.AccountService;

public class AccountController {
    private final AccountService accountService;

    public static Account currentAccount;

    public AccountController() {
        accountService = new AccountService();
    }

    public Account createAccount(String firstName, String lastName, String email, String password, Role role) {
        return accountService.createAccount(firstName, lastName, email, password, role);
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public void updateProfile(String firstName, String lastName, String newPassword) {
        accountService.updateProfile(currentAccount, firstName, lastName, newPassword);
        currentAccount = accountService.getAccountByEmail(currentAccount.getEmail());
    }

    public void sendVerificationCode(String email) {
        accountService.sendVerificationCode(email);
    }

    public boolean verifyAccountEmail(String email, String code) {
        return accountService.verifyEmailCode(email, code);
    }

    public void resendVerificationCode(String email) {
        accountService.resendVerificationCode(email);
    }

    public Account getAccountByEmail(String email) {
        return accountService.getAccountByEmail(email);
    }
}
