package com.sixstars.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.sixstars.model.Account;

/**
 * Rounded avatar with optional profile photo or initials fallback (shared by Account Center).
 */
public final class AccountAvatarPanel extends JPanel {

    private final int pixelSize;
    private BufferedImage image;
    private String initials = "??";

    public AccountAvatarPanel(int pixelSize) {
        this.pixelSize = pixelSize;
        setOpaque(false);
        setPreferredSize(new Dimension(pixelSize, pixelSize));
        setMinimumSize(new Dimension(pixelSize, pixelSize));
        setMaximumSize(new Dimension(pixelSize, pixelSize));
    }

    public void setAccount(Account account) {
        initials = buildInitials(account);
        image = null;
        if (account != null && account.getProfileImagePath() != null && !account.getProfileImagePath().isBlank()) {
            try {
                File file = new File(account.getProfileImagePath());
                if (file.exists()) {
                    image = ImageIO.read(file);
                }
            } catch (Exception ignored) {
                image = null;
            }
        }
        repaint();
    }

    public void clear() {
        image = null;
        initials = "??";
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            Shape clip = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setClip(clip);

            if (image != null) {
                drawCoverImage(g2, image);
                g2.setColor(new Color(0, 0, 0, 48));
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g2.setPaint(new Color(194, 159, 92));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setPaint(new Color(229, 220, 201));
                g2.fillRect(0, 0, getWidth(), getHeight() / 2);
            }

            g2.setClip(null);
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 20, 20));

            if (image == null) {
                g2.setColor(new Color(70, 50, 35));
                int fontSize = Math.max(18, Math.min(40, pixelSize / 3));
                g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initials)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initials, x, y);
            }
        } finally {
            g2.dispose();
        }
    }

    private void drawCoverImage(Graphics2D g2, BufferedImage img) {
        int panelW = getWidth();
        int panelH = getHeight();
        int imageW = img.getWidth();
        int imageH = img.getHeight();
        double scale = Math.max((double) panelW / imageW, (double) panelH / imageH);
        int drawW = (int) Math.round(imageW * scale);
        int drawH = (int) Math.round(imageH * scale);
        int drawX = (panelW - drawW) / 2;
        int drawY = (panelH - drawH) / 2;
        g2.drawImage(img, drawX, drawY, drawW, drawH, null);
    }

    private static String buildInitials(Account account) {
        if (account == null) {
            return "??";
        }
        String first = account.getFirstName() == null || account.getFirstName().isBlank() ? "" : account.getFirstName().trim();
        String last = account.getLastName() == null || account.getLastName().isBlank() ? "" : account.getLastName().trim();
        StringBuilder sb = new StringBuilder();
        if (!first.isEmpty()) {
            sb.append(Character.toUpperCase(first.charAt(0)));
        }
        if (!last.isEmpty()) {
            sb.append(Character.toUpperCase(last.charAt(0)));
        }
        return sb.length() == 0 ? "??" : sb.toString();
    }
}
