package com.financetracker.model;

/**
 * Abstract class representing a generic financial transaction.
 */
public abstract class Transaction {
    private double amount;
    private String category;
    private String date;
    private String note;

    public Transaction(double amount, String category, String date) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = "";
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Subclasses must implement these to specify how they affect balance and how they are classified
    public abstract double getImpact();

    public abstract String getType();
}
