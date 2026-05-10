package com.sixstars.app;

import java.awt.CardLayout;

import javax.swing.JPanel;

import com.sixstars.controller.AccountController;

/**
 * Cross-page session actions (logout, etc.).
 */
public final class AppSession {

    private AppSession() {
    }

    /** Persists cart state, clears session, refreshes chrome, navigates home. */
    public static void logout(JPanel pages, CardLayout cardLayout) {
        if (Main.shopPage != null) {
            Main.shopPage.persistCurrentCart();
            Main.shopPage.clearTransientCart();
        }
        AccountController.currentAccount = null;
        if (Main.headerBar != null) {
            Main.headerBar.refreshInfo();
        }
        if (Main.accountCenterPage != null) {
            Main.accountCenterPage.refreshInfo();
        }
        cardLayout.show(pages, "home");
    }
}
