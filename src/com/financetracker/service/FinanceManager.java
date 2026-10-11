package com.financetracker.service;

import com.financetracker.exception.BudgetExceededException;
import com.financetracker.model.Expense;
import com.financetracker.model.Income;
import com.financetracker.model.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main service class, brain of the app.
 */
public class FinanceManager {
    private List<Transaction> transactions;
    private BudgetService budgetService;

    public FinanceManager(BudgetService budgetService) {
        this.transactions = new ArrayList<>();
        this.budgetService = budgetService;
    }

    public void addTransaction(Transaction t) throws BudgetExceededException {
        transactions.add(0, t); // add at top for recent
        // If it's an Expense, verify against BudgetService
        if (t instanceof Expense) {
            Expense expense = (Expense) t;
            budgetService.checkAndUpdate(expense);
        }
    }

    public void loadTransactionWithoutBudgetCheck(Transaction t) {
        transactions.add(0, t);
        // We strictly do NOT pass this down to budgetService because the exact spent number 
        // is now stored and loaded directly via budgets.csv! Re-calculating throws timestamps off!
    }

    public void deleteTransaction(Transaction t) {
        transactions.remove(t);
    }

    public void recalculateBudgets() {
        for (com.financetracker.model.Budget b : budgetService.getAllBudgets()) {
            b.setSpent(0.0);
        }
        for (Transaction t : transactions) {
            if (t instanceof Expense) {
                budgetService.checkAndUpdateSilently((Expense) t);
            }
        }
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }
    
    public List<Transaction> getTransactionsByMonth(int year, int month) {
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : transactions) {
            try {
                LocalDate date = LocalDate.parse(t.getDate());
                if (date.getYear() == year && date.getMonthValue() == month) {
                    filtered.add(t);
                }
            } catch (DateTimeParseException e) {
                // Ignore invalid dates dynamically
            }
        }
        return filtered;
    }

    public List<Transaction> searchTransactions(String query, int year, int month) {
        List<Transaction> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();
        for (Transaction t : getTransactionsByMonth(year, month)) {
            // Match note/description (primary search target - what user typed as label)
            boolean matchesNote = t.getNote() != null && t.getNote().toLowerCase().contains(lowerQuery);
            // Match category only when query is 3+ chars to avoid false single-letter hits
            // e.g. typing "foo" matches FOOD, but "d" alone should NOT match FOOD
            String categoryDisplay = t.getCategory().replace("_", " ").toLowerCase();
            boolean matchesCategory = lowerQuery.length() >= 3 && categoryDisplay.contains(lowerQuery);
            // Match type: "income" or "expense"
            boolean matchesType = t.getType().toLowerCase().contains(lowerQuery);
            if (matchesNote || matchesCategory || matchesType) {
                filtered.add(t);
            }
        }
        return filtered;
    }


    public double getTotalIncome(int year, int month) {
        double sum = 0;
        for (Transaction t : getTransactionsByMonth(year, month)) {
            if (t instanceof Income) {
                sum += t.getImpact();
            }
        }
        return sum;
    }

    public double getTotalExpenses(int year, int month) {
        double sum = 0;
        for (Transaction t : getTransactionsByMonth(year, month)) {
            if (t instanceof Expense) {
                sum += Math.abs(t.getImpact());
            }
        }
        return sum;
    }

    public double getNetBalance(int year, int month) {
        return getTotalIncome(year, month) - getTotalExpenses(year, month);
    }
}
