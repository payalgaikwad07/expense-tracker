package com.financetracker;

import com.financetracker.service.BudgetService;
import com.financetracker.service.FinanceManager;
import com.financetracker.ui.Dashboard;
import com.financetracker.util.FileStorage;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        BudgetService budgetService = new BudgetService();
        FinanceManager financeManager = new FinanceManager(budgetService);

        // Load persisted data
        FileStorage.loadData(financeManager, budgetService);

        SwingUtilities.invokeLater(() -> {
            Dashboard dashboard = new Dashboard(financeManager, budgetService);
            dashboard.setVisible(true);
        });
    }
}
