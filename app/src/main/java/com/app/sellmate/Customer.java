package com.app.sellmate;

import java.io.Serializable;

public class Customer implements Serializable {
    private String id;
    private String name;
    private String contactNumber;
    private String contactPersonName;
    private String address;
    private String existingid;

    // Default constructor
    public Customer() {}

    // Constructor without ID
    public Customer(String name, String contactNumber, String contactPersonName, String address, String existingid) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.contactPersonName = contactPersonName;
        this.address = address;
        this.existingid = existingid;
    }

    // Constructor with ID
    public Customer(String id, String name, String contactNumber, String contactPersonName, String address, String existingid) {
        this.id = id;
        this.name = name;
        this.contactNumber = contactNumber;
        this.contactPersonName = contactPersonName;
        this.address = address;
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
        Customer customer = (Customer) o;
        return existingid != null ? existingid.equals(customer.existingid) : customer.existingid == null;
    }

    @Override
    public int hashCode() {
        return existingid != null ? existingid.hashCode() : 0;
    }
}
