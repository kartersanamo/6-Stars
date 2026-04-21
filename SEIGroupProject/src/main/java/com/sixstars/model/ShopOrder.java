package com.sixstars.model;

import java.time.LocalDate;
import java.util.List;

public class ShopOrder {
    private int id;
    private String guestEmail;
    private LocalDate purchaseDate;
    private double totalCost;
    private List<ShopOrderItem> items;

    public ShopOrder(String guestEmail, LocalDate purchaseDate, double totalCost, List<ShopOrderItem> items) {
        this.guestEmail = guestEmail;
        this.purchaseDate = purchaseDate;
        this.totalCost = totalCost;
        this.items = items;
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

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public List<ShopOrderItem> getItems() {
        return items;
    }
}