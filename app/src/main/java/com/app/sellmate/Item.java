package com.app.sellmate;

import java.io.Serializable;

public class Item implements Serializable{
    private String id;
    private String name;
    private double sellingPrice;

    // Constructor without ID
    public Item(String name, double sellingPrice) {
        this.name = name;
        this.sellingPrice = sellingPrice;
        // Optionally generate a unique ID here
        this.id = generateUniqueId(); // Assuming you have a method for this
    }

    // Constructor with ID
    public Item(String id, String name, double sellingPrice) {
        this.id = id;
        this.name = name;
        this.sellingPrice = sellingPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        if (sellingPrice >= 0) { // Example validation
            this.sellingPrice = sellingPrice;
        } else {
            throw new IllegalArgumentException("Selling price cannot be negative");
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return id != null ? id.equals(item.id) : item.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // Optional method to generate a unique ID
    private String generateUniqueId() {
        // Implement ID generation logic
        return "ID_" + System.currentTimeMillis(); // Example
    }
}
