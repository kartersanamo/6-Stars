package com.sixstars.model;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private List<CartItem> items = new ArrayList<>();

    public void addItem(Item item) {
        for (CartItem cartItem : items) {
            if (cartItem.getItem().getId() == item.getId()) {
                cartItem.increment();
                return;
            }
        }
        items.add(new CartItem(item));
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotal() {
        double total = 0.0;
        for (CartItem cartItem : items) {
            total += cartItem.getTotalPrice();
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