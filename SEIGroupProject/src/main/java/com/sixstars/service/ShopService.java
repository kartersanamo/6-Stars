package com.sixstars.service;

import com.sixstars.model.Item;
import com.sixstars.model.ShoppingCart;

import java.util.ArrayList;
import java.util.List;

public class ShopService {
    private List<Item> inventory = new ArrayList<>();

    public ShopService() {
        // Sample hotel shop items
        inventory.add(new Item("Water Bottle", 2.50));
        inventory.add(new Item("Snacks", 3.00));
        inventory.add(new Item("Toothbrush Kit", 5.00));
        inventory.add(new Item("Hotel T-Shirt", 15.00));
        inventory.add(new Item("Sunscreen", 8.00));
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public double checkout(ShoppingCart cart) {
        double total = cart.getTotal();
        cart.clear(); // simulate purchase
        return total;
    }
}