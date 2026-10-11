package com.financetracker.ui.dialogs;

import com.financetracker.model.Category;
import com.financetracker.service.BudgetService;

import javax.swing.*;
import java.awt.*;

public class SetBudgetDialog extends JDialog {
    private JComboBox<String> categoryCombo;
    private JTextField limitField;
    private BudgetService budgetService;
    private boolean budgetUpdated = false;
    private int targetYear;
    private int targetMonth;

    public SetBudgetDialog(JFrame parent, BudgetService budgetService, int year, int month) {
        super(parent, "Set Budget", true);
        this.budgetService = budgetService;
        this.targetYear = year;
        this.targetMonth = month;

        setLayout(new BorderLayout(10, 10));
        setSize(300, 200);
        setLocationRelativeTo(parent);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        categoryCombo = new JComboBox<>();
        for (Category c : Category.getExpenseCategories()) {
            categoryCombo.addItem(c.getDisplayName());
        }
        
        formPanel.add(new JLabel("Expense Category:"));
        formPanel.add(categoryCombo);

        limitField = new JTextField();
        formPanel.add(new JLabel("Budget Limit (Rs):"));
        formPanel.add(limitField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(new Color(0, 123, 255));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> saveBudget());
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        
        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void saveBudget() {
    try {
        double limit = Double.parseDouble(limitField.getText());

        if (limit <= 0) {
            JOptionPane.showMessageDialog(
                this,
                "Budget must be greater than 0.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE
            );
            return; // stop execution
        }

        String catName = (String) categoryCombo.getSelectedItem();
        Category cat = Category.fromString(catName);
        budgetService.setBudget(targetYear, targetMonth, cat.name(), limit);
        budgetUpdated = true;
        dispose();

    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(
            this,
            "Invalid limit amount.",
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}

    public boolean isBudgetUpdated() {
        return budgetUpdated;
    }
}
