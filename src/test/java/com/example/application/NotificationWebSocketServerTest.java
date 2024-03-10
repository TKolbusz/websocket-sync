package com.example.application;

import com.example.domain.change.ChangeNotificationService;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.websocket.WebSocketClient;
import io.micronaut.websocket.annotation.ClientWebSocket;
import io.micronaut.websocket.annotation.OnMessage;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.awaitility.Awaitility.await;

@MicronautTest
class NotificationWebSocketServerTest {

    @Inject
    BeanContext beanContext;
    @Inject
    ChangeNotificationService changeNotificationService;

    @Inject
    EmbeddedServer embeddedServer;

    @ClientWebSocket
    static abstract class TestWebSocketClient implements AutoCloseable {

        private final Deque<String> messageHistory = new ConcurrentLinkedDeque<>();

        public String getLatestMessage() {
            return messageHistory.peekLast();
        }

        public List<String> getMessagesChronologically() {
            return new ArrayList<>(messageHistory);
        }

        @OnMessage
        void onMessage(String message) {
            messageHistory.add(message);
        }

        abstract void send(@NonNull String message);
    }

    private TestWebSocketClient createWebSocketClient(int port, String tenantId) {
        WebSocketClient webSocketClient = beanContext.getBean(WebSocketClient.class);
        URI uri = UriBuilder.of("ws://localhost")
                .port(port)
                .path("{tenantId}")
                .path("notifications")
                .expand(CollectionUtils.mapOf("tenantId", tenantId));
        Publisher<TestWebSocketClient> client = webSocketClient.connect(TestWebSocketClient.class, uri);
        return Flux.from(client).blockFirst();
    }


    @Test
    void testWebsockerServer() throws Exception {
        String tenant1 = "tenant1";
        String tenant2 = "tenant2";
        TestWebSocketClient tenant1Client = createWebSocketClient(embeddedServer.getPort(), tenant1);
        TestWebSocketClient tenant2Client = createWebSocketClient(embeddedServer.getPort(), tenant2);

        changeNotificationService.processChange(tenant1, "1", "RESERVATION");
        changeNotificationService.processChange(tenant2, "2", "RESERVATION");

        await().until(() -> {
            List<String> messagesChronologically = tenant1Client.getMessagesChronologically();
            return messagesChronologically.size() == 1 &&
                    messagesChronologically.get(0).contains("RESERVATION") && messagesChronologically.get(0).contains("1");
        });

        await().until(() -> {
            List<String> messagesChronologically = tenant1Client.getMessagesChronologically();
            return messagesChronologically.size() == 1 &&
                    messagesChronologically.get(0).contains("RESERVATION") && messagesChronologically.get(0).contains("2");
        });

        tenant1Client.close();
        tenant2Client.close();
    }
}

