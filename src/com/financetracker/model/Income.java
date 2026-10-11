package com.financetracker.model;
 
/**
 * Represents an Income transaction.
 */
public class Income extends Transaction {

    public Income(double amount, String category, String date) {
        super(amount, category, date);
    }

    @Override
    public double getImpact() {
        return getAmount(); // Positive impact
    }

    @Override
    public String getType() {
        return "Income";
    }
}

