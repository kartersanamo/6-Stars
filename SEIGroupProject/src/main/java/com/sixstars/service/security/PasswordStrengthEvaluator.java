package com.sixstars.service.security;

import java.util.regex.Pattern;

/** Mirrors production-style rules aligned with {@link com.sixstars.service.AccountService} password policy. */
public final class PasswordStrengthEvaluator {

    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    private PasswordStrengthEvaluator() {
    }

    public record Result(
            int score,
            boolean lengthOk,
            boolean upperOk,
            boolean lowerOk,
            boolean digitOk,
            boolean specialOk
    ) {
        public String tierLabel() {
            if (score <= 1) {
                return "Too weak";
            }
            if (score <= 3) {
                return "Fair";
            }
            if (score == 4) {
                return "Good";
            }
            return "Strong";
        }
    }

    public static Result evaluate(String password) {
        if (password == null || password.isEmpty()) {
            return new Result(0, false, false, false, false, false);
        }
        boolean len = password.length() >= 8;
        boolean upper = UPPER.matcher(password).find();
        boolean lower = LOWER.matcher(password).find();
        boolean digit = DIGIT.matcher(password).find();
        boolean spec = SPECIAL.matcher(password).find();
        int score = 0;
        if (len) {
            score++;
        }
        if (upper) {
            score++;
        }
        if (lower) {
            score++;
        }
        if (digit) {
            score++;
        }
        if (spec) {
            score++;
        }
        if (password.length() >= 12 && len && upper && lower && digit && spec) {
            score = Math.min(5, score + 1);
        }
        return new Result(Math.min(5, score), len, upper, lower, digit, spec);
    }

    public static boolean matches(String newPassword, String confirm) {
        if (newPassword == null || confirm == null) {
            return false;
        }
        return newPassword.equals(confirm);
    }
}
