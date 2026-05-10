package com.sixstars.ui.accountcenter;

import java.awt.CardLayout;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.JPanel;

import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.service.NotificationService;

/**
 * Dependencies shared across Account Center tabs (composition root passes one instance).
 */
public final class AccountCenterContext {

    public static final String PREF_NODE_PREFIX = "account-center-";

    private final JPanel applicationPages;
    private final CardLayout applicationCardLayout;
    private final AccountController accountController;
    private final NotificationService notificationService;
    private final Preferences preferencesRoot;
    private final Runnable refreshFullAccountCenterUi;

    public AccountCenterContext(
            JPanel applicationPages,
            CardLayout applicationCardLayout,
            AccountController accountController,
            NotificationService notificationService,
            Preferences preferencesRoot,
            Runnable refreshFullAccountCenterUi) {
        this.applicationPages = applicationPages;
        this.applicationCardLayout = applicationCardLayout;
        this.accountController = accountController;
        this.notificationService = notificationService;
        this.preferencesRoot = preferencesRoot;
        this.refreshFullAccountCenterUi = refreshFullAccountCenterUi;
    }

    public JPanel applicationPages() {
        return applicationPages;
    }

    public CardLayout applicationCardLayout() {
        return applicationCardLayout;
    }

    public AccountController accountController() {
        return accountController;
    }

    public NotificationService notificationService() {
        return notificationService;
    }

    public Preferences preferencesRoot() {
        return preferencesRoot;
    }

    public void refreshFullAccountCenterUi() {
        refreshFullAccountCenterUi.run();
    }

    public Preferences preferencesFor(Account account) {
        return preferencesRoot.node(PREF_NODE_PREFIX + sanitizeNodeName(account.getEmail()));
    }

    private static String sanitizeNodeName(String input) {
        return input == null ? "unknown" : input.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase(Locale.ROOT);
    }
}
