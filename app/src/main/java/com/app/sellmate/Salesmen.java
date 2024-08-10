package com.app.sellmate;

public class Salesmen {
    private String id;
    private String name;
    private String contactNumber;
    private String nicNumber;

    // Default constructor
    public Salesmen() {}

    // Constructor without ID (for new salesmen)
    public Salesmen(String name, String contactNumber, String nicNumber) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.nicNumber = nicNumber;
        // Optionally generate a unique ID here
        this.id = generateUniqueId(); // Assuming you have a method for this
    }

    // Constructor with ID (for existing salesmen)
    public Salesmen(String id, String name, String contactNumber, String nicNumber) {
        this.id = id;
        this.name = name;
        this.contactNumber = contactNumber;
        this.nicNumber = nicNumber;
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
        // Add validation if needed
        this.contactNumber = contactNumber;
    }

    public String getNicNumber() {
        return nicNumber;
    }

    public void setNicNumber(String nicNumber) {
        // Add validation if needed
        this.nicNumber = nicNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Salesmen salesmen = (Salesmen) o;
        return id != null ? id.equals(salesmen.id) : salesmen.id == null;
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
