package com.sixstars.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    @Test
    void constructorDefaultsQuantityToOne() {
        Item item = new Item(1, "Soda", 3.00, 10, "soda.png");

        CartItem cartItem = new CartItem(item);

        assertEquals(item, cartItem.getItem());
        assertEquals(1, cartItem.getQuantity());
    }

    @Test
    void constructorSetsGivenQuantity() {
        Item item = new Item(1, "Soda", 3.00, 10, "soda.png");

        CartItem cartItem = new CartItem(item, 4);

        assertEquals(4, cartItem.getQuantity());
    }

    @Test
    void constructorThrowsWhenItemIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new CartItem(null));
    }

    @Test
    void constructorThrowsWhenQuantityIsLessThanOne() {
        Item item = new Item(1, "Soda", 3.00, 10, "soda.png");

        assertThrows(IllegalArgumentException.class, () -> new CartItem(item, 0));
    }

    @Test
    void incrementIncreasesQuantity() {
        Item item = new Item(1, "Soda", 3.00, 10, "soda.png");
        CartItem cartItem = new CartItem(item);

        cartItem.increment();

        assertEquals(2, cartItem.getQuantity());
    }

    @Test
    void decrementDoesNotGoBelowOne() {
        Item item = new Item(1, "Soda", 3.00, 10, "soda.png");
        CartItem cartItem = new CartItem(item);

        cartItem.decrement();

        assertEquals(1, cartItem.getQuantity());
    }

    @Test
    void getTotalPriceReturnsPriceTimesQuantity() {
        Item item = new Item(1, "Soda", 3.00, 10, "soda.png");
        CartItem cartItem = new CartItem(item, 4);

        assertEquals(12.00, cartItem.getTotalPrice(), 0.001);
    }
}