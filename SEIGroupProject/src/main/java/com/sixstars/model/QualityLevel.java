package com.sixstars.model;

public enum QualityLevel {
    EXECUTIVE(329),
    BUSINESS(259),
    COMFORT(199),
    ECONOMY(149);

    private final int maxDailyRate;

    QualityLevel(int maxDailyRate) {
        this.maxDailyRate = maxDailyRate;
    }

    public int getMaxDailyRate() {
        return maxDailyRate;
    }
}
