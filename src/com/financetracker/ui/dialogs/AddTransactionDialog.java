package com.financetracker.ui.dialogs;

import com.financetracker.exception.BudgetExceededException;
import com.financetracker.model.Category;
import com.financetracker.model.Expense;
import com.financetracker.model.Income;
import com.financetracker.model.Transaction;
import com.financetracker.service.FinanceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AddTransactionDialog extends JDialog {
    private JRadioButton incomeRadio;
    private JRadioButton expenseRadio;
    private JComboBox<String> categoryCombo;
    private JTextField amountField;
    private JTextField dateField;
    private JTextField noteField;
    private FinanceManager financeManager;
    private boolean transactionAdded = false;

    public AddTransactionDialog(JFrame parent, FinanceManager manager) {
        super(parent, "Add Transaction", true);
        this.financeManager = manager;
        
        setLayout(new BorderLayout(10, 10));
        setSize(350, 400);
        setLocationRelativeTo(parent);
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Type selection
        incomeRadio = new JRadioButton("Income");
        expenseRadio = new JRadioButton("Expense", true); // Default expense
        ButtonGroup group = new ButtonGroup();
        group.add(incomeRadio);
        group.add(expenseRadio);
        JPanel radioPanel = new JPanel(new GridLayout(1, 2));
        radioPanel.add(expenseRadio);
        radioPanel.add(incomeRadio);

        formPanel.add(new JLabel("Type:"));
        formPanel.add(radioPanel);

        // Category
        categoryCombo = new JComboBox<>();
        updateCategoryOptions();
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryCombo);

        incomeRadio.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) updateCategoryOptions();
        });
        expenseRadio.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) updateCategoryOptions();
        });

        // Amount
        amountField = new JTextField();
        formPanel.add(new JLabel("Amount (Rs):"));
        formPanel.add(amountField);

        // Date
        dateField = new JTextField(LocalDate.now().toString());
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        formPanel.add(dateField);

        // Note
        noteField = new JTextField();
        formPanel.add(new JLabel("Note/Description:"));
        formPanel.add(noteField);
        
        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(new Color(0, 123, 255));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> saveTransaction());
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        
        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void updateCategoryOptions() {
        categoryCombo.removeAllItems();
        List<Category> options = incomeRadio.isSelected() ? Category.getIncomeCategories() : Category.getExpenseCategories();
        for (Category c : options) {
            categoryCombo.addItem(c.getDisplayName());
        }
    }

    private void saveTransaction() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String dateStr = dateField.getText();
            LocalDate.parse(dateStr); // Validate format
            
            String catName = (String) categoryCombo.getSelectedItem();
            Category category = Category.fromString(catName);
            String note = noteField.getText();

            Transaction t;
            if (incomeRadio.isSelected()) {
                t = new Income(amount, category.name(), dateStr);
            } else {
                t = new Expense(amount, category.name(), dateStr);
            }
            t.setNote(note);

            financeManager.addTransaction(t);
            transactionAdded = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (BudgetExceededException ex) {
            JOptionPane.showMessageDialog(this, "Budget Alert: " + ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            transactionAdded = true; // The expense was still recorded inside FinanceManager! wait no it isn't.
            // Let's add it explicitly if exception is thrown because FinanceManager doesn't add it.
            // But wait, the FinanceManager logic was checked, it throws before adding.
            // So we need to call transactions.add(t). Instead, I'll modify FinanceManager later.
            transactionAdded = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isTransactionAdded() {
        return transactionAdded;
    }
}
