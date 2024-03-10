package com.example.domain.change;

import com.example.model.Change;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.data.connection.annotation.Connectable;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.websocket.WebSocketSession;
import jakarta.inject.Singleton;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ChangeNotificationService {
    private final Map<String, Set<WebSocketSession>> openSessionsPerTenant = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final DataSource dataSource;
    private static final Logger LOG = LoggerFactory.getLogger(ChangeNotificationService.class);

    public ChangeNotificationService(ObjectMapper objectMapper, DataSource dataSource) {
        this.objectMapper = objectMapper;
        this.dataSource = dataSource;
    }

    @Connectable
    public void processChange(String tenantId, String entityId, String entityType) {
        long timestampSeconds = System.currentTimeMillis() / 1000;
        Change change = new Change(tenantId, timestampSeconds, entityId, entityType);
        try (Connection connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);

                PreparedStatement preparedStatement = connection.prepareStatement("""
                        INSERT INTO change (entityId,entityType, timestamp, tenantId) VALUES (?,?,?,?);
                        """);
                preparedStatement.setString(1, change.entityId());
                preparedStatement.setString(2, change.entityType());
                preparedStatement.setLong(3, change.timestamp());
                preparedStatement.setString(4, change.tenantId());
                long updated = preparedStatement.executeUpdate();
                try (Statement stmt = connection.createStatement()) {
                    String json = objectMapper.writeValueAsString(change);
                    String notifyQuery = String.format("NOTIFY changes, '%s'", json);
                    stmt.execute(notifyQuery);
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addSession(String tenantId, WebSocketSession session, long replaySince) {
        openSessionsPerTenant.computeIfAbsent(tenantId, (k) -> ConcurrentHashMap.newKeySet()).add(session);
        replaySince(replaySince, tenantId, session);
    }

    public void removeSession(String tenantId, WebSocketSession session) {
        openSessionsPerTenant.computeIfAbsent(tenantId, (k) -> ConcurrentHashMap.newKeySet()).remove(session);
    }

    @EventListener
    public void onStartup(StartupEvent event) {
        Thread.ofVirtual().start(() -> {
            listen();
        });
    }

    @Connectable
    void listen() {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("LISTEN changes ");
            PGConnection pgconn = connection.unwrap(PGConnection.class);
            while (!Thread.currentThread().isInterrupted()) {
                PGNotification[] nts = pgconn.getNotifications(10_000);
                if (nts == null || nts.length == 0) {
                    continue;
                }
                for (PGNotification nt : nts) {
                    String json = nt.getParameter();
                    String tenantId = objectMapper.readValue(json, Change.class).tenantId();
                    LOG.info("Publishing notification {}", json);
                    openSessionsPerTenant.getOrDefault(tenantId, Collections.emptySet())
                            .forEach(session -> session.sendAsync(json));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Connectable
    void replaySince(long since, String tenantId, WebSocketSession session) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT entityId, entityType,timestamp FROM change 
                    WHERE tenantId = ? AND timestamp > ?
                    """);
            preparedStatement.setString(1, tenantId);
            preparedStatement.setLong(2, since);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet != null) {
                while (resultSet.next()) {
                    String entityId = resultSet.getString(1);
                    String entityType = resultSet.getString(2);
                    long timestamp = resultSet.getLong(3);
                    Change change = new Change(tenantId, timestamp, entityId, entityType);
                    String json = objectMapper.writeValueAsString(change);
                    session.sendAsync(json);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
