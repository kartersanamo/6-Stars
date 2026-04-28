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
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public AccountService() {
        accountDAO = new AccountDAO();
    }

    public Account createAccount(String firstName, String lastName, String email, String password, Role role) {
        if (firstName == null || firstName.isBlank() ||
                lastName == null || lastName.isBlank() ||
                email == null || email.isBlank() ||
                password == null || password.isBlank()) {
            throw new RuntimeException("Please fill in all fields.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new RuntimeException("Please enter a valid email address.");
        }

        validatePasswordStrength(password);

        if (role == Role.ADMIN) {
            throw new RuntimeException("Admin account creation is not allowed.");
        }

        if (accountDAO.getAccountByEmail(normalizedEmail) != null){
            throw new RuntimeException("An account with that email already exists!");
        }

        String passwordHash = hashPassword(password);
        Account guestAccount = new Account(firstName.trim(), lastName.trim(), normalizedEmail, passwordHash, role);
        accountDAO.saveAccount(guestAccount);

        return guestAccount;
    }

    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long.");
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            throw new RuntimeException("Password must include uppercase, lowercase, number, and special character.");
        }
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

    public void updateProfile(Account performer, String fName, String lName, String newPass) {
        // Basic verification
        if (performer == null || performer.getRole() != Role.CLERK) {
            throw new RuntimeException("Only clerks can edit profiles.");
        }

        String passwordHash = (newPass != null && !newPass.isBlank())
                ? hashPassword(newPass)
                : performer.getPasswordHash();

        Account updated = new Account(fName, lName, performer.getEmail(), passwordHash, performer.getRole());
        accountDAO.saveAccount(updated);
    }

    public Account getAccountByEmail(String email) {
        return accountDAO.getAccountByEmail(email);
    }
}
