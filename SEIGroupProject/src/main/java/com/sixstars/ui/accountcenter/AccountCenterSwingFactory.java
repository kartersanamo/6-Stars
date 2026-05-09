package com.sixstars.ui.accountcenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import com.sixstars.ui.UITheme;

/**
 * Reusable Swing layout/styling for Account Center tabs — keeps {@code AccountCenterPage} thin (Clean Code: SRP).
 */
public final class AccountCenterSwingFactory {

    public static final char PASSWORD_ECHO_CHAR = new JPasswordField().getEchoChar();

    private AccountCenterSwingFactory() {
    }

    public static JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.PAGE_BACKGROUND);
        panel.setBorder(new EmptyBorder(32, 32, 32, 32));
        return panel;
    }

    public static JPanel wrapInScrollPane(JPanel contentPanel) {
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.PAGE_BACKGROUND);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    public static JPanel createSectionTitle(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(UITheme.TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(UITheme.TEXT_MEDIUM);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(subtitleLabel);

        return panel;
    }

    public static JLabel createHeaderCell(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(UITheme.TEXT_MEDIUM);
        if (!"Notification Type".equals(text)) {
            label.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return label;
    }

    public static JPanel createLabeledFieldCard(String label, JComponent component, boolean editable) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(new Color(249, 249, 249));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_MEDIUM);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (component instanceof JTextField textField) {
            styleTextField(textField);
            textField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
            textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        } else if (component instanceof JLabel valueLabel) {
            valueLabel.setFont(UITheme.INPUT_FONT);
            valueLabel.setForeground(UITheme.TEXT_DARK);
            valueLabel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UITheme.BORDER_COLOR, 1, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));
            valueLabel.setOpaque(true);
            valueLabel.setBackground(Color.WHITE);
            valueLabel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
            valueLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        }
        if (!editable && component instanceof JTextField textField) {
            textField.setEditable(false);
        }
        card.add(component);
        return card;
    }

    public static JPanel createPasswordCard(String label, JPasswordField field) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(new Color(249, 249, 249));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_MEDIUM);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        stylePasswordField(field);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(field);
        return card;
    }

    public static JButton styleButton(JButton button, boolean primary) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(10, 42));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        if (primary) {
            button.setBackground(UITheme.ACCENT_GOLD);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(UITheme.SECONDARY_BUTTON);
            button.setForeground(UITheme.TEXT_DARK);
        }
        return button;
    }

    public static void styleTextField(JTextField field) {
        field.setFont(UITheme.INPUT_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(UITheme.TEXT_DARK);
        field.setCaretColor(UITheme.TEXT_DARK);
    }

    public static void stylePasswordField(JPasswordField field) {
        field.setFont(UITheme.INPUT_FONT);
        field.setEchoChar(PASSWORD_ECHO_CHAR);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(UITheme.TEXT_DARK);
        field.setCaretColor(UITheme.TEXT_DARK);
    }

    public static void stylePaymentQuickAction(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(UITheme.ACCENT_GOLD);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 42));
        button.setMinimumSize(new Dimension(160, 42));
        button.setMaximumSize(new Dimension(280, 42));
    }

    public static void applyBasicStripeConnectButtonChrome(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
    }

    public static JPanel makeLabeledStripeFieldRow(String label, JTextField field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_DARK);
        styleTextField(field);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.add(l);
        row.add(Box.createRigidArea(new Dimension(0, 4)));
        row.add(field);
        return row;
    }

    public static JPanel makeLabeledStripeFieldPassword(String label, JPasswordField field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_DARK);
        stylePasswordField(field);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.add(l);
        row.add(Box.createRigidArea(new Dimension(0, 4)));
        row.add(field);
        return row;
    }

    public static JPanel labeledStripeMini(String hint, JTextField field) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        JLabel l = new JLabel(hint);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(UITheme.TEXT_MEDIUM);
        styleTextField(field);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        box.add(l);
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        box.add(field);
        return box;
    }

    public static JPanel makeLabeledStripeFieldPasswordMini(String hint, JPasswordField field) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        JLabel l = new JLabel(hint);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(UITheme.TEXT_MEDIUM);
        stylePasswordField(field);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        box.add(l);
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        box.add(field);
        return box;
    }

    public static JPanel createSecurityShellCard(Color sectionBg, Color borderColor) {
        JPanel card = createCardPanel();
        card.setBackground(sectionBg);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(20, 22, 22, 22)
        ));
        return card;
    }

    public static JPanel createNavButtonRow(JButton button) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setPreferredSize(new Dimension(0, 42));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.add(button, BorderLayout.CENTER);
        return row;
    }

    /** Two-column row for account name fields. */
    public static JPanel gridTwo(JComponent left, JComponent right, int gap) {
        JPanel row = new JPanel(new GridLayout(1, 2, gap, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        row.add(left);
        row.add(right);
        return row;
    }
}
