package com.sixstars.logicClasses;

public class AccountController {
    private final AccountService accountService;

    public static Account currentAccount;

    public AccountController() {
        accountService = new AccountService();
    }

    public Account createAccount(String firstName, String lastName, String email, String password, Role role) {
        return accountService.createAccount(firstName, lastName, email, password, role);
    }
}
