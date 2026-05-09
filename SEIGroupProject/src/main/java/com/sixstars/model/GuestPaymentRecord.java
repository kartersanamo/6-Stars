package com.sixstars.model;

import java.time.LocalDateTime;

/** A recorded payment against the guest ledger (Stripe or simulated saved card). */
public class GuestPaymentRecord {

    private final int id;
    private final String guestEmail;
    private final double amount;
    private final PaymentKind kind;
    private final String methodSummary;
    private final Integer savedMethodId;
    private final LocalDateTime createdAt;

    public GuestPaymentRecord(int id, String guestEmail, double amount, PaymentKind kind,
            String methodSummary, Integer savedMethodId, LocalDateTime createdAt) {
        this.id = id;
        this.guestEmail = guestEmail;
        this.amount = amount;
        this.kind = kind;
        this.methodSummary = methodSummary;
        this.savedMethodId = savedMethodId;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentKind getKind() {
        return kind;
    }

    public String getMethodSummary() {
        return methodSummary;
    }

    public Integer getSavedMethodId() {
        return savedMethodId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
