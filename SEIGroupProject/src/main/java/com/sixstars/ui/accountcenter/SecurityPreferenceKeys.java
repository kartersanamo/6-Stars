package com.sixstars.ui.accountcenter;

import java.util.Locale;
import java.util.prefs.Preferences;

import com.sixstars.model.Account;

/**
 * Keys and reads for security-related {@link Preferences} under the account-center node.
 */
public final class SecurityPreferenceKeys {

    public static final String NEW_DEVICE_EMAIL = "sec_alert_new_device_email";
    public static final String LOGIN_ALERT_IN_APP = "sec_login_alerts_in_app";
    public static final String REAUTH_SENSITIVE = "sec_require_reauth_sensitive";

    private SecurityPreferenceKeys() {
    }

    public static Preferences nodeForAccount(Preferences preferencesRoot, Account account) {
        return preferencesRoot.node(AccountCenterContext.PREF_NODE_PREFIX + sanitizeNodeName(account.getEmail()));
    }

    public static boolean isNewDeviceEmailEnabled(Preferences preferencesRoot, Account account) {
        return nodeForAccount(preferencesRoot, account).getBoolean(NEW_DEVICE_EMAIL, true);
    }

    public static boolean isLoginInAppAlertEnabled(Preferences preferencesRoot, Account account) {
        return nodeForAccount(preferencesRoot, account).getBoolean(LOGIN_ALERT_IN_APP, true);
    }

    public static boolean isReauthForSensitiveActions(Preferences preferencesRoot, Account account) {
        return nodeForAccount(preferencesRoot, account).getBoolean(REAUTH_SENSITIVE, true);
    }

    private static String sanitizeNodeName(String input) {
        return input == null ? "unknown" : input.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase(Locale.ROOT);
    }
}
