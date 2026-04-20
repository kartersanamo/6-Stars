package com.sixstars.model;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private List<CartItem> items = new ArrayList<>();

    public void addItem(Item item) {
        for (CartItem ci : items) {
            if (ci.getItem().getName().equals(item.getName())) {
                ci.increment();
                return;
            }
        }
        items.add(new CartItem(item));
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

    public void clear() {
        items.clear();
    }
}