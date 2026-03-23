package com.sixstars.logicClasses;

import java.util.Arrays;

/**
 * Validates admin credentials. Default demo account: username {@code admin}, password {@code admin}.
 */
public class AdminAuth {
    private final String adminUsername;
    private final char[] adminPassword;

    public AdminAuth() {
        this("admin", "admin".toCharArray());
    }

    public AdminAuth(String adminUsername, char[] adminPassword) {
        this.adminUsername = adminUsername;
        this.adminPassword = Arrays.copyOf(adminPassword, adminPassword.length);
    }

    public boolean authenticate(String username, char[] password) {
        if (username == null || password == null) {
            return false;
        }
        boolean userOk = adminUsername.equals(username.trim());
        boolean passOk = Arrays.equals(adminPassword, password);
        Arrays.fill(password, '\0');
        return userOk && passOk;
    }
}
