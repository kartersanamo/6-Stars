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

    public void updateProfileDetails(String firstName, String lastName, String profileImagePath) {
        ensureCurrentAccount();
        accountService.updateProfile(currentAccount, firstName, lastName, profileImagePath);
        currentAccount = accountService.getAccountByEmail(currentAccount.getEmail());
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        ensureCurrentAccount();
        accountService.changePassword(currentAccount, currentPassword, newPassword, confirmPassword);
        currentAccount = accountService.getAccountByEmail(currentAccount.getEmail());
    }

    public void updateProfileImage(String profileImagePath) {
        ensureCurrentAccount();
        accountService.updateProfile(currentAccount, currentAccount.getFirstName(), currentAccount.getLastName(), profileImagePath);
        currentAccount = accountService.getAccountByEmail(currentAccount.getEmail());
    }

    public void removeProfileImage() {
        updateProfileImage(null);
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    @Deprecated
    public void updateProfile(String firstName, String lastName, String newPassword) {
        ensureCurrentAccount();
        accountService.updateProfile(currentAccount, firstName, lastName, currentAccount.getProfileImagePath());
        currentAccount = accountService.getAccountByEmail(currentAccount.getEmail());
    }

    private void ensureCurrentAccount() {
        if (currentAccount == null) {
            throw new IllegalStateException("No account is currently logged in.");
        }
    }
}
