package com.sixstars.controller;

import com.sixstars.model.Account;
import com.sixstars.service.AccountService;

public class LoginController {
    public static Account checkLogin(String username, String password) {
        AccountService accountService = new AccountService();
        Account authenticatedUser = accountService.authenticate(username, password);
        if (authenticatedUser != null) {
            AccountController.currentAccount = authenticatedUser;
        }
        return authenticatedUser;
    }
}
