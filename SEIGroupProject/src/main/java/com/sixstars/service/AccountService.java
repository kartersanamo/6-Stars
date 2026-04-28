package com.sixstars.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

import com.sixstars.database.AccountDAO;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class AccountService {
    private final AccountDAO accountDAO;
    private final MailgunEmailSender mailgunEmailSender;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public AccountService() {
        accountDAO = new AccountDAO();
        String apiKey = env("MAILGUN_API_KEY");
        String domain = env("MAILGUN_DOMAIN");
        String fromEmail = env("MAILGUN_FROM_EMAIL");
        if (apiKey != null && domain != null && fromEmail != null) {
            mailgunEmailSender = new MailgunEmailSender(apiKey, domain, fromEmail);
        } else {
            mailgunEmailSender = null;
        }
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
        boolean verified = role != Role.GUEST;
        Account guestAccount = new Account(firstName.trim(), lastName.trim(), normalizedEmail, passwordHash, role, verified, null, null);
        accountDAO.saveAccount(guestAccount);

        return guestAccount;
    }

    private String env(String key) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? null : value.trim();
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
        Account account = accountDAO.getAccountByEmail(email == null ? null : email.trim().toLowerCase());
        if (account != null && account.isEmailVerified() && account.getPasswordHash().equals(hashPassword(password))) {
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
        if (performer == null) {
            throw new RuntimeException("You must be logged in to edit your profile.");
        }

        String passwordHash = (newPass != null && !newPass.isBlank())
                ? hashPassword(newPass)
                : performer.getPasswordHash();

        Account updated = new Account(
                fName,
                lName,
                performer.getEmail(),
                passwordHash,
                performer.getRole(),
                performer.getEmailVerified(),
                performer.getVerificationCodeHash(),
                performer.getVerificationExpiresAt()
        );
        accountDAO.saveAccount(updated);
    }

    public void sendVerificationCode(String email) {
        if (mailgunEmailSender == null) {
            throw new RuntimeException("Mailgun is not configured. Set MAILGUN_API_KEY, MAILGUN_DOMAIN, and MAILGUN_FROM_EMAIL.");
        }

        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new RuntimeException("Email is required.");
        }
        Account account = accountDAO.getAccountByEmail(normalizedEmail);
        if (account == null) {
            throw new RuntimeException("Account not found.");
        }

        String code = generateVerificationCode();
        String codeHash = hashPassword(code);
        String expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES).toString();

        accountDAO.updateVerificationState(normalizedEmail, false, codeHash, expiresAt);

        try {
            mailgunEmailSender.sendVerificationCode(normalizedEmail, code);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        }
    }

    public boolean verifyEmailCode(String email, String code) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        if (normalizedEmail == null || normalizedEmail.isBlank() || code == null || code.isBlank()) {
            return false;
        }
        Account account = accountDAO.getAccountByEmail(normalizedEmail);
        if (account == null) {
            return false;
        }

        if (account.isEmailVerified()) {
            return true;
        }

        if (account.getVerificationCodeHash() == null || account.getVerificationExpiresAt() == null) {
            return false;
        }

        Instant expiresAt;
        try {
            expiresAt = Instant.parse(account.getVerificationExpiresAt());
        } catch (Exception ex) {
            return false;
        }

        if (Instant.now().isAfter(expiresAt)) {
            return false;
        }

        if (!hashPassword(code).equals(account.getVerificationCodeHash())) {
            return false;
        }

        accountDAO.updateVerificationState(normalizedEmail, true, null, null);
        return true;
    }

    public void resendVerificationCode(String email) {
        sendVerificationCode(email);
    }

    private String generateVerificationCode() {
        int value = 100000 + SECURE_RANDOM.nextInt(900000);
        return Integer.toString(value);
    }

    public Account getAccountByEmail(String email) {
        return accountDAO.getAccountByEmail(email);
    }
}
