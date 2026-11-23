package com.business.dashboard.smallbuisness;

import java.time.LocalDate;

public class Expense {
    private int id;
    private String category;
    private String description;
    private double amount;
    private LocalDate expenseDate;

    // Constructor for new expense
    public Expense(String category, String description, double amount,
                   LocalDate expenseDate) {
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
    }

    // Constructor from database
    public Expense(int id, String category, String description, double amount,
                   LocalDate expenseDate) {
        this.id = id;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

}
