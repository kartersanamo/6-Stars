package com.sixstars.model;

public class ShopOrderItem {
    private int id;
    private Integer itemId;
    private String itemName;
    private double unitPrice;
    private int quantity;
    private double lineTotal;

    public ShopOrderItem(Integer itemId, String itemName, double unitPrice, int quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = unitPrice * quantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getLineTotal() {
        return lineTotal;
    }
}