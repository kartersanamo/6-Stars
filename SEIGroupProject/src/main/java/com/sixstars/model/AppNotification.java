package com.sixstars.model;

import java.time.Instant;
import java.util.UUID;

public class AppNotification {
    private final String id;
    private final NotificationType type;
    private final String message;
    private final Instant createdAt;

    public AppNotification(NotificationType type, String message) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public NotificationType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
