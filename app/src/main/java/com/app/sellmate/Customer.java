package com.app.sellmate;

import java.io.Serializable;

public class Customer implements Serializable {
    private String id;
    private String name;
    private String contactNumber;
    private String contactPersonName;
    private String address;

    // Default constructor
    public Customer() {}

    // Constructor without ID (for new customers)
    public Customer(String name, String contactNumber, String contactPersonName, String address) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.contactPersonName = contactPersonName;
        this.address = address;
    }

    // Constructor with ID (for existing customers)
    public Customer(String id, String name, String contactNumber, String contactPersonName, String address) {
        this.id = id;
        this.name = name;
        this.contactNumber = contactNumber;
        this.contactPersonName = contactPersonName;
        this.address = address;
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

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return id != null ? id.equals(customer.id) : customer.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
