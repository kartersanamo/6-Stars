package com.sixstars.model;

public class CartItem {
    private Item item;
    private int quantity;

    public CartItem(Item item) {
        this(item, 1);
    }

    public CartItem(Item item, int quantity) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
        this.item = item;
        this.quantity = quantity;
    }

    public void increment() {
        quantity++;
    }

    public void decrement() {
        if (quantity > 1) {
            quantity--;
        }
    }

    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
        this.quantity = quantity;
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return item.getPrice() * quantity;
    }
}