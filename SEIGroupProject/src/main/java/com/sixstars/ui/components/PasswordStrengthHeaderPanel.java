package com.sixstars.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.sixstars.service.security.PasswordStrengthEvaluator;
import com.sixstars.ui.UITheme;

/**
 * Compact row: strength meter + tier label. Shared by Account Center security and Create Account.
 */
public final class PasswordStrengthHeaderPanel extends JPanel {

    private final PasswordStrengthMeter meter = new PasswordStrengthMeter();
    private final JLabel tierLabel = new JLabel();

    public PasswordStrengthHeaderPanel(boolean useRegistrationCopy) {
        super(new BorderLayout(14, 0));
        setOpaque(false);
        tierLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        tierLabel.setForeground(UITheme.TEXT_MEDIUM);
        tierLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tierLabel.setPreferredSize(new Dimension(130, 22));
        tierLabel.setMinimumSize(new Dimension(96, 22));
        add(meter, BorderLayout.CENTER);
        add(tierLabel, BorderLayout.EAST);
        this.registrationCopy = useRegistrationCopy;
    }

    private final boolean registrationCopy;

    /** Empty password → neutral meter; otherwise uses {@link PasswordStrengthEvaluator}. */
    public void updateFromNewPassword(String rawNew) {
        if (rawNew == null || rawNew.isEmpty()) {
            meter.setMeter(0, new Color(210, 205, 198));
            tierLabel.setText(registrationCopy ? "Enter a password" : "Enter a new password");
            tierLabel.setForeground(UITheme.TEXT_MEDIUM);
            return;
        }
        PasswordStrengthEvaluator.Result r = PasswordStrengthEvaluator.evaluate(rawNew);
        Color barColor = switch (r.score()) {
            case 0, 1 -> new Color(180, 60, 55);
            case 2, 3 -> new Color(200, 145, 55);
            default -> new Color(55, 140, 85);
        };
        meter.setMeter(r.score(), barColor);
        tierLabel.setText(r.tierLabel());
        Color tierText = switch (r.score()) {
            case 0, 1 -> new Color(150, 45, 40);
            case 2, 3 -> new Color(145, 100, 35);
            default -> new Color(35, 110, 70);
        };
        tierLabel.setForeground(tierText);
    }
}
