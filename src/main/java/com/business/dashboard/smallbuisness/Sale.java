package com.business.dashboard.smallbuisness;

import java.time.LocalDate;

public class Sale {
    private int id;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double totalAmount;
    private LocalDate saleDate;
    private String customerName;

    // Constructor for new sale
    public Sale(String productName, int quantity, double unitPrice,
                LocalDate saleDate, String customerName) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = quantity * unitPrice;
        this.saleDate = saleDate;
        this.customerName = customerName;
    }

    // Constructor from database
    public Sale(int id, String productName, int quantity, double unitPrice,
                double totalAmount, LocalDate saleDate, String customerName) {
        this.id = id;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.saleDate = saleDate;
        this.customerName = customerName;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalAmount = quantity * unitPrice;
    }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.totalAmount = quantity * unitPrice;
    }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
