package com.financetracker.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MetricCard extends JPanel {
    private JLabel titleLabel;
    private JLabel valueLabel;
    private JLabel subtitleLabel;

    public MetricCard(String title, Color valueColor) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        
        // Creating a clean rounded border with padding
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, valueColor),
                new EmptyBorder(15, 12, 15, 12)
            )
        ));

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(130, 130, 130));

        valueLabel = new JLabel("Rs.0");
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(valueColor);

        subtitleLabel = new JLabel("● This month");
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        subtitleLabel.setForeground(valueColor);

        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(valueLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(subtitleLabel);
    }

    public void setValue(String value, String subtitle, Color subtitleColor) {
        valueLabel.setText(value);
        if (subtitle != null && !subtitle.isEmpty()) {
            subtitleLabel.setText("● " + subtitle);
            if (subtitleColor != null) {
                subtitleLabel.setForeground(subtitleColor);
            }
        } else {
            subtitleLabel.setText(" ");
        }
    }
}
