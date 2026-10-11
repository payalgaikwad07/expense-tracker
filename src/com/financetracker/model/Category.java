package com.financetracker.model;

import java.util.ArrayList;
import java.util.List;

/* Category enum to restrict options and provide type safety.
 */
public enum Category {
    // Expenses
    FOOD("Food", false),
    RENT("Rent", false),
    TRAVEL("Travel", false),
    SHOPPING("Shopping", false),
    UTILITIES("Utilities", false),
    ENTERTAINMENT("Entertainment", false),
    OTHER("Other", false),
    
    // Income
    INCOME("Income", true),
    SALARY("Salary", true),
    BUSINESS("Business", true),
    INVESTMENT("Investment", true),
    RENTAL("Rental", true),
    GIFTS("Gifts", true),
    OTHER_INCOME("Other Income", true);

    private final String displayName;
    private final boolean isIncome;

    Category(String displayName, boolean isIncome) {
        this.displayName = displayName;
        this.isIncome = isIncome;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public static Category fromString(String input) { // converts string to enum
        for (Category category : Category.values()) {
            if (category.name().equalsIgnoreCase(input) || category.getDisplayName().equalsIgnoreCase(input)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid category: " + input);
    }
    
    public static List<Category> getIncomeCategories() {
        List<Category> list = new ArrayList<>();
        for (Category c : values()) {
            if (c.isIncome()) list.add(c);
        }
        return list;
    }
    
    public static List<Category> getExpenseCategories() {
        List<Category> list = new ArrayList<>();
        for (Category c : values()) {
            if (!c.isIncome()) list.add(c);
        }
        return list;
    }
}
