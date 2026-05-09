package com.sixstars.ui.accountcenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.NotificationType;
import com.sixstars.service.security.PasswordStrengthEvaluator;
import com.sixstars.service.security.SignInAuditService;
import com.sixstars.ui.UITheme;
import com.sixstars.ui.components.PasswordStrengthHeaderPanel;
import com.sixstars.ui.components.PasswordStrengthUiFormatter;

/**
 * Security & sign-in tab — isolated from {@code AccountCenterPage} (single responsibility).
 */
public final class AccountSecurityTabPanel extends JPanel {

    private static final Color BILLING_SECTION_BG = new Color(252, 250, 245);
    private static final DateTimeFormatter SIGNIN_TIME = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a")
            .withZone(ZoneId.systemDefault());

    private final AccountCenterContext ctx;

    private final JPasswordField currentPasswordField = new JPasswordField();
    private final JPasswordField newPasswordField = new JPasswordField();
    private final JPasswordField confirmPasswordField = new JPasswordField();
    private final JCheckBox showPasswordsCheck = new JCheckBox("Show passwords");
    private final JButton updatePasswordButton = new JButton("Update password");
    private final PasswordStrengthHeaderPanel strengthHeader = new PasswordStrengthHeaderPanel(false);
    private JLabel secRuleLengthLabel;
    private JLabel secRuleUpperLabel;
    private JLabel secRuleLowerLabel;
    private JLabel secRuleDigitLabel;
    private JLabel secRuleSpecialLabel;
    private JLabel passwordConfirmMatchLabel;
    private JLabel securityAccountStatusLabel;
    private JCheckBox secChkNewDevice;
    private JCheckBox secChkLoginAlert;
    private JCheckBox secChkReauth;
    private JButton secBtnForgotPassword;
    private JLabel thisDeviceSummaryLabel;
    private JPanel securityLoginHistoryInner;
    private boolean loadingSecurityPrefs;

