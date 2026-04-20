package com.sixstars.service;

import com.sixstars.database.ShopItemDAO;
import com.sixstars.model.CartItem;
import com.sixstars.model.Item;
import com.sixstars.model.ShoppingCart;

import java.util.ArrayList;
import java.util.List;

public class ShopService {

    private ShopItemDAO dao = new ShopItemDAO();

    public List<Item> getInventory() {
        List<Item> available = new ArrayList<>();

        for (Item item : dao.getAllItems()) {
            if (item.getStock() > 0) {
                available.add(item);
            }
        }

        return available;
    }

    public double checkout(ShoppingCart cart) {
        double total = cart.getTotal();

        for (CartItem ci : cart.getItems()) {
            Item item = ci.getItem();
            int newStock = item.getStock() - ci.getQuantity();
            dao.updateStock(item.getId(), newStock);
        }

        cart.clear();
        return total;
    }
}