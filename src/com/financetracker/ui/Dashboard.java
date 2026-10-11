package com.financetracker.ui;

import com.financetracker.service.FinanceManager;
import com.financetracker.service.BudgetService;
import com.financetracker.ui.components.BarChartPanel;
import com.financetracker.ui.components.BudgetPanel;
import com.financetracker.ui.components.MetricCard;
import com.financetracker.ui.components.PieChartPanel;
import com.financetracker.ui.components.TransactionPanel;
import com.financetracker.ui.dialogs.AddTransactionDialog;
import com.financetracker.ui.dialogs.SetBudgetDialog;
import com.financetracker.model.Transaction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class Dashboard extends JFrame {
    private FinanceManager financeManager;
    private BudgetService budgetService;

    private int currentYear;
    private int currentMonth;

    private JLabel monthLabel;
    private MetricCard incomeCard;
    private MetricCard expenseCard;
    private MetricCard balanceCard;
    private MetricCard txCountCard;

    private TransactionPanel transactionPanel;
    private BudgetPanel budgetPanel;

    private PieChartPanel pieChartPanel;
    private BarChartPanel barChartPanel;
    private JTabbedPane tabbedPane;

    public Dashboard(FinanceManager manager, BudgetService budgetService) {
        this.financeManager = manager;
        this.budgetService = budgetService;

        LocalDate now = LocalDate.now();
        this.currentYear = now.getYear();
        this.currentMonth = now.getMonthValue();

        setTitle("FinTrack Dashboard");
        setSize(1050, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(248, 249, 250));
        setLayout(new BorderLayout());

        initUI();
        refreshDashboard();
    }

    private void initUI() {
        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.setBackground(new Color(248, 249, 250));
        topArea.setBorder(new EmptyBorder(20, 30, 10, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 249, 250));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel titleLabel = new JLabel("FinTrack");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel monthSwitchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        monthSwitchPanel.setBackground(new Color(248, 249, 250));

        JButton prevMonthBtn = new JButton("<");
        prevMonthBtn.setFocusPainted(false);
        prevMonthBtn.setBackground(Color.WHITE);
        prevMonthBtn.addActionListener(e -> changeMonth(-1));

        monthLabel = new JLabel();
        monthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JButton nextMonthBtn = new JButton(">");
        nextMonthBtn.setFocusPainted(false);
        nextMonthBtn.setBackground(Color.WHITE);
        nextMonthBtn.addActionListener(e -> changeMonth(1));

        monthSwitchPanel.add(prevMonthBtn);
        monthSwitchPanel.add(monthLabel);
        monthSwitchPanel.add(nextMonthBtn);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(monthSwitchPanel, BorderLayout.EAST);

        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        metricsPanel.setBackground(new Color(248, 249, 250));
        metricsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        metricsPanel.setBorder(new EmptyBorder(20, 0, 15, 0));

        incomeCard  = new MetricCard("Total Income",   new Color(40, 167, 69));
        expenseCard = new MetricCard("Total Expenses", new Color(220, 53, 69));
        balanceCard = new MetricCard("Net Balance",    new Color(0, 123, 255));
        txCountCard = new MetricCard("Transactions",   Color.BLACK);

        metricsPanel.add(incomeCard);
        metricsPanel.add(expenseCard);
        metricsPanel.add(balanceCard);
        metricsPanel.add(txCountCard);

        topArea.add(headerPanel);
        topArea.add(metricsPanel);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(new Color(248, 249, 250));

        tabbedPane.addTab("  Overview  ", buildOverviewTab());
        tabbedPane.addTab("  Charts  ",   buildChartsTab());

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) refreshCharts();
        });

        add(topArea,     BorderLayout.NORTH);
        add(tabbedPane,  BorderLayout.CENTER);

        updateMonthLabel();
    }

    private JPanel buildOverviewTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(new EmptyBorder(10, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        transactionPanel = new TransactionPanel(
            e -> openAddTransactionDialog(),
            this::handleSearch,
            this::openEditTransactionDialog,
            this::deleteTransaction
        );
        gbc.gridx = 0; gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 0, 0, 10);
        panel.add(transactionPanel, gbc);

        budgetPanel = new BudgetPanel(e -> openSetBudgetDialog());
        gbc.gridx = 1; gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 10, 0, 0);
        panel.add(budgetPanel, gbc);

        return panel;
    }

    private JPanel buildChartsTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(new EmptyBorder(10, 30, 20, 30));

        pieChartPanel = new PieChartPanel();
        barChartPanel = new BarChartPanel();

        panel.add(wrapInCard(pieChartPanel));
        panel.add(wrapInCard(barChartPanel));

        return panel;
    }

    private JPanel wrapInCard(JPanel inner) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        inner.setBackground(Color.WHITE);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private void changeMonth(int delta) {
        currentMonth += delta;
        if (currentMonth > 12) { currentMonth = 1;  currentYear++; }
        else if (currentMonth < 1) { currentMonth = 12; currentYear--; }
        updateMonthLabel();
        refreshDashboard();
    }

    private void updateMonthLabel() {
        String name = LocalDate.of(currentYear, currentMonth, 1)
                               .getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthLabel.setText(name + " " + currentYear);
    }

    private void refreshDashboard() {
        double income   = financeManager.getTotalIncome(currentYear, currentMonth);
        double expenses = financeManager.getTotalExpenses(currentYear, currentMonth);
        double net      = financeManager.getNetBalance(currentYear, currentMonth);
        long txCount    = financeManager.getTransactionsByMonth(currentYear, currentMonth).size();

        incomeCard.setValue(String.format("Rs.%.2f", income), "This month", null);
        expenseCard.setValue(String.format("Rs.%.2f", expenses), "This month", null);
        balanceCard.setValue(String.format("Rs.%.2f", net),
            net >= 0 ? "Positive standing" : "Negative standing",
            net >= 0 ? new Color(40, 167, 69) : new Color(220, 53, 69));
        txCountCard.setValue(String.valueOf(txCount), "This month", null);

        handleSearch();
        budgetPanel.renderBudgets(budgetService.getBudgetsForMonth(currentYear, currentMonth));

        if (tabbedPane != null && tabbedPane.getSelectedIndex() == 1) refreshCharts();
    }

    private void refreshCharts() {
        String label = LocalDate.of(currentYear, currentMonth, 1)
                                .getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                       + " " + currentYear;

        pieChartPanel.updateData(
            financeManager.getTransactionsByMonth(currentYear, currentMonth), label);
        barChartPanel.updateData(financeManager, currentYear, currentMonth);
    }

    private void handleSearch() {
        String q = transactionPanel.getSearchQuery();
        if (q == null || q.trim().isEmpty()) {
            transactionPanel.renderTransactions(financeManager.getTransactionsByMonth(currentYear, currentMonth));
        } else {
            transactionPanel.renderTransactions(financeManager.searchTransactions(q, currentYear, currentMonth));
        }
    }

    private void openAddTransactionDialog() {
        AddTransactionDialog d = new AddTransactionDialog(this, financeManager);
        d.setVisible(true);
        if (d.isTransactionAdded()) {
            com.financetracker.util.FileStorage.saveData(financeManager, budgetService);
            refreshDashboard();
        }
    }

    private void openSetBudgetDialog() {
        SetBudgetDialog d = new SetBudgetDialog(this, budgetService, currentYear, currentMonth);
        d.setVisible(true);
        if (d.isBudgetUpdated()) {
            financeManager.recalculateBudgets();
            com.financetracker.util.FileStorage.saveData(financeManager, budgetService);
            refreshDashboard();
        }
    }

    private void openEditTransactionDialog(Transaction t) {
        com.financetracker.ui.dialogs.EditTransactionDialog d =
            new com.financetracker.ui.dialogs.EditTransactionDialog(this, financeManager, t);
        d.setVisible(true);
        if (d.isTransactionUpdated()) {
            com.financetracker.util.FileStorage.saveData(financeManager, budgetService);
            refreshDashboard();
        }
    }

    private void deleteTransaction(Transaction t) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this transaction?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            financeManager.deleteTransaction(t);
            financeManager.recalculateBudgets();
            com.financetracker.util.FileStorage.saveData(financeManager, budgetService);
            refreshDashboard();
        }
    }
}
