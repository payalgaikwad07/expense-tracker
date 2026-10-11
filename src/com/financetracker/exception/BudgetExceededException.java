package com.financetracker.exception;

/**
 * Exception thrown when adding an expense pushes spent over its budget limit.
 */
public class BudgetExceededException extends Exception {
    private String category;
    private double overage;

    public BudgetExceededException(String category, double overage) {
        super(category + " budget exceeded by Rs." + String.format("%.2f", overage)); // for e.getErrorMessage()
        this.category = category;
        this.overage = overage;
    }

    public String getCategory() {
        return category;
    }

    public double getOverage() {
        return overage;
    }
}
