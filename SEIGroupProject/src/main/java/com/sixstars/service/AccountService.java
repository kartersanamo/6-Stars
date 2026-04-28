package com.sixstars.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;

import com.sixstars.database.AccountDAO;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class AccountService {
    private final AccountDAO accountDAO;
    private static final Pattern PASSWORD_UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern PASSWORD_LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile(".*[0-9].*");
    private static final Pattern PASSWORD_SPECIAL = Pattern.compile(".*[^A-Za-z0-9].*");

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

    public void updateAccount(Account account) {
        accountDAO.saveAccount(account);
    }

    public void updateProfile(Account performer, String fName, String lName, String profileImagePath) {
        if (performer == null || performer.getRole() != Role.CLERK) {
            throw new RuntimeException("Only clerks can edit profiles.");
        }

        Account updated = new Account(fName, lName, performer.getEmail(), performer.getPasswordHash(), performer.getRole(), profileImagePath);
        accountDAO.saveAccount(updated);
    }

    public void changePassword(Account performer, String currentPassword, String newPassword, String confirmPassword) {
        if (performer == null) {
            throw new RuntimeException("No active account found.");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("Please enter your current password.");
        }
        if (!performer.getPasswordHash().equals(hashPassword(currentPassword))) {
            throw new RuntimeException("Current password is incorrect.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("Please enter a new password.");
        }
        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new RuntimeException("Please confirm your new password.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("New password and confirmation do not match.");
        }
        if (newPassword.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long.");
        }
        if (!PASSWORD_UPPER.matcher(newPassword).matches()
                || !PASSWORD_LOWER.matcher(newPassword).matches()
                || !PASSWORD_DIGIT.matcher(newPassword).matches()
                || !PASSWORD_SPECIAL.matcher(newPassword).matches()) {
            throw new RuntimeException("Password must include uppercase, lowercase, number, and special character.");
        }
        if (performer.getPasswordHash().equals(hashPassword(newPassword))) {
            throw new RuntimeException("New password must be different from the current password.");
        }

        Account updated = new Account(
                performer.getFirstName(),
                performer.getLastName(),
                performer.getEmail(),
                hashPassword(newPassword),
                performer.getRole(),
                performer.getProfileImagePath()
        );
        accountDAO.saveAccount(updated);
    }


    public Account getAccountByEmail(String email) {
        return accountDAO.getAccountByEmail(email);
    }
}
