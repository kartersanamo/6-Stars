package com.sixstars.model;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private List<CartItem> items = new ArrayList<>();

    public void addItem(Item item) {
        for (CartItem ci : items) {
            if (ci.getItem().getId() == item.getId()) {
                ci.increment();
                return;
            }
        }
        items.add(new CartItem(item));
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