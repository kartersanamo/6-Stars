package com.sixstars.model;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private List<CartItem> items = new ArrayList<>();

    public void addItem(Item item) {
        addItem(item, 1);
    }

    public void addItem(Item item, int quantity) {
        if (item == null || quantity < 1) {
            return;
        }

        for (CartItem ci : items) {
            if (ci.getItem().getId() == item.getId()) {
                ci.setQuantity(ci.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(item, quantity));
    }

    public void decrementItem(Item item) {
        for (CartItem ci : items) {
            if (ci.getItem().getId() == item.getId()) {
                if (ci.getQuantity() > 1) {
                    ci.decrement();
                } else {
                    removeItem(item);
                }
                return;
            }
        }
    }

    public void removeItem(Item item) {
        items.removeIf(ci -> ci.getItem().getId() == item.getId());
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = new ArrayList<>();
        if (items == null) {
            return;
        }

        for (CartItem item : items) {
            if (item != null && item.getItem() != null && item.getQuantity() > 0) {
                this.items.add(new CartItem(item.getItem(), item.getQuantity()));
            }
        }
    }

    public double getTotal() {
        double total = 0;
        for (CartItem ci : items) {
            total += ci.getTotalPrice();
        }
        return total;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }
}