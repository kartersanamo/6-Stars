package com.sixstars.service;

import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.AppNotification;
import com.sixstars.model.NotificationType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;

public class NotificationService {
    private static final NotificationService INSTANCE = new NotificationService();
    private static final String PREF_NODE_PREFIX = "notification-preferences-";

    private final Map<String, List<AppNotification>> notificationStore = new HashMap<>();
    private final List<NotificationListener> listeners = new CopyOnWriteArrayList<>();
    private final Preferences preferencesRoot = Preferences.userNodeForPackage(NotificationService.class);

    public static NotificationService getInstance() {
        return INSTANCE;
    }

    public void publish(NotificationType type, String targetEmail, String message) {
        if (targetEmail == null || targetEmail.isBlank() || message == null || message.isBlank()) {
            return;
        }
        String normalizedEmail = targetEmail.trim().toLowerCase(Locale.ROOT);

        synchronized (notificationStore) {
            notificationStore.computeIfAbsent(normalizedEmail, _ -> new ArrayList<>())
                    .add(0, new AppNotification(type, message));
        }
        notifyListeners(normalizedEmail);
    }

    public void publishForCurrentAccount(NotificationType type, String message) {
        Account current = AccountController.currentAccount;
        if (current == null) {
            return;
        }
        publish(type, current.getEmail(), message);
    }

    public List<AppNotification> getNotifications(String email) {
        String normalizedEmail = normalize(email);
        if (normalizedEmail == null) {
            return List.of();
        }
        synchronized (notificationStore) {
            return new ArrayList<>(notificationStore.getOrDefault(normalizedEmail, List.of()));
        }
    }

    public int getUnreadCount(String email) {
        return getNotifications(email).size();
    }

    public void clearNotification(String email, String notificationId) {
        String normalizedEmail = normalize(email);
        if (normalizedEmail == null || notificationId == null || notificationId.isBlank()) {
            return;
        }
        synchronized (notificationStore) {
            List<AppNotification> notifications = notificationStore.get(normalizedEmail);
            if (notifications == null) {
                return;
            }
            notifications.removeIf(n -> notificationId.equals(n.getId()));
        }
        notifyListeners(normalizedEmail);
    }

    public void clearAll(String email) {
        String normalizedEmail = normalize(email);
        if (normalizedEmail == null) {
            return;
        }
        synchronized (notificationStore) {
            notificationStore.remove(normalizedEmail);
        }
        notifyListeners(normalizedEmail);
    }

    public boolean isEmailEnabled(String email, NotificationType type) {
        return preferencesFor(email).getBoolean(type.name() + ".email", true);
    }

    public boolean isInAppEnabled(String email, NotificationType type) {
        return preferencesFor(email).getBoolean(type.name() + ".inapp", true);
    }

    public void setEmailEnabled(String email, NotificationType type, boolean enabled) {
        preferencesFor(email).putBoolean(type.name() + ".email", enabled);
        notifyListeners(normalize(email));
    }

    public void setInAppEnabled(String email, NotificationType type, boolean enabled) {
        preferencesFor(email).putBoolean(type.name() + ".inapp", enabled);
        notifyListeners(normalize(email));
    }

    public Map<NotificationType, Boolean> getEmailPreferences(String email) {
        Map<NotificationType, Boolean> result = new EnumMap<>(NotificationType.class);
        for (NotificationType type : NotificationType.values()) {
            result.put(type, isEmailEnabled(email, type));
        }
        return result;
    }

    public Map<NotificationType, Boolean> getInAppPreferences(String email) {
        Map<NotificationType, Boolean> result = new EnumMap<>(NotificationType.class);
        for (NotificationType type : NotificationType.values()) {
            result.put(type, isInAppEnabled(email, type));
        }
        return result;
    }

    public void registerListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(String email) {
        if (email == null) {
            return;
        }
        for (NotificationListener listener : listeners) {
            listener.onNotificationsChanged(email);
        }
    }

    private Preferences preferencesFor(String email) {
        String normalized = normalize(email);
        if (normalized == null) {
            normalized = "unknown";
        }
        return preferencesRoot.node(PREF_NODE_PREFIX + normalized.replaceAll("[^a-zA-Z0-9._-]", "_"));
    }

    private String normalize(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public interface NotificationListener {
        void onNotificationsChanged(String email);
    }
}
