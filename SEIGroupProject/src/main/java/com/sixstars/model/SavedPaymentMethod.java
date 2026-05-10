package com.sixstars.model;

import java.time.LocalDateTime;

/** Guest-saved card metadata (no full PAN stored). */
public class SavedPaymentMethod {

    private int id;
    private final String guestEmail;
    private final String nickname;
    private final String cardBrand;
    private final String lastFour;
    private final int expMonth;
    private final int expYear;
    private final String nameOnCard;
    private final String line1;
    private final String line2;
    private final String city;
    private final String state;
    private final String zip;
    private final String phone;
    private final LocalDateTime createdAt;

    public SavedPaymentMethod(int id, String guestEmail, String nickname, String cardBrand, String lastFour,
            int expMonth, int expYear, String nameOnCard, String line1, String line2,
            String city, String state, String zip, String phone, LocalDateTime createdAt) {
        this.id = id;
        this.guestEmail = guestEmail;
        this.nickname = nickname;
        this.cardBrand = cardBrand;
        this.lastFour = lastFour;
        this.expMonth = expMonth;
        this.expYear = expYear;
        this.nameOnCard = nameOnCard;
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public String getNickname() {
        return nickname;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public String getLastFour() {
        return lastFour;
    }

    public int getExpMonth() {
        return expMonth;
    }

    public int getExpYear() {
        return expYear;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getDisplayLabel() {
        String nick = nickname == null || nickname.isBlank() ? "Card" : nickname.trim();
        return cardBrand + " •••• " + lastFour + " · " + nick;
    }

    @Override
    public String toString() {
        return getDisplayLabel();
    }
}
