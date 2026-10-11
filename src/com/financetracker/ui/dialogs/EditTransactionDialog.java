package com.financetracker.ui.dialogs;

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

public class EditTransactionDialog extends JDialog {
    private JRadioButton incomeRadio;
    private JRadioButton expenseRadio;
    private JComboBox<String> categoryCombo;
    private JTextField amountField;
    private JTextField dateField;
    private JTextField noteField;
    
    private FinanceManager financeManager;
    private Transaction transaction;
    private boolean transactionUpdated = false;

    public EditTransactionDialog(JFrame parent, FinanceManager manager, Transaction transaction) {
        super(parent, "Edit Transaction", true);
        this.financeManager = manager;
        this.transaction = transaction;
        
        setLayout(new BorderLayout(10, 10));
        setSize(350, 400);
        setLocationRelativeTo(parent);
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Type selection
        incomeRadio = new JRadioButton("Income");
        expenseRadio = new JRadioButton("Expense");
        ButtonGroup group = new ButtonGroup();
        group.add(incomeRadio);
        group.add(expenseRadio);
        JPanel radioPanel = new JPanel(new GridLayout(1, 2));
        radioPanel.add(expenseRadio);
        radioPanel.add(incomeRadio);

        if (transaction instanceof Income) {
            incomeRadio.setSelected(true);
        } else {
            expenseRadio.setSelected(true);
        }

        formPanel.add(new JLabel("Type:"));
        formPanel.add(radioPanel);

        // Category
        categoryCombo = new JComboBox<>();
        updateCategoryOptions();
        categoryCombo.setSelectedItem(Category.valueOf(transaction.getCategory()).getDisplayName());
        
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryCombo);

        incomeRadio.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) updateCategoryOptions();
        });
        expenseRadio.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) updateCategoryOptions();
        });

        // Amount
        amountField = new JTextField(String.valueOf(transaction.getAmount()));
        formPanel.add(new JLabel("Amount (Rs):"));
        formPanel.add(amountField);

        // Date
        dateField = new JTextField(transaction.getDate());
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        formPanel.add(dateField);

        // Note
        noteField = new JTextField(transaction.getNote() != null ? transaction.getNote() : "");
        formPanel.add(new JLabel("Note/Description:"));
        formPanel.add(noteField);
        
        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Update");
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> updateTransaction());
        
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

    private void updateTransaction() {
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

            // We must replace the object in FinanceManager or just update it if type is same.
            // But if type swaps Income <-> Expense, we must replace it.
            boolean typeChanged = (incomeRadio.isSelected() && transaction instanceof Expense) ||
                                  (expenseRadio.isSelected() && transaction instanceof Income);

            if (typeChanged) {
                financeManager.deleteTransaction(transaction);
                Transaction t;
                if (incomeRadio.isSelected()) {
                    t = new Income(amount, category.name(), dateStr);
                } else {
                    t = new Expense(amount, category.name(), dateStr);
                }
                t.setNote(note);
                financeManager.loadTransactionWithoutBudgetCheck(t); // add without showing budget exception directly
            } else {
                transaction.setAmount(amount);
                transaction.setCategory(category.name());
                transaction.setDate(dateStr);
                transaction.setNote(note);
            }

            // Immediately trigger global budget recalculation
            financeManager.recalculateBudgets();
            
            transactionUpdated = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isTransactionUpdated() {
        return transactionUpdated;
    }
}
