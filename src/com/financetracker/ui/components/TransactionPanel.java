package com.financetracker.ui.components;

import com.financetracker.model.Expense;
import com.financetracker.model.Income;
import com.financetracker.model.Transaction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;

public class TransactionPanel extends JPanel {
    private static final String PLACEHOLDER = "Search history...";
    private JPanel listPanel;
    private JTextField searchField;
    private Consumer<Transaction> onEdit;
    private Consumer<Transaction> onDelete;

    public TransactionPanel(ActionListener addAction, Runnable onSearch, Consumer<Transaction> onEdit, Consumer<Transaction> onDelete) {
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Recent transactions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        searchField = new JTextField(15);
        searchField.setText(PLACEHOLDER);
        searchField.setForeground(Color.GRAY);

    searchField.addFocusListener(new java.awt.event.FocusAdapter() {
        public void focusGained(java.awt.event.FocusEvent e) {
            if (searchField.getText().equals(PLACEHOLDER)) {
                searchField.setText("");
                searchField.setForeground(Color.BLACK);
            }
        }

        public void focusLost(java.awt.event.FocusEvent e) {
            if (searchField.getText().isEmpty()) {
                searchField.setText(PLACEHOLDER);
                searchField.setForeground(Color.GRAY);
            }
        }
    });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onSearch.run(); }
            public void removeUpdate(DocumentEvent e) { onSearch.run(); }
            public void changedUpdate(DocumentEvent e) { onSearch.run(); }
        });

        JButton addBtn = new JButton("+ Add");
        addBtn.setBackground(new Color(0, 123, 255));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(addAction);

        JPanel headerControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerControls.setBackground(Color.WHITE);
        headerControls.add(searchField);
        headerControls.add(addBtn);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(headerControls, BorderLayout.EAST);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
        public String getSearchQuery() {
        String text = searchField.getText();
        if (text.equals(PLACEHOLDER)) {
            return ""; // treat placeholder as empty
        }
        return text;
    }

    public void renderTransactions(List<Transaction> transactions) {
        listPanel.removeAll();
        if (transactions.isEmpty()) {
            JLabel empty = new JLabel("No transactions found.");
            empty.setForeground(Color.GRAY);
            listPanel.add(empty);
        } else {
            for (Transaction t : transactions) {
                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(Color.WHITE);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));

                // Left: Icon placeholder + Info
                JPanel infoPanel = new JPanel(new GridLayout(2, 1));
                infoPanel.setBackground(Color.WHITE);
                infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                
                String title = t.getNote() != null && !t.getNote().isEmpty() ? t.getNote() : t.getCategory();
                JLabel noteLbl = new JLabel(title);
                noteLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                JLabel catLbl = new JLabel(t.getCategory());
                catLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                catLbl.setForeground(Color.GRAY);
                
                infoPanel.add(noteLbl);
                infoPanel.add(catLbl);

                // Right: Date & Amount
                JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
                rightPanel.setBackground(Color.WHITE);
                JLabel dateLbl = new JLabel(t.getDate());
                dateLbl.setForeground(Color.GRAY);
                
                JLabel amtLbl = new JLabel();
                amtLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                if (t instanceof Income) {
                    amtLbl.setText(String.format("+Rs.%.2f", t.getAmount()));
                    amtLbl.setForeground(new Color(40, 167, 69));
                } else {
                    amtLbl.setText(String.format("-Rs.%.2f", t.getAmount()));
                    amtLbl.setForeground(new Color(220, 53, 69));
                }
                
                JButton editBtn = new JButton("Edit");
                editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                editBtn.setMargin(new Insets(2, 5, 2, 5));
                editBtn.addActionListener(e -> onEdit.accept(t));

                JButton delBtn = new JButton("X");
                delBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
                delBtn.setForeground(Color.RED);
                delBtn.setMargin(new Insets(2, 5, 2, 5));
                delBtn.addActionListener(e -> onDelete.accept(t));

                rightPanel.add(dateLbl);
                rightPanel.add(amtLbl);
                rightPanel.add(editBtn);
                rightPanel.add(delBtn);

                row.add(infoPanel, BorderLayout.CENTER);
                row.add(rightPanel, BorderLayout.EAST);
                listPanel.add(row);
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}
