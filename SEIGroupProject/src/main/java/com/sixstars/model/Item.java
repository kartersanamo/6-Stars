package com.sixstars.model;

public class Item {
    private int id;
    private String name;
    private double price;
    private int stock;
    private String imagePath;

    public Item(int id, String name, double price, int stock, String imagePath) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imagePath = imagePath;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getImagePath() { return imagePath; }

    public void setStock(int stock) {
        this.stock = stock;
    }
}