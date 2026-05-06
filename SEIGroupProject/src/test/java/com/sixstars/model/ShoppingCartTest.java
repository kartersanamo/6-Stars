package com.sixstars.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {

    @Test
    void newCartStartsEmpty() {
        ShoppingCart cart = new ShoppingCart();

        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItems().size());
    }

    @Test
    void addItemAddsSingleItem() {
        ShoppingCart cart = new ShoppingCart();
        Item item = new Item(1, "Water", 2.00, 10, "water.png");

        cart.addItem(item);

        assertFalse(cart.isEmpty());
        assertEquals(1, cart.getItems().size());
        assertEquals(1, cart.getItems().get(0).getQuantity());
    }

    @Test
    void addSameItemIncreasesQuantityInsteadOfAddingDuplicate() {
        ShoppingCart cart = new ShoppingCart();
        Item item = new Item(1, "Water", 2.00, 10, "water.png");

        cart.addItem(item);
        cart.addItem(item, 3);

        assertEquals(1, cart.getItems().size());
        assertEquals(4, cart.getItems().get(0).getQuantity());
    }

    @Test
    void addItemIgnoresNullItem() {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem(null);

        assertTrue(cart.isEmpty());
    }

    @Test
    void addItemIgnoresInvalidQuantity() {
        ShoppingCart cart = new ShoppingCart();
        Item item = new Item(1, "Water", 2.00, 10, "water.png");

        cart.addItem(item, 0);

        assertTrue(cart.isEmpty());
    }

    @Test
    void decrementItemLowersQuantity() {
        ShoppingCart cart = new ShoppingCart();
        Item item = new Item(1, "Water", 2.00, 10, "water.png");

        cart.addItem(item, 3);
        cart.decrementItem(item);

        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    void decrementItemRemovesItemWhenQuantityIsOne() {
        ShoppingCart cart = new ShoppingCart();
        Item item = new Item(1, "Water", 2.00, 10, "water.png");

        cart.addItem(item);
        cart.decrementItem(item);

        assertTrue(cart.isEmpty());
    }

    @Test
    void removeItemRemovesMatchingItem() {
        ShoppingCart cart = new ShoppingCart();
        Item water = new Item(1, "Water", 2.00, 10, "water.png");
        Item chips = new Item(2, "Chips", 3.00, 10, "chips.png");

        cart.addItem(water);
        cart.addItem(chips);
        cart.removeItem(water);

        assertEquals(1, cart.getItems().size());
        assertEquals(chips, cart.getItems().get(0).getItem());
    }

    @Test
    void getTotalAddsAllCartItems() {
        ShoppingCart cart = new ShoppingCart();
        Item water = new Item(1, "Water", 2.00, 10, "water.png");
        Item chips = new Item(2, "Chips", 3.00, 10, "chips.png");

        cart.addItem(water, 2);
        cart.addItem(chips, 3);

        assertEquals(13.00, cart.getTotal(), 0.001);
    }

    @Test
    void clearRemovesAllItems() {
        ShoppingCart cart = new ShoppingCart();
        Item item = new Item(1, "Water", 2.00, 10, "water.png");

        cart.addItem(item);
        cart.clear();

        assertTrue(cart.isEmpty());
    }

    @Test
    void setItemsCopiesOnlyValidCartItems() {
        ShoppingCart cart = new ShoppingCart();
        Item item = new Item(1, "Water", 2.00, 10, "water.png");

        cart.setItems(List.of(new CartItem(item, 2)));

        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    void setItemsWithNullListClearsCart() {
        ShoppingCart cart = new ShoppingCart();
        Item item = new Item(1, "Water", 2.00, 10, "water.png");

        cart.addItem(item);
        cart.setItems(null);

        assertTrue(cart.isEmpty());
    }
}