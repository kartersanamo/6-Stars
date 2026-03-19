package com.sixstars.logicClasses;

public class AccountController {
    private final AccountService accountService;

    public AccountController() {
        accountService = new AccountService();
    }

    public Account createGuestAccount(String firstName, String lastName, String email, String password) {
        return accountService.createGuestAccount(firstName, lastName, email, password);
    }
}
