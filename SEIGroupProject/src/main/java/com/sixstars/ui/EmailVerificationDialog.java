package com.sixstars.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.sixstars.controller.AccountController;

public class EmailVerificationDialog extends JDialog {
    private final AccountController accountController;
    private final String email;
    private final JTextField codeField = new JTextField();
    private boolean verified = false;

    public EmailVerificationDialog(Window owner, AccountController accountController, String email) {
        super(owner, "Verify Your Email", ModalityType.APPLICATION_MODAL);
        this.accountController = accountController;
        this.email = email;
        buildUi();
        pack();
        setMinimumSize(new Dimension(420, 240));
        setLocationRelativeTo(owner);
    }

    public boolean wasVerified() {
        return verified;
    }

    private void buildUi() {
        setLayout(new BorderLayout());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setBackground(UITheme.CARD_BACKGROUND);

        JLabel title = new JLabel("Verify your email");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("We sent a 6-digit code to " + email + ". Enter it below to finish creating your account.");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        codeField.setPreferredSize(new Dimension(360, 40));
        codeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        codeField.setFont(UITheme.INPUT_FONT);
        codeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        codeField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton resendButton = new JButton("Resend Code");
        styleSecondaryButton(resendButton);
        resendButton.addActionListener(_ -> {
            try {
                accountController.resendVerificationCode(email);
                JOptionPane.showMessageDialog(this, "A new verification code was sent.");
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Resend Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        styleSecondaryButton(cancelButton);
        cancelButton.addActionListener(_ -> dispose());

        JButton verifyButton = new JButton("Verify");
        verifyButton.setFont(UITheme.BUTTON_FONT);
        verifyButton.setBackground(UITheme.ACCENT_GOLD);
        verifyButton.setForeground(java.awt.Color.WHITE);
        verifyButton.setFocusPainted(false);
        verifyButton.setBorderPainted(false);
        verifyButton.setOpaque(true);
        verifyButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        verifyButton.addActionListener(_ -> {
            String code = codeField.getText().trim();
            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter the verification code.", "Missing Code", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                if (accountController.verifyAccountEmail(email, code)) {
                    verified = true;
                    JOptionPane.showMessageDialog(this, "Email verified successfully.");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid or expired code.", "Verification Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Verification Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        actions.add(resendButton);
        actions.add(cancelButton);
        actions.add(verifyButton);

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(codeField);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(actions);

        add(card, BorderLayout.CENTER);
    }

    private void styleSecondaryButton(JButton button) {
        button.setPreferredSize(new Dimension(120, 36));
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
}

