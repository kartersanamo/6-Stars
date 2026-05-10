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
import com.sixstars.model.NotificationType;
import com.sixstars.model.Role;

public class AccountService {
    private final AccountDAO accountDAO;
    private final MailgunEmailSender mailgunEmailSender;
    private static final Pattern PASSWORD_UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern PASSWORD_LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile(".*[0-9].*");
    private static final Pattern PASSWORD_SPECIAL = Pattern.compile(".*[^A-Za-z0-9].*");
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
        // Try System.getenv() first (for shell-set variables), then fall back to System.getProperty() (for .env-loaded variables)
        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key);
        }
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

    /** Best-effort security email; no-op when Mailgun is not configured or send fails. */
    public void sendSecurityNoticeEmailIfConfigured(String email, String subject, String htmlBody) {
        if (mailgunEmailSender == null || email == null || email.isBlank() || subject == null || htmlBody == null) {
            return;
        }
        String to = email.trim().toLowerCase();
        try {
            mailgunEmailSender.sendHtmlEmail(to, subject, htmlBody);
        } catch (Exception ignored) {
            // Do not block sign-in if outbound mail fails
        }
    }

    public List<Account> getAllAccounts() {
        return accountDAO.getAllAccounts();
    }

    public void updateAccount(Account account) {
        accountDAO.saveAccount(account);
    }

    public void updateProfile(Account performer, String fName, String lName, String profileImagePath) {
        if (performer == null) {
            throw new RuntimeException("You must be logged in to edit your profile.");
        }

        // Update the performer's name and profile image, preserving all other fields including verification state
        Account updated = new Account(
                fName,
                lName,
                performer.getEmail(),
                performer.getPasswordHash(),
                performer.getRole(),
                performer.getEmailVerified(),
                performer.getVerificationCodeHash(),
                performer.getVerificationExpiresAt(),
                profileImagePath
        );
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
        NotificationService notifications = NotificationService.getInstance();
        notifications.publish(NotificationType.PASSWORD_AND_VERIFICATION, performer.getEmail(),
                "Your password was changed successfully.");
        notifications.publish(NotificationType.APP_UPDATES_AND_LEGAL, performer.getEmail(),
                "Security reminder: if you did not change your password, reset it again and contact the hotel.");
    }

    public void sendVerificationCode(String email) {
        issueCode(email, false);
    }

    public void sendPasswordResetCode(String email) {
        issueCode(email, true);
    }

    public void resetPasswordWithCode(String email, String code, String newPassword, String confirmPassword) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new RuntimeException("Email is required.");
        }
        if (code == null || code.isBlank()) {
            throw new RuntimeException("Please enter the access code.");
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

        validatePasswordStrength(newPassword);

        Account account = accountDAO.getAccountByEmail(normalizedEmail);
        if (account == null) {
            throw new RuntimeException("Account not found.");
        }

        if (account.getVerificationCodeHash() == null || account.getVerificationExpiresAt() == null) {
            throw new RuntimeException("Please request an access code first.");
        }

        Instant expiresAt;
        try {
            expiresAt = Instant.parse(account.getVerificationExpiresAt());
        } catch (Exception ex) {
            throw new RuntimeException("Your access code has expired. Please request a new one.");
        }

        if (Instant.now().isAfter(expiresAt)) {
            throw new RuntimeException("Your access code has expired. Please request a new one.");
        }

        if (!hashPassword(code).equals(account.getVerificationCodeHash())) {
            throw new RuntimeException("Invalid access code.");
        }

        if (account.getPasswordHash().equals(hashPassword(newPassword))) {
            throw new RuntimeException("New password must be different from the current password.");
        }

        Account updated = new Account(
                account.getFirstName(),
                account.getLastName(),
                account.getEmail(),
                hashPassword(newPassword),
                account.getRole(),
                account.getEmailVerified(),
                null,
                null,
                account.getProfileImagePath()
        );
        accountDAO.saveAccount(updated);
        accountDAO.updateVerificationState(normalizedEmail, account.isEmailVerified(), null, null);
        NotificationService.getInstance().publish(NotificationType.PASSWORD_AND_VERIFICATION, normalizedEmail,
                "Your password was reset using the code sent to your email.");
    }

    private void issueCode(String email, boolean passwordReset) {
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

        accountDAO.updateVerificationState(normalizedEmail, passwordReset && account.isEmailVerified(), codeHash, expiresAt);

        try {
            if (passwordReset) {
                mailgunEmailSender.sendPasswordResetCode(normalizedEmail, code);
            } else {
                mailgunEmailSender.sendVerificationCode(normalizedEmail, code);
            }
            NotificationService ns = NotificationService.getInstance();
            if (passwordReset) {
                ns.publish(NotificationType.PASSWORD_AND_VERIFICATION, normalizedEmail,
                        "Password reset code sent to your inbox (valid 15 minutes).");
            } else {
                ns.publish(NotificationType.PASSWORD_AND_VERIFICATION, normalizedEmail,
                        "Email verification code sent (valid 15 minutes).");
            }
        } catch (Exception e) {
            throw new RuntimeException((passwordReset ? "Failed to send password reset email: " : "Failed to send verification email: ") + e.getMessage(), e);
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
        NotificationService ns = NotificationService.getInstance();
        ns.publish(NotificationType.ACCOUNT_ACTIVITY, normalizedEmail, "Email address verified on your account.");
        ns.publish(NotificationType.PRIVACY_AND_POLICY, normalizedEmail,
                "You can manage privacy-related notices under Account Center → Notifications.");
        return true;
    }

    public void resendVerificationCode(String email) {
        sendVerificationCode(email);
    }

    public void sendAccountActionCode(String email) {
        issueActionCode(email, "Account action verification");
    }

    public boolean verifyAccountActionCode(String email, String code) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        if (normalizedEmail == null || normalizedEmail.isBlank() || code == null || code.isBlank()) {
            return false;
        }

        Account account = accountDAO.getAccountByEmail(normalizedEmail);
        if (account == null || account.getVerificationCodeHash() == null || account.getVerificationExpiresAt() == null) {
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

        accountDAO.updateVerificationState(normalizedEmail, account.isEmailVerified(), null, null);
        NotificationService.getInstance().publish(NotificationType.PASSWORD_AND_VERIFICATION, normalizedEmail,
                "Account action verified with your emailed code.");
        return true;
    }

    public void deleteAccount(String email) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new RuntimeException("Account email is required.");
        }
        if (accountDAO.getAccountByEmail(normalizedEmail) == null) {
            throw new RuntimeException("Account not found.");
        }
        accountDAO.deleteAccountByEmail(normalizedEmail);
    }

    private String generateVerificationCode() {
        int value = 100000 + SECURE_RANDOM.nextInt(900000);
        return Integer.toString(value);
    }

    private void issueActionCode(String email, String purpose) {
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
        accountDAO.updateVerificationState(normalizedEmail, account.isEmailVerified(), codeHash, expiresAt);

        try {
            mailgunEmailSender.sendVerificationCode(normalizedEmail, code);
            NotificationService.getInstance().publish(NotificationType.PASSWORD_AND_VERIFICATION, normalizedEmail,
                    "Account action verification code sent (valid 15 minutes).");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send " + purpose.toLowerCase() + " email: " + e.getMessage(), e);
        }
    }


    public Account getAccountByEmail(String email) {
        return accountDAO.getAccountByEmail(email);
    }
}
