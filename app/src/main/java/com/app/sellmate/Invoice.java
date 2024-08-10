package com.app.sellmate;

public class Invoice {
    private String id;
    private String invoiceNumber;
    private String invoiceDate;
    private String customerId;
    private String salesmanId;
    private double totalAmount;
    private double discount;

    public Invoice(String id, String invoiceNumber, String invoiceDate, String customerId, String salesmanId, double totalAmount, double discount) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.customerId = customerId;
        this.salesmanId = salesmanId;
        this.totalAmount = totalAmount;
        this.discount = discount;
    }

    public Invoice(String invoiceNumber, String invoiceDate, String customerId, double totalAmount, double discount) {
        this(null, invoiceNumber, invoiceDate, customerId, null, totalAmount, discount);
    }

    public String getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String getInvoiceDate() { return invoiceDate; }
    public String getCustomerId() { return customerId; }
    public String getSalesmanId() { return salesmanId; }
    public double getTotalAmount() { return totalAmount; }
    public double getDiscount() { return discount; }
}
