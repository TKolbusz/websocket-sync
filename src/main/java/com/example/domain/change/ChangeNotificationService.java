package com.example.domain.change;

import com.example.model.Change;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ChangeNotificationService {
    private final Map<String, Set<WebSocketSession>> openSessionsPerTenant = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private static final Logger LOG = LoggerFactory.getLogger(ChangeNotificationService.class);

    public ChangeNotificationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void processChange(String tenantId, String entityId, String entityType) {
        Change notification = new Change(System.currentTimeMillis(), entityId, entityType);
        broadcastNotification(tenantId, notification);
    }

    public void addSession(String tenantId, WebSocketSession session) {
        openSessionsPerTenant.computeIfAbsent(tenantId, (k) -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void removeSession(String tenantId, WebSocketSession session) {
        openSessionsPerTenant.computeIfAbsent(tenantId, (k) -> ConcurrentHashMap.newKeySet()).remove(session);
    }

    private void broadcastNotification(String tenantId, Change notification) {
        LOG.info("Publishing notification {} for tenant {} ", notification, tenantId);
        String notificationJson;
        try {
            notificationJson = objectMapper.writeValueAsString(notification);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        openSessionsPerTenant.getOrDefault(tenantId, Collections.emptySet())
                .forEach(session -> session.sendAsync(notificationJson));
    }
}
