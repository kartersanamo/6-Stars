package com.sixstars.service;

import com.sixstars.database.ShopItemDAO;
import com.sixstars.model.CartItem;
import com.sixstars.model.Item;
import com.sixstars.model.ShoppingCart;

import java.util.ArrayList;
import java.util.List;

public class ShopService {

    private ShopItemDAO shopItemDAO;

    public ShopService() {
        shopItemDAO = new ShopItemDAO();
    }

    public List<Item> getInventory() {
        List<Item> allItems = shopItemDAO.getAllItems();
        List<Item> availableItems = new ArrayList<>();

        for (Item item : allItems) {
            if (item.getStock() > 0) {
                availableItems.add(item);
            }
        }

        return availableItems;
    }

    public double checkout(ShoppingCart cart) {
        double total = cart.getTotal();

        for (CartItem cartItem : cart.getItems()) {
            Item dbItem = shopItemDAO.getItemById(cartItem.getItem().getId());

            if (dbItem == null) {
                throw new IllegalStateException("Item not found: " + cartItem.getItem().getName());
            }

            if (dbItem.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException("Not enough stock for " + dbItem.getName());
            }

            int newStock = dbItem.getStock() - cartItem.getQuantity();
            shopItemDAO.updateStock(dbItem.getId(), newStock);
        }

        cart.clear();
        return total;
    }
}