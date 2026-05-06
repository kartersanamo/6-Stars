package com.sixstars.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void constructorSetsAllFields() {
        Item item = new Item(1, "Water Bottle", 4.99, 10, "assets/shopImages/water.png");

        assertEquals(1, item.getId());
        assertEquals("Water Bottle", item.getName());
        assertEquals(4.99, item.getPrice(), 0.001);
        assertEquals(10, item.getStock());
        assertEquals("assets/shopImages/water.png", item.getImagePath());
    }

    @Test
    void setStockUpdatesStock() {
        Item item = new Item(1, "Snack", 2.50, 5, "assets/shopImages/snack.png");

        item.setStock(0);

        assertEquals(0, item.getStock());
    }
}