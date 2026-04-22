package com.sixstars.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.sixstars.database.AccountDAO;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class AccountService {
    private final AccountDAO accountDAO;

    public AccountService() {
        accountDAO = new AccountDAO();
    }

    public Account createAccount(String firstName, String lastName, String email, String password, Role role) {
        if (accountDAO.getAccountByEmail(email) != null){
            throw new RuntimeException("An account with that email already exists!");
        }

        String passwordHash = hashPassword(password);
        Account guestAccount = new Account(firstName, lastName, email, passwordHash, role);
        accountDAO.saveAccount(guestAccount);

        return guestAccount;
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not hash password.", e);
        }
    }
    public Account authenticate(String email, String password) {
        Account account = accountDAO.getAccountByEmail(email);
        if (account != null && account.getPasswordHash().equals(hashPassword(password))) {
            return account;
        }
        return null;
    }

    public List<Account> getAllAccounts() {
        return accountDAO.getAllAccounts();
    }
}
