package com.sixstars.service;

import com.sixstars.database.SystemSettingsDAO;

public class PricingSettingsService {
    private static final String KEY_GLOBAL_DISCOUNT_RATE = "global_discount_rate";
    private static final double DEFAULT_DISCOUNT_RATE = 0.0;
    private static final double MAX_ALLOWED_DISCOUNT_RATE = 0.80;

    private final SystemSettingsDAO systemSettingsDAO;

    public PricingSettingsService() {
        this.systemSettingsDAO = new SystemSettingsDAO();
    }

    public double getGlobalDiscountRate() {
        String value = systemSettingsDAO.getValue(KEY_GLOBAL_DISCOUNT_RATE);
        if (value == null || value.isBlank()) {
            return DEFAULT_DISCOUNT_RATE;
        }

        try {
            double parsed = Double.parseDouble(value);
            if (parsed < 0 || parsed > MAX_ALLOWED_DISCOUNT_RATE) {
                return DEFAULT_DISCOUNT_RATE;
            }
            return parsed;
        } catch (NumberFormatException ex) {
            return DEFAULT_DISCOUNT_RATE;
        }
    }

    public void setGlobalDiscountRate(double discountRate) {
        if (discountRate < 0 || discountRate > MAX_ALLOWED_DISCOUNT_RATE) {
            throw new IllegalArgumentException("Discount rate must be between 0% and 80%.");
        }
        systemSettingsDAO.upsertValue(KEY_GLOBAL_DISCOUNT_RATE, String.valueOf(discountRate));
    }
}
