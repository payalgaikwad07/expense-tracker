package com.financetracker.model;

/**
 * Represents a spending limit for a specific category.
 */
public class Budget {
    private String category;
    private double limit;
    private double spent;
    private String setDate;
    private int targetYear;
    private int targetMonth;

    public Budget(int year, int month, String category, double limit) {
        this.targetYear = year;
        this.targetMonth = month;
        this.category = category;
        this.limit = limit;
        this.spent = 0.0;
        this.setDate = java.time.LocalDate.now().toString();
    }

    public String getCategory() {
        return category;
    }

    public double getLimit() {
        return limit;
    }

    public double getSpent() {
        return spent;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    public String getSetDate() {
        return setDate;
    }

    public void setSetDate(String setDate) {
        this.setDate = setDate;
    }

    public int getTargetYear() {
        return targetYear;
    }

    public int getTargetMonth() {
        return targetMonth;
    }

    public void addSpending(double amount) {
        if (amount > 0) {
            this.spent += amount;
        }
    }

    public double getRemainingBudget() {
        return limit - spent;
    }

    public boolean isExceeded() {
        return spent > limit;
    }

    public double getUsedPercentage() {
        if (limit == 0) return 0.0;
        return (spent / limit) * 100;
    }
}
