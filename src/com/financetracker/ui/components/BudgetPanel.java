package com.financetracker.ui.components;

import com.financetracker.model.Budget;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class BudgetPanel extends JPanel {
    private JPanel listPanel;

    public BudgetPanel(ActionListener setBudgetAction) {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Budget status");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JButton setBudgetBtn = new JButton("Set up budgets");
        setBudgetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        setBudgetBtn.setBackground(Color.WHITE);
        setBudgetBtn.setFocusPainted(false);
        setBudgetBtn.addActionListener(setBudgetAction);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(setBudgetBtn, BorderLayout.EAST);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(listPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
    }
    
    public void renderBudgets(List<Budget> budgets) {
        listPanel.removeAll();
        for (Budget b : budgets) {
            JPanel row = new JPanel(new BorderLayout(0, 5));
            row.setBackground(Color.WHITE);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            row.setBorder(new EmptyBorder(5, 0, 10, 0));

            JPanel textRow = new JPanel(new BorderLayout());
            textRow.setBackground(Color.WHITE);

            JLabel catLabel = new JLabel(b.getCategory());
            catLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

            JLabel infoLabel = new JLabel();
            infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            infoLabel.setForeground(new Color(100, 100, 100));
            
            if (b.isExceeded()) {
                infoLabel.setText(String.format("Over by: Rs.%.0f | Limit: Rs.%.0f (Set on: %s)", Math.abs(b.getRemainingBudget()), b.getLimit(), b.getSetDate()));
                infoLabel.setForeground(new Color(220, 53, 69));
            } else {
                infoLabel.setText(String.format("Rs.%.0f spent | Rs.%.0f left (Set on: %s)", b.getSpent(), b.getRemainingBudget(), b.getSetDate()));
            }

            textRow.add(catLabel, BorderLayout.WEST);
            textRow.add(infoLabel, BorderLayout.EAST);

           int max = (int) b.getLimit();

            if (max <= 0) {
                max = 1; // prevent crash
            }

            JProgressBar progress = new JProgressBar(0, max);
            int value = (int) Math.min(b.getSpent(), max);
            progress.setValue(value);
            progress.setStringPainted(false);
            progress.setPreferredSize(new Dimension(100, 8)); // thin progress bar
            
            if (b.isExceeded()) {
                progress.setForeground(new Color(220, 53, 69)); // Red
            } else if (b.getUsedPercentage() > 80) {
                progress.setForeground(new Color(255, 193, 7)); // Yellow
            } else {
                progress.setForeground(new Color(40, 167, 69)); // Green
            }

            row.add(textRow, BorderLayout.NORTH);
            row.add(progress, BorderLayout.CENTER);
            
            listPanel.add(row);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}
