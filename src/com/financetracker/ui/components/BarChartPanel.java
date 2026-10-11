package com.financetracker.ui.components;

import com.financetracker.service.FinanceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Custom bar chart panel showing income vs expenses for the last 6 months.
 * Pure Swing - no external library needed.
 */
public class BarChartPanel extends JPanel {

    private static final Color INCOME_COLOR  = new Color(40, 167, 69);   // green
    private static final Color EXPENSE_COLOR = new Color(220, 53, 69);   // red
    private static final Color GRID_COLOR    = new Color(235, 235, 235);
    private static final int   BAR_GAP       = 8;  // gap between income & expense bar in same month

    private List<String> monthLabels  = new ArrayList<>();
    private List<Double> incomeValues = new ArrayList<>();
    private List<Double> expenseValues= new ArrayList<>();

    public BarChartPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 400));
    }

    /**
     * Pass the currently viewed year+month so we show the 6 months ending there.
     */
    public void updateData(FinanceManager fm, int currentYear, int currentMonth) {
        monthLabels.clear();
        incomeValues.clear();
        expenseValues.clear();

        // Build last 6 months ending at currentYear/currentMonth
        LocalDate cursor = LocalDate.of(currentYear, currentMonth, 1);
        for (int i = 5; i >= 0; i--) {
            LocalDate m = cursor.minusMonths(i);
            int y = m.getYear();
            int mo = m.getMonthValue();

            String label = m.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                           + " " + String.valueOf(y).substring(2); // e.g. "Apr 26"
            monthLabels.add(label);
            incomeValues.add(fm.getTotalIncome(y, mo));
            expenseValues.add(fm.getTotalExpenses(y, mo));
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        int paddingLeft   = 65;
        int paddingRight  = 20;
        int paddingTop    = 50;
        int paddingBottom = 55;

        int chartW = w - paddingLeft - paddingRight;
        int chartH = h - paddingTop - paddingBottom;

        // Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g2.setColor(new Color(50, 50, 50));
        String title = "Income vs Expenses — Last 6 Months";
        FontMetrics tfm = g2.getFontMetrics();
        g2.drawString(title, (w - tfm.stringWidth(title)) / 2, 30);

        // Legend
        int legendY = 46;
        drawLegendDot(g2, paddingLeft, legendY, INCOME_COLOR, "Income");
        drawLegendDot(g2, paddingLeft + 80, legendY, EXPENSE_COLOR, "Expenses");

        // Find max value for Y scale
        double maxVal = 0;
        for (int i = 0; i < monthLabels.size(); i++) {
            maxVal = Math.max(maxVal, incomeValues.get(i));
            maxVal = Math.max(maxVal, expenseValues.get(i));
        }
        if (maxVal == 0) maxVal = 1000; // default if no data

        // Round maxVal up to a nice number
        maxVal = niceMax(maxVal);

        // Draw horizontal grid lines + Y axis labels
        int gridLines = 5;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        for (int i = 0; i <= gridLines; i++) {
            int y = paddingTop + chartH - (int)((double) i / gridLines * chartH);
            double val = (double) i / gridLines * maxVal;

            // Grid line
            g2.setColor(GRID_COLOR);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(paddingLeft, y, paddingLeft + chartW, y);

            // Y label
            g2.setColor(Color.GRAY);
            String label = formatAmount(val);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, paddingLeft - fm.stringWidth(label) - 5, y + 4);
        }

        // Draw bars
        int n = monthLabels.size();
        int groupWidth = chartW / n;
        int barWidth = Math.max(8, (groupWidth - BAR_GAP * 3) / 2);

        for (int i = 0; i < n; i++) {
            int groupX = paddingLeft + i * groupWidth;
            int centerX = groupX + groupWidth / 2;

            // Income bar
            double income = incomeValues.get(i);
            int incomeH = (int)(income / maxVal * chartH);
            int incomeX = centerX - barWidth - BAR_GAP / 2;
            int incomeY = paddingTop + chartH - incomeH;
            drawBar(g2, incomeX, incomeY, barWidth, incomeH, INCOME_COLOR);

            // Expense bar
            double expense = expenseValues.get(i);
            int expenseH = (int)(expense / maxVal * chartH);
            int expenseX = centerX + BAR_GAP / 2;
            int expenseY = paddingTop + chartH - expenseH;
            drawBar(g2, expenseX, expenseY, barWidth, expenseH, EXPENSE_COLOR);

            // Value labels on top of bars (only if bar is tall enough)
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2.setColor(new Color(80, 80, 80));
            if (incomeH > 15) {
                String iv = formatAmount(income);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(iv, incomeX + (barWidth - fm.stringWidth(iv)) / 2, incomeY - 3);
            }
            if (expenseH > 15) {
                String ev = formatAmount(expense);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ev, expenseX + (barWidth - fm.stringWidth(ev)) / 2, expenseY - 3);
            }

            // Month label below
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(new Color(80, 80, 80));
            String ml = monthLabels.get(i);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(ml, centerX - fm.stringWidth(ml) / 2, paddingTop + chartH + 18);
        }

        // X axis line
        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(paddingLeft, paddingTop + chartH, paddingLeft + chartW, paddingTop + chartH);

        g2.dispose();
    }

    private void drawBar(Graphics2D g2, int x, int y, int w, int h, Color color) {
        if (h <= 0) return;
        g2.setColor(color);
        // Rounded top corners
        int arc = Math.min(6, w / 2);
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, arc, arc));
        // Make bottom square by overdrawing bottom half
        if (h > arc) {
            g2.fillRect(x, y + h / 2, w, h - h / 2);
        }
    }

    private void drawLegendDot(Graphics2D g2, int x, int y, Color color, String label) {
        g2.setColor(color);
        g2.fillRoundRect(x, y - 9, 14, 10, 4, 4);
        g2.setColor(new Color(60, 60, 60));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.drawString(label, x + 18, y);
    }

    private double niceMax(double rawMax) {
        double magnitude = Math.pow(10, Math.floor(Math.log10(rawMax)));
        double normalized = rawMax / magnitude;
        double nice;
        if (normalized <= 1) nice = 1;
        else if (normalized <= 2) nice = 2;
        else if (normalized <= 5) nice = 5;
        else nice = 10;
        return nice * magnitude;
    }

    private String formatAmount(double val) {
        if (val >= 100000) return String.format("%.1fL", val / 100000);
        if (val >= 1000)   return String.format("%.1fK", val / 1000);
        return String.format("%.0f", val);
    }
}
