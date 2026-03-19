package com.sixstars.logicClasses;

import java.util.ArrayList;

public class AccountController {
    private ArrayList<Account> accounts;

    public void createGuestAccount(String firstName, String lastName, String email, String password) {
        Account account = new Account(firstName, lastName, password, email, Role.GUEST);
        accounts.add(account);
    }
}
