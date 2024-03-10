package com.example.application;

import com.example.domain.change.ChangeNotificationService;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerWebSocket("/{tenantId}/notifications")
public class NotificationWebSocketServer {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationWebSocketServer.class);

    private final ChangeNotificationService changeNotificationService;

    public NotificationWebSocketServer(ChangeNotificationService changeNotificationService) {
        this.changeNotificationService = changeNotificationService;
    }

    @OnOpen
    public void onOpen(String tenantId, WebSocketSession session) {
        String sinceStr = session.getRequestParameters().get("since");
        long sinceTimestamp = sinceStr != null ? Long.parseLong(sinceStr) : 0;
        log("onOpen " + sinceTimestamp, session, tenantId);
        changeNotificationService.addSession(tenantId, session);

        // TODO broadcast all events for a given `since` timestamp
    }

    @OnMessage
    public void onMessage(
            String tenantId,
            String message,
            WebSocketSession session) {
        log("onMessage", session, tenantId);
    }

    @OnClose
    public void onClose(
            String tenantId,
            WebSocketSession session) {
        log("onClose", session, tenantId);
        changeNotificationService.removeSession(tenantId, session);
    }

    private void log(String event, WebSocketSession session, String tenantId) {
        LOG.info("* WebSocket: {} received for session {} from '{}'", event, session.getId(), tenantId);
    }
}
