package com.financetracker.model;
 
/**
 * Represents an Expense transaction.
 */
public class Expense extends Transaction {

    public Expense(double amount, String category, String date) {
        super(amount, category, date);
    }

    @Override
    public double getImpact() {
        return -getAmount(); // Negative impact
    }

    @Override
    public String getType() {
        return "Expense";
    }
}

