package com.app.sellmate;

import java.io.Serializable;

public class Item implements Serializable {
    private String id; // Optional, not used for uniqueness
    private String name;
    private double sellingPrice;
    private String existingid; // Unique identifier

    // Constructor without ID
    public Item(String name, double sellingPrice, String existingid) {
        this.name = name;
        this.sellingPrice = sellingPrice;
        this.existingid = existingid;
    }

    // Constructor with ID
    public Item(String id, String name, double sellingPrice, String existingid) {
        this.id = id;
        this.name = name;
        this.sellingPrice = sellingPrice;
        this.existingid = existingid;
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
        if (sellingPrice >= 0) {
            this.sellingPrice = sellingPrice;
        } else {
            throw new IllegalArgumentException("Selling price cannot be negative");
        }
    }

    public String getExistingid() {
        return existingid;
    }

    public void setExistingid(String existingid) {
        this.existingid = existingid;
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
        return existingid != null ? existingid.equals(item.existingid) : item.existingid == null;
    }

    @Override
    public int hashCode() {
        return existingid != null ? existingid.hashCode() : 0;
    }
}
