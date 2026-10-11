package com.financetracker.util;

import com.financetracker.model.Budget;
import com.financetracker.model.Expense;
import com.financetracker.model.Income;
import com.financetracker.model.Transaction;
import com.financetracker.service.BudgetService;
import com.financetracker.service.FinanceManager;

import java.io.*;
import java.util.List;

public class FileStorage {
    private static final String DATA_DIR = "data";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.csv";
    private static final String BUDGETS_FILE = DATA_DIR + "/budgets.csv";

    private static void ensureDataDir() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static void saveData(FinanceManager financeManager, BudgetService budgetService) {
        ensureDataDir();
        saveBudgets(budgetService.getAllBudgets());
        saveTransactions(financeManager.getAllTransactions());
    }

    private static void saveBudgets(List<Budget> budgets) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BUDGETS_FILE))) {
            for (Budget b : budgets) {
                // Save year, month, category, limit, and setDate directly
                writer.printf("%d,%d,%s,%.2f,%s\n", b.getTargetYear(), b.getTargetMonth(), b.getCategory(), b.getLimit(), b.getSetDate());
            }
        } catch (IOException e) {
            System.err.println("Error saving budgets: " + e.getMessage());
        }
    }

    private static void saveTransactions(List<Transaction> transactions) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TRANSACTIONS_FILE))) {
            for (Transaction t : transactions) {
                // Type,Amount,Category,Date,Note
                // Reverse iteration is saved from FinanceManager so it's most recent first.
                writer.printf("%s,%.2f,%s,%s,%s\n", 
                    t.getType(), t.getAmount(), t.getCategory(), t.getDate(), 
                    t.getNote() != null ? t.getNote().replace(",", ";") : ""); // avoid csv breaks
            }
        } catch (IOException e) {
            System.err.println("Error saving transactions: " + e.getMessage());
        }
    }

    public static void loadData(FinanceManager financeManager, BudgetService budgetService) {
        ensureDataDir();
        
        // Load budgets first
        File bFile = new File(BUDGETS_FILE);
        if (bFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(bFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        try {
                            int year = Integer.parseInt(parts[0]);
                            int month = Integer.parseInt(parts[1]);
                            String category = parts[2];
                            double limit = Double.parseDouble(parts[3]);
                            budgetService.setBudget(year, month, category, limit);
                            
                            Budget b = budgetService.getBudget(year, month, category);
                            if (b != null) {
                                b.setSetDate(parts[4]);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing budget line: " + line);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading budgets: " + e.getMessage());
            }
        }

        // Load transactions (loading these will automatically populate budget spent amounts)
        File tFile = new File(TRANSACTIONS_FILE);
        if (tFile.exists()) {
            // Because they were saved newest first, but FinanceManager adds to index 0,
            // reading them sequentially and adding them would reverse the order!
            // Wait, we need to add them to index 0. If we read newest first and add to 0, 
            // the newest becomes the oldest. Let's read to a list first, then reverse it, 
            // or we use loadTransactionWithoutBudgetCheck which appends to end.
            try (BufferedReader br = new BufferedReader(new FileReader(tFile))) {
                String line;
                java.util.List<Transaction> loaded = new java.util.ArrayList<>();
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",", 5); // limit to 5 so note is always parts[4] even if it had commas
                    if (parts.length >= 4) {
                        String type = parts[0];
                        double amount = Double.parseDouble(parts[1]);
                        String catStr = parts[2];
                        String dateStr = parts[3];
                        String note = parts.length > 4 ? parts[4].replace(";", ",") : ""; // restore commas

                        Transaction t;
                        if ("Income".equalsIgnoreCase(type)) {
                            t = new Income(amount, catStr, dateStr);
                        } else {
                            t = new Expense(amount, catStr, dateStr);
                        }
                        t.setNote(note);
                        loaded.add(t);
                    }
                }
                
                // Add them in reverse order so that index 0 behavior restores original order
                for (int i = loaded.size() - 1; i >= 0; i--) {
                    financeManager.loadTransactionWithoutBudgetCheck(loaded.get(i));
                }
                
                // Now chronologically rebuild all budgets based on the updated structural logic!
                financeManager.recalculateBudgets();
            } catch (Exception e) {
                System.err.println("Error loading transactions: " + e.getMessage());
            }
        }
    }
}
