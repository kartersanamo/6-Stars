package com.sixstars.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * Custom horizontal strength meter. Avoids macOS {@code AquaProgressBarUI} which can throw
 * {@link NegativeArraySizeException} when laid out at transient zero width.
 */
public final class PasswordStrengthMeter extends JPanel {

    private int score;
    private Color fillColor = new Color(210, 205, 198);

    public PasswordStrengthMeter() {
        setOpaque(false);
        setPreferredSize(new Dimension(320, 18));
        setMinimumSize(new Dimension(120, 14));
        setMaximumSize(new Dimension(4000, 28));
    }

    public void setMeter(int score, Color fill) {
        this.score = Math.max(0, Math.min(5, score));
        this.fillColor = fill != null ? fill : new Color(210, 205, 198);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        if (w <= 2 || h <= 2) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = Math.max(4, Math.min(h / 2, 10));
            g2.setColor(new Color(245, 240, 234));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setColor(new Color(200, 190, 180));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            int innerW = w - 6;
            int innerH = h - 6;
            if (innerW > 0 && innerH > 0 && score > 0) {
                int fillW = (int) Math.round(innerW * (score / 5.0));
                fillW = Math.max(1, Math.min(innerW, fillW));
                g2.setColor(fillColor);
                g2.fillRoundRect(3, 3, fillW, innerH, Math.max(2, arc - 2), Math.max(2, arc - 2));
            }
        } finally {
            g2.dispose();
        }
    }
}
