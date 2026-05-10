package com.sixstars.service.stripe;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Client-side validators for billing and card fields before starting Stripe-hosted flows (UI only — no PAN storage). */
public final class PaymentBillingValidator {

    private PaymentBillingValidator() {
    }

    public static ValidationResult validateBillingProfile(String name, String line1, String city, String state, String zip) {
        StringBuilder errs = new StringBuilder();
        if (isBlank(name)) {
            errs.append("Full name on account is required.\n");
        }
        if (isBlank(line1)) {
            errs.append("Street address is required.\n");
        }
        if (isBlank(city)) {
            errs.append("City is required.\n");
        }
        if (!isBlank(state) && state.trim().length() != 2) {
            errs.append("Use a 2-letter state code.\n");
        }
        String zipDigits = normalizeDigits(zip == null ? "" : zip.trim());
        if (zipDigits.length() != 5 && zipDigits.length() != 9) {
            errs.append("ZIP must be 5 digits or 9 digits.\n");
        }
        boolean ok = errs.isEmpty();
        return new ValidationResult(ok, ok ? "" : errs.toString().trim());
    }

    public static ValidationResult validateCardPracticeFields(String rawNumber, String mmYy, String cvv, String postalOptional) {
        StringBuilder errs = new StringBuilder();

        String pan = normalizeDigits(rawNumber);
        if (pan.length() < 13 || pan.length() > 19) {
            errs.append("Card number must contain 13–19 digits.\n");
        } else if (!luhnOk(pan)) {
            errs.append("Card number does not pass checksum.\n");
        }

        String mmYyClean = normalizeExpiryMmYy(mmYy == null ? "" : mmYy);
        YearMonth ym = parseExpiry(mmYyClean);
        if (ym == null) {
            errs.append("Enter expiry as MMYY or MM/YY.\n");
        } else {
            YearMonth now = YearMonth.now();
            if (ym.isBefore(now)) {
                errs.append("This card appears expired.\n");
            }
        }

        String cvDigits = normalizeDigits(cvv);
        if (cvDigits.length() != 3 && cvDigits.length() != 4) {
            errs.append("CVV must be 3 or 4 digits.\n");
        }

        if (!PaymentBillingValidator.isBlank(postalOptional)) {
            String zipDigits = normalizeDigits(postalOptional);
            if (zipDigits.length() != 5 && zipDigits.length() != 9) {
                errs.append("Billing ZIP must be 5 or 9 digits if provided.\n");
            }
        }

        boolean ok = errs.isEmpty();
        return new ValidationResult(ok, ok ? "" : errs.toString().trim());
    }

    static String normalizeExpiryMmYy(String raw) {
        String s = raw.trim().replace("/", "").replaceAll("\\s+", "");
        if (s.length() == 4) {
            return s;
        }
        if (s.length() >= 6) {
            try {
                return YearMonth.parse(s.substring(0, 6), DateTimeFormatter.ofPattern("MMyyyy"))
                        .format(DateTimeFormatter.ofPattern("MMuu"));
            } catch (DateTimeParseException ignored) {
            }
        }
        return s;
    }

    private static YearMonth parseExpiry(String mmYyClean) {
        if (mmYyClean.length() != 4) {
            return null;
        }
        try {
            int mm = Integer.parseInt(mmYyClean.substring(0, 2));
            int yy = Integer.parseInt(mmYyClean.substring(2, 4));
            if (mm < 1 || mm > 12) {
                return null;
            }
            int fullYear = 2000 + yy;
            return YearMonth.of(fullYear, mm);
        } catch (NumberFormatException | DateTimeParseException ex) {
            return null;
        }
    }

    static boolean isBlank(String s) {
        return s == null || s.strip().isEmpty();
    }

    static boolean luhnOk(String pan) {
        int sum = 0;
        boolean alt = false;
        for (int i = pan.length() - 1; i >= 0; i--) {
            char c = pan.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            int n = c - '0';
            if (alt) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alt = !alt;
        }
        return sum % 10 == 0;
    }

    private static String normalizeDigits(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c)) {
                b.append(c);
            }
        }
        return b.toString();
    }

    /** Visa / Mastercard / Amex / Discover heuristic from PAN digits only. */
    public static String inferCardBrand(String rawPan) {
        String pan = normalizeDigits(rawPan == null ? "" : rawPan);
        if (pan.isEmpty()) {
            return "Card";
        }
        char c0 = pan.charAt(0);
        if (c0 == '4') {
            return "Visa";
        }
        if (c0 == '5') {
            return "Mastercard";
        }
        if (c0 == '3') {
            char c1 = pan.length() > 1 ? pan.charAt(1) : '0';
            if (c1 == '4' || c1 == '7') {
                return "Amex";
            }
            return "Card";
        }
        if (c0 == '6') {
            return "Discover";
        }
        return "Card";
    }

    public static String lastFourDigits(String rawPan) {
        String pan = normalizeDigits(rawPan == null ? "" : rawPan);
        if (pan.length() < 4) {
            return "0000";
        }
        return pan.substring(pan.length() - 4);
    }

    /** @return {month, fourDigitYear} or null if invalid */
    public static int[] parseExpiryMonthYear(String mmYyInput) {
        String mmYyClean = normalizeExpiryMmYy(mmYyInput == null ? "" : mmYyInput);
        YearMonth ym = parseExpiry(mmYyClean);
        if (ym == null) {
            return null;
        }
        return new int[]{ym.getMonthValue(), ym.getYear()};
    }

    public static ValidationResult validateOptionalNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return new ValidationResult(true, "");
        }
        String t = nickname.trim();
        if (t.length() > 48) {
            return new ValidationResult(false, "Nickname must be at most 48 characters.");
        }
        return new ValidationResult(true, "");
    }

    public record ValidationResult(boolean ok, String message) {
    }
}
