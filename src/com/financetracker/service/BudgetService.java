package com.financetracker.service;

import com.financetracker.exception.BudgetExceededException;
import com.financetracker.model.Budget;
import com.financetracker.model.Expense;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all budget related logic.
 */
public class BudgetService {
    private List<Budget> budgets;

    public BudgetService() {
        this.budgets = new ArrayList<>();
    }

    public void setBudget(int year, int month, String category, double limit) {
        Budget existing = getBudget(year, month, category);
        if (existing != null) {
            budgets.remove(existing);
        }
        budgets.add(new Budget(year, month, category, limit));
    }

    public List<Budget> getAllBudgets() {
        return new ArrayList<>(budgets);
    }

    public List<Budget> getBudgetsForMonth(int year, int month) {
        List<Budget> filtered = new ArrayList<>();
        for (Budget b : budgets) {
            if (b.getTargetYear() == year && b.getTargetMonth() == month) {
                filtered.add(b);
            }
        }
        return filtered;
    }

    public Budget getBudget(int year, int month, String category) {
        for (Budget b : budgets) {
            if (b.getTargetYear() == year && b.getTargetMonth() == month && b.getCategory().equalsIgnoreCase(category)) {
                return b;
            }
        }
        return null; // No budget set
    }

    public void checkAndUpdate(Expense expense) throws BudgetExceededException {
        java.time.LocalDate eDate = java.time.LocalDate.parse(expense.getDate());
        Budget budget = getBudget(eDate.getYear(), eDate.getMonthValue(), expense.getCategory());
        if (budget == null) {
            return; // No budget set, so no check needed
        }
        
        java.time.LocalDate bDate = java.time.LocalDate.parse(budget.getSetDate());
        
        if (eDate.isBefore(bDate)) {
            return; // Ignore past transactions that happened before this budget was created
        }
        
        budget.addSpending(expense.getAmount());
        
        if (budget.isExceeded()) {
            double overage = budget.getSpent() - budget.getLimit();
            throw new BudgetExceededException(budget.getCategory(), overage);
        }
    }

    public void checkAndUpdateSilently(Expense expense) {
        java.time.LocalDate eDate = java.time.LocalDate.parse(expense.getDate());
        Budget budget = getBudget(eDate.getYear(), eDate.getMonthValue(), expense.getCategory());
        if (budget == null) return;
        
        java.time.LocalDate bDate = java.time.LocalDate.parse(budget.getSetDate());
        
        if (!eDate.isBefore(bDate)) {
            budget.addSpending(expense.getAmount());
        }
    }
}
