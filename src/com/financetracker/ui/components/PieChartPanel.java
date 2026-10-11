package com.financetracker.ui.components;

import com.financetracker.model.Expense;
import com.financetracker.model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;

/**
 * Custom pie chart panel showing expense breakdown by category for a given month.
 * Pure Swing - no external library needed.
 */
public class PieChartPanel extends JPanel {

    // Palette matching dashboard green/red/blue style
    private static final Color[] COLORS = {
        new Color(0, 123, 255),   // blue
        new Color(220, 53, 69),   // red
        new Color(40, 167, 69),   // green
        new Color(255, 193, 7),   // yellow
        new Color(111, 66, 193),  // purple
        new Color(253, 126, 20),  // orange
        new Color(32, 201, 151),  // teal
        new Color(232, 62, 140),  // pink
        new Color(52, 58, 64),    // dark gray
        new Color(23, 162, 184),  // cyan
    };

    private Map<String, Double> categoryTotals = new LinkedHashMap<>();
    private double grandTotal = 0;
    private String monthLabel = "";

    public PieChartPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 400));
    }

    /**
     * Call this whenever the month changes.
     */
    public void updateData(List<Transaction> transactions, String monthLabel) {
        this.monthLabel = monthLabel;
        categoryTotals.clear();
        grandTotal = 0;

        for (Transaction t : transactions) {
            if (t instanceof Expense) {
                String cat = t.getCategory();
                categoryTotals.merge(cat, t.getAmount(), Double::sum);
                grandTotal += t.getAmount();
            }
        }

        // Sort by value descending for cleaner pie
        List<Map.Entry<String, Double>> entries = new ArrayList<>(categoryTotals.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        categoryTotals.clear();
        for (Map.Entry<String, Double> e : entries) {
            categoryTotals.put(e.getKey(), e.getValue());
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

        if (categoryTotals.isEmpty() || grandTotal == 0) {
            drawEmptyState(g2, w, h);
            g2.dispose();
            return;
        }

        // Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g2.setColor(new Color(50, 50, 50));
        String title = "Expense Breakdown — " + monthLabel;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, 30);

        // Layout: pie on left, legend on right
        int legendWidth = 160;
        int pieAreaWidth = w - legendWidth - 20;
        int pieSize = Math.min(pieAreaWidth - 40, h - 80);
        if (pieSize < 50) pieSize = 50;

        int pieX = (pieAreaWidth - pieSize) / 2;
        int pieY = (h - pieSize) / 2 + 10;

        // Draw pie slices
        double startAngle = 90; // Start from top
        int colorIndex = 0;
        List<String> keys = new ArrayList<>(categoryTotals.keySet());

        for (String cat : keys) {
            double value = categoryTotals.get(cat);
            double sweepAngle = (value / grandTotal) * 360.0;

            g2.setColor(COLORS[colorIndex % COLORS.length]);
            Arc2D.Double arc = new Arc2D.Double(pieX, pieY, pieSize, pieSize,
                    startAngle, sweepAngle, Arc2D.PIE);
            g2.fill(arc);

            // White border between slices
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(arc);

            startAngle += sweepAngle;
            colorIndex++;
        }

        // Draw legend on the right
        int legendX = pieAreaWidth + 10;
        int legendStartY = (h - (keys.size() * 28)) / 2 + 10;
        colorIndex = 0;

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        for (String cat : keys) {
            double value = categoryTotals.get(cat);
            double pct = (value / grandTotal) * 100;

            int ly = legendStartY + colorIndex * 28;

            // Color dot
            g2.setColor(COLORS[colorIndex % COLORS.length]);
            g2.fill(new Ellipse2D.Double(legendX, ly, 12, 12));

            // Category name
            g2.setColor(new Color(50, 50, 50));
            String catDisplay = formatCategory(cat);
            g2.drawString(catDisplay, legendX + 18, ly + 11);

            // Percentage + amount
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(Color.GRAY);
            g2.drawString(String.format("Rs.%.0f (%.1f%%)", value, pct), legendX + 18, ly + 24);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            colorIndex++;
        }

        // Total in center of pie
        g2.setColor(new Color(50, 50, 50));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        String totalStr = String.format("Rs.%.0f", grandTotal);
        FontMetrics tfm = g2.getFontMetrics();
        int cx = pieX + pieSize / 2;
        int cy = pieY + pieSize / 2;

        // White circle in center for donut look (optional, looks nicer)
        int holeSize = pieSize / 3;
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(cx - holeSize / 2, cy - holeSize / 2, holeSize, holeSize));

        g2.setColor(new Color(50, 50, 50));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        FontMetrics mfm = g2.getFontMetrics();
        g2.drawString(totalStr, cx - mfm.stringWidth(totalStr) / 2, cy + 5);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(Color.GRAY);
        g2.drawString("total", cx - g2.getFontMetrics().stringWidth("total") / 2, cy + 18);

        g2.dispose();
    }

    private void drawEmptyState(Graphics2D g2, int w, int h) {
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2.setColor(Color.GRAY);
        String msg = "No expense data for this month.";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
    }

    private String formatCategory(String raw) {
        // e.g. "OTHER_INCOME" -> "Other Income"
        String[] words = raw.replace("_", " ").toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}