    public AccountSecurityTabPanel(AccountCenterContext ctx) {
        this.ctx = ctx;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UITheme.PAGE_BACKGROUND);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        add(buildScrollableBody());
        wireListeners();
    }

    private JPanel buildScrollableBody() {
        JPanel mainPanel = AccountCenterSwingFactory.createContentPanel();

        JLabel title = new JLabel("Security & sign-in");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("<html><div style=\"width:720px\">Protect your 6 Stars account with a strong password, "
                + "sensible alerts, and recovery options — organized the way modern travel apps present security.</div></html>");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 22)));

        JPanel statusCard = AccountCenterSwingFactory.createSecurityShellCard(BILLING_SECTION_BG, new Color(231, 223, 204));
        statusCard.add(AccountCenterSwingFactory.createSectionTitle("Account protection", "Verification status and where you are signed in."));
        statusCard.add(Box.createRigidArea(new Dimension(0, 12)));
        securityAccountStatusLabel = new JLabel(" ");
        securityAccountStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        securityAccountStatusLabel.setForeground(UITheme.TEXT_DARK);
        securityAccountStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusCard.add(securityAccountStatusLabel);
        mainPanel.add(statusCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel pwdCard = AccountCenterSwingFactory.createSecurityShellCard(BILLING_SECTION_BG, new Color(231, 223, 204));
        pwdCard.add(AccountCenterSwingFactory.createSectionTitle("Change password", "We never store your plain password — only a secure hash."));
        pwdCard.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel passwordGrid = new JPanel(new GridLayout(3, 1, 0, 12));
        passwordGrid.setOpaque(false);
        passwordGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        passwordGrid.add(AccountCenterSwingFactory.createPasswordCard("Current password", currentPasswordField));
        passwordGrid.add(AccountCenterSwingFactory.createPasswordCard("New password", newPasswordField));
        passwordGrid.add(AccountCenterSwingFactory.createPasswordCard("Confirm new password", confirmPasswordField));
        pwdCard.add(passwordGrid);
        pwdCard.add(Box.createRigidArea(new Dimension(0, 14)));

        JLabel strengthHead = new JLabel("Password strength");
        strengthHead.setFont(new Font("SansSerif", Font.BOLD, 12));
        strengthHead.setForeground(UITheme.TEXT_DARK);
        strengthHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        pwdCard.add(strengthHead);
        pwdCard.add(Box.createRigidArea(new Dimension(0, 6)));

        JPanel strengthRow = new JPanel(new BorderLayout(0, 0));
        strengthRow.setOpaque(false);
        strengthRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        strengthRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        strengthHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        strengthRow.add(strengthHeader, BorderLayout.CENTER);
        pwdCard.add(strengthRow);
        pwdCard.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel rulesCol = new JPanel();
        rulesCol.setLayout(new BoxLayout(rulesCol, BoxLayout.Y_AXIS));
        rulesCol.setOpaque(false);
        rulesCol.setAlignmentX(Component.LEFT_ALIGNMENT);
        secRuleLengthLabel = new JLabel();
        secRuleUpperLabel = new JLabel();
        secRuleLowerLabel = new JLabel();
        secRuleDigitLabel = new JLabel();
        secRuleSpecialLabel = new JLabel();
        styleSecurityRuleLabel(secRuleLengthLabel);
        styleSecurityRuleLabel(secRuleUpperLabel);
        styleSecurityRuleLabel(secRuleLowerLabel);
        styleSecurityRuleLabel(secRuleDigitLabel);
        styleSecurityRuleLabel(secRuleSpecialLabel);
        rulesCol.add(secRuleLengthLabel);
        rulesCol.add(Box.createRigidArea(new Dimension(0, 4)));
        rulesCol.add(secRuleUpperLabel);
        rulesCol.add(Box.createRigidArea(new Dimension(0, 4)));
        rulesCol.add(secRuleLowerLabel);
        rulesCol.add(Box.createRigidArea(new Dimension(0, 4)));
        rulesCol.add(secRuleDigitLabel);
        rulesCol.add(Box.createRigidArea(new Dimension(0, 4)));
        rulesCol.add(secRuleSpecialLabel);
        pwdCard.add(rulesCol);
        pwdCard.add(Box.createRigidArea(new Dimension(0, 8)));

        passwordConfirmMatchLabel = new JLabel(" ");
        passwordConfirmMatchLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        passwordConfirmMatchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pwdCard.add(passwordConfirmMatchLabel);

        pwdCard.add(Box.createRigidArea(new Dimension(0, 10)));
        showPasswordsCheck.setOpaque(false);
        showPasswordsCheck.setFont(new Font("SansSerif", Font.PLAIN, 13));
        showPasswordsCheck.setForeground(UITheme.TEXT_DARK);
        pwdCard.add(showPasswordsCheck);

        pwdCard.add(Box.createRigidArea(new Dimension(0, 12)));
        JLabel pwdFoot = new JLabel("<html><div style=\"width:680px;color:#777;font-size:12px;\">After a successful change you "
                + "remain signed in on this device. Use a password manager when possible.</div></html>");
        pwdFoot.setAlignmentX(Component.LEFT_ALIGNMENT);
        pwdCard.add(pwdFoot);
        pwdCard.add(Box.createRigidArea(new Dimension(0, 14)));

        AccountCenterSwingFactory.styleButton(updatePasswordButton, true);
        updatePasswordButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        updatePasswordButton.setMaximumSize(new Dimension(320, 44));
        pwdCard.add(updatePasswordButton);

        mainPanel.add(pwdCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel signInCard = AccountCenterSwingFactory.createSecurityShellCard(BILLING_SECTION_BG, new Color(231, 223, 204));
        signInCard.add(AccountCenterSwingFactory.createSectionTitle("Sign-in & alerts", "Preferences stored on this computer for this account."));
        signInCard.add(Box.createRigidArea(new Dimension(0, 12)));
        secChkNewDevice = new JCheckBox("Email me when sign-in looks different on this computer");
        secChkLoginAlert = new JCheckBox("In-app alerts for password and sign-in changes");
        secChkReauth = new JCheckBox("Require password again for sensitive actions");
        for (JCheckBox c : new JCheckBox[]{secChkNewDevice, secChkLoginAlert, secChkReauth}) {
            c.setOpaque(false);
            c.setFont(new Font("SansSerif", Font.PLAIN, 14));
            c.setForeground(UITheme.TEXT_DARK);
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        signInCard.add(secChkNewDevice);
        signInCard.add(securityPrefHint("Sends one email (when Mailgun is configured) if this app’s saved device profile "
                + "does not match the last sign-in — for example after reinstalling the app."));
        signInCard.add(Box.createRigidArea(new Dimension(0, 8)));
        signInCard.add(secChkLoginAlert);
        signInCard.add(securityPrefHint("Uses your notification preferences for “Account activity” in the Notifications tab."));
        signInCard.add(Box.createRigidArea(new Dimension(0, 8)));
        signInCard.add(secChkReauth);
        signInCard.add(securityPrefHint("When enabled, deleting your account asks for your password again after email and code verification."));
        mainPanel.add(signInCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel sessionCard = AccountCenterSwingFactory.createSecurityShellCard(BILLING_SECTION_BG, new Color(231, 223, 204));
        sessionCard.add(AccountCenterSwingFactory.createSectionTitle("This app",
                "This desktop client keeps sign-in history on this computer only."));
        sessionCard.add(Box.createRigidArea(new Dimension(0, 12)));
        JLabel thisDevice = new JLabel("<html><div style=\"width:680px;\"><b>6 Stars Hotel desktop app</b><br/>"
                + "Your session exists only in this running application. There are no separate web or mobile sessions "
                + "to revoke from here.</div></html>");
        thisDevice.setFont(new Font("SansSerif", Font.PLAIN, 14));
        thisDevice.setForeground(UITheme.TEXT_DARK);
        thisDevice.setAlignmentX(Component.LEFT_ALIGNMENT);
        sessionCard.add(thisDevice);
        sessionCard.add(Box.createRigidArea(new Dimension(0, 10)));
        thisDeviceSummaryLabel = new JLabel(" ");
        thisDeviceSummaryLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        thisDeviceSummaryLabel.setForeground(UITheme.TEXT_MEDIUM);
        thisDeviceSummaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sessionCard.add(thisDeviceSummaryLabel);
        mainPanel.add(sessionCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel recoveryCard = AccountCenterSwingFactory.createSecurityShellCard(BILLING_SECTION_BG, new Color(231, 223, 204));
        recoveryCard.add(AccountCenterSwingFactory.createSectionTitle("Account recovery", "If you cannot remember your password, use email verification."));
        recoveryCard.add(Box.createRigidArea(new Dimension(0, 12)));
        JLabel recoveryBlurb = new JLabel("<html><div style=\"width:680px;\">If Mailgun is configured in your environment, "
                + "we email a one-time code. Use the button below to open the reset flow for your signed-in email.</div></html>");
        recoveryBlurb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        recoveryBlurb.setForeground(UITheme.TEXT_MEDIUM);
        recoveryBlurb.setAlignmentX(Component.LEFT_ALIGNMENT);
        recoveryCard.add(recoveryBlurb);
        recoveryCard.add(Box.createRigidArea(new Dimension(0, 12)));
        secBtnForgotPassword = new JButton("Forgot password — open email reset");
        AccountCenterSwingFactory.styleButton(secBtnForgotPassword, false);
        secBtnForgotPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        secBtnForgotPassword.setMaximumSize(new Dimension(360, 40));
        recoveryCard.add(secBtnForgotPassword);
        mainPanel.add(recoveryCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel activityCard = AccountCenterSwingFactory.createSecurityShellCard(BILLING_SECTION_BG, new Color(231, 223, 204));
        activityCard.add(AccountCenterSwingFactory.createSectionTitle("Recent sign-in activity",
                "Recorded on this computer when you successfully sign in."));
        activityCard.add(Box.createRigidArea(new Dimension(0, 12)));
        securityLoginHistoryInner = new JPanel();
        securityLoginHistoryInner.setLayout(new BoxLayout(securityLoginHistoryInner, BoxLayout.Y_AXIS));
        securityLoginHistoryInner.setOpaque(true);
        securityLoginHistoryInner.setBackground(new Color(252, 252, 254));
        securityLoginHistoryInner.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane activityScroll = new JScrollPane(securityLoginHistoryInner);
        activityScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 215, 205)));
        activityScroll.getViewport().setBackground(new Color(252, 252, 254));
        activityScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        activityScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        activityScroll.getVerticalScrollBar().setUnitIncrement(16);
        activityCard.add(activityScroll);
        mainPanel.add(activityCard);

        DocumentListener passwordMeterListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshPasswordStrengthMeter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshPasswordStrengthMeter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshPasswordStrengthMeter();
            }
        };
        newPasswordField.getDocument().addDocumentListener(passwordMeterListener);
        confirmPasswordField.getDocument().addDocumentListener(passwordMeterListener);
        refreshPasswordStrengthMeter();

        return mainPanel;
    }

    private void wireListeners() {
        updatePasswordButton.addActionListener(e -> updatePassword());
        showPasswordsCheck.addActionListener(e -> togglePasswordVisibility(showPasswordsCheck.isSelected()));
        Runnable saveSec = this::saveSecurityPreferences;
        secChkNewDevice.addActionListener(e -> saveSec.run());
        secChkLoginAlert.addActionListener(e -> saveSec.run());
        secChkReauth.addActionListener(e -> saveSec.run());
        secBtnForgotPassword.addActionListener(e -> openForgotPasswordFromSecurity());
    }

    private JLabel securityPrefHint(String text) {
        JLabel l = new JLabel("<html><div style=\"width:660px;color:#777;font-size:12px;\">"
                + PasswordStrengthUiFormatter.escapeHtmlLite(text) + "</div></html>");
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleSecurityRuleLabel(JLabel label) {
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public void refreshSecurityWorkspace() {
        if (securityAccountStatusLabel == null) {
            return;
        }
        Account account = AccountController.currentAccount;
        if (account == null) {
            securityAccountStatusLabel.setText(" ");
            secChkNewDevice.setEnabled(false);
            secChkLoginAlert.setEnabled(false);
            secChkReauth.setEnabled(false);
            secBtnForgotPassword.setEnabled(false);
            if (thisDeviceSummaryLabel != null) {
                thisDeviceSummaryLabel.setText(" ");
            }
            securityLoginHistoryInner.removeAll();
            securityLoginHistoryInner.revalidate();
            securityLoginHistoryInner.repaint();
            return;
        }

        boolean verified = account.isEmailVerified();
        String badge = verified
                ? "<span style='background:#e6f4ea;color:#1e6b3a;padding:4px 10px;border-radius:6px;font-weight:bold;'>Verified</span>"
                : "<span style='background:#fff3e0;color:#a65a12;padding:4px 10px;border-radius:6px;font-weight:bold;'>Not verified</span>";
        securityAccountStatusLabel.setText("<html><div style=\"width:700px;\">Signed in as <b>"
                + PasswordStrengthUiFormatter.escapeHtmlLite(account.getEmail())
                + "</b> &nbsp;" + badge + "<br/><span style='color:#666;font-size:13px;'>"
                + (verified ? "Your email can be used for receipts and password reset."
                : "Verify your email from the welcome flow so we can send security codes.")
                + "</span></div></html>");

        loadSecurityPreferences(account);
        secChkNewDevice.setEnabled(true);
        secChkLoginAlert.setEnabled(true);
        secChkReauth.setEnabled(true);
        secBtnForgotPassword.setEnabled(true);

        if (thisDeviceSummaryLabel != null) {
            thisDeviceSummaryLabel.setText("<html><div style=\"width:680px;\">"
                    + PasswordStrengthUiFormatter.escapeHtmlLite(SignInAuditService.currentSessionSummary(account))
                    + "</div></html>");
        }
        refreshSignInActivityList(account);
        refreshPasswordStrengthMeter();
    }

    public void resetWhenLoggedOut() {
        clearPasswordFields();
        showPasswordsCheck.setSelected(false);
        togglePasswordVisibility(false);
        refreshSecurityWorkspace();
    }

    /** Call when the signed-in account changes while this tab may stay mounted. */
    public void refreshAfterAccountContextChange() {
        clearPasswordFields();
        togglePasswordVisibility(showPasswordsCheck.isSelected());
        refreshSecurityWorkspace();
    }

    private void loadSecurityPreferences(Account account) {
        loadingSecurityPrefs = true;
        try {
            Preferences p = ctx.preferencesFor(account);
            secChkNewDevice.setSelected(p.getBoolean(SecurityPreferenceKeys.NEW_DEVICE_EMAIL, true));
            secChkLoginAlert.setSelected(p.getBoolean(SecurityPreferenceKeys.LOGIN_ALERT_IN_APP, true));
            secChkReauth.setSelected(p.getBoolean(SecurityPreferenceKeys.REAUTH_SENSITIVE, true));
        } finally {
            loadingSecurityPrefs = false;
        }
    }

    private void saveSecurityPreferences() {
        if (loadingSecurityPrefs) {
            return;
        }
        Account account = AccountController.currentAccount;
        if (account == null) {
            return;
        }
        Preferences p = ctx.preferencesFor(account);
        p.putBoolean(SecurityPreferenceKeys.NEW_DEVICE_EMAIL, secChkNewDevice.isSelected());
        p.putBoolean(SecurityPreferenceKeys.LOGIN_ALERT_IN_APP, secChkLoginAlert.isSelected());
        p.putBoolean(SecurityPreferenceKeys.REAUTH_SENSITIVE, secChkReauth.isSelected());
    }

    private void refreshPasswordStrengthMeter() {
        if (secRuleLengthLabel == null) {
            return;
        }
        String rawNew = new String(newPasswordField.getPassword());
        String rawConfirm = new String(confirmPasswordField.getPassword());

        strengthHeader.updateFromNewPassword(rawNew);

        if (rawNew.isEmpty()) {
            secRuleLengthLabel.setText(PasswordStrengthUiFormatter.formatRuleLineNeutral("At least 8 characters"));
            secRuleUpperLabel.setText(PasswordStrengthUiFormatter.formatRuleLineNeutral("One uppercase letter (A–Z)"));
            secRuleLowerLabel.setText(PasswordStrengthUiFormatter.formatRuleLineNeutral("One lowercase letter (a–z)"));
            secRuleDigitLabel.setText(PasswordStrengthUiFormatter.formatRuleLineNeutral("One number (0–9)"));
            secRuleSpecialLabel.setText(PasswordStrengthUiFormatter.formatRuleLineNeutral("One special character (!@#$… )"));
        } else {
            PasswordStrengthEvaluator.Result r = PasswordStrengthEvaluator.evaluate(rawNew);
            secRuleLengthLabel.setText(PasswordStrengthUiFormatter.formatRuleLine(r.lengthOk(), "At least 8 characters"));
            secRuleUpperLabel.setText(PasswordStrengthUiFormatter.formatRuleLine(r.upperOk(), "One uppercase letter (A–Z)"));
            secRuleLowerLabel.setText(PasswordStrengthUiFormatter.formatRuleLine(r.lowerOk(), "One lowercase letter (a–z)"));
            secRuleDigitLabel.setText(PasswordStrengthUiFormatter.formatRuleLine(r.digitOk(), "One number (0–9)"));
            secRuleSpecialLabel.setText(PasswordStrengthUiFormatter.formatRuleLine(r.specialOk(), "One special character (!@#$… )"));
        }

        if (rawConfirm.isEmpty()) {
            passwordConfirmMatchLabel.setText(" ");
        } else if (PasswordStrengthEvaluator.matches(rawNew, rawConfirm)) {
            passwordConfirmMatchLabel.setText("<html><span style='color:#2d7a4a;'>✓ New passwords match.</span></html>");
        } else {
            passwordConfirmMatchLabel.setText("<html><span style='color:#b03030;'>Passwords do not match yet.</span></html>");
        }
    }

    private void refreshSignInActivityList(Account account) {
        securityLoginHistoryInner.removeAll();
        List<SignInAuditService.SignInEntry> entries = SignInAuditService.readRecentHistory(account, 25);
        if (entries.isEmpty()) {
            JLabel empty = new JLabel("<html><div style=\"width:640px;color:#666;font-size:13px;\">"
                    + "No sign-ins recorded yet. Each successful login from this app is listed here.</div></html>");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            securityLoginHistoryInner.add(empty);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                SignInAuditService.SignInEntry e = entries.get(i);
                securityLoginHistoryInner.add(buildSignInHistoryRow(e, i == 0));
                securityLoginHistoryInner.add(Box.createRigidArea(new Dimension(0, 6)));
            }
        }
        securityLoginHistoryInner.revalidate();
        securityLoginHistoryInner.repaint();
    }

    private JPanel buildSignInHistoryRow(SignInAuditService.SignInEntry entry, boolean mostRecent) {
        String when = SIGNIN_TIME.format(entry.when());
        String line = when + " — " + entry.detailLine();
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(true);
        row.setBackground(mostRecent ? new Color(236, 248, 240) : Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 215, 205), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        JLabel l = new JLabel("<html><div style=\"font-size:13px;\">"
                + PasswordStrengthUiFormatter.escapeHtmlLite(line) + "</div></html>");
        if (mostRecent) {
            JLabel pill = new JLabel("Latest");
            pill.setFont(new Font("SansSerif", Font.BOLD, 11));
            pill.setForeground(new Color(30, 110, 65));
            pill.setOpaque(true);
            pill.setBackground(new Color(210, 240, 218));
            pill.setBorder(new EmptyBorder(3, 8, 3, 8));
            row.add(l, BorderLayout.CENTER);
            row.add(pill, BorderLayout.EAST);
        } else {
            row.add(l, BorderLayout.CENTER);
        }
        return row;
    }

    private void openForgotPasswordFromSecurity() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            return;
        }
        if (Main.passwordResetPage != null) {
            Main.passwordResetPage.openForEmail(account.getEmail());
        }
        ctx.applicationCardLayout().show(ctx.applicationPages(), "forgot password");
    }

    private void togglePasswordVisibility(boolean visible) {
        char echo = visible ? (char) 0 : AccountCenterSwingFactory.PASSWORD_ECHO_CHAR;
        currentPasswordField.setEchoChar(echo);
        newPasswordField.setEchoChar(echo);
        confirmPasswordField.setEchoChar(echo);
    }

    private void clearPasswordFields() {
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private void updatePassword() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            JOptionPane.showMessageDialog(this, "Please sign in to change your password.", "Change password",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String current = new String(currentPasswordField.getPassword());
        String next = new String(newPasswordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (current.isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter your current password so we can verify it is you.",
                    "Change password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PasswordStrengthEvaluator.Result strength = PasswordStrengthEvaluator.evaluate(next);
        if (!strength.lengthOk() || !strength.upperOk() || !strength.lowerOk() || !strength.digitOk() || !strength.specialOk()) {
            JOptionPane.showMessageDialog(this,
                    "Your new password must satisfy every rule in the checklist (length, upper, lower, number, and special character).",
                    "Change password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (confirm.isBlank()) {
            JOptionPane.showMessageDialog(this, "Type your new password again in the confirmation field.",
                    "Change password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!PasswordStrengthEvaluator.matches(next, confirm)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.",
                    "Change password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ctx.accountController().changePassword(current, next, confirm);
            clearPasswordFields();
            refreshPasswordStrengthMeter();
            if (Main.headerBar != null) {
                Main.headerBar.refreshInfo();
            }
            ctx.refreshFullAccountCenterUi();
            ctx.notificationService().publishForCurrentAccount(NotificationType.ACCOUNT_ACTIVITY, "Your password was changed successfully.");
            JOptionPane.showMessageDialog(this,
                    "Your password was updated. You remain signed in on this device.",
                    "Change password", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Could not change password.";
            JOptionPane.showMessageDialog(this, msg, "Change password", JOptionPane.ERROR_MESSAGE);
        }
    }
}
