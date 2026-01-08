package app.adapter.in.websocket;

import app.domain.model.enums.TrackingStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Tracking WebSocket Integration Tests")
class TrackingWebSocketTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        this.stompClient = new WebSocketStompClient(new SockJsClient(transports));
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        this.stompClient.setMessageConverter(converter);
    }

    @Autowired
    private app.adapter.out.security.JwtAdapter jwtAdapter;

    @Test
    @DisplayName("Should successfully connect to WebSocket and subscribe to tracking topic")
    void shouldConnectAndSubscribeSuccessfully() throws Exception {
        // Generate token for a test messenger
        app.domain.model.auth.AuthCredentials credentials = new app.domain.model.auth.AuthCredentials();
        credentials.setDocument(12345678L);
        String token = jwtAdapter.authenticate(credentials, "MESSENGER", 1L).getToken();

        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + token);

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", "Bearer " + token);

        String url = "ws://localhost:" + port + "/ws/tracking";
        StompSession session = stompClient
                .connectAsync(url, httpHeaders, stompHeaders, new StompSessionHandlerAdapter() {
                })
                .get(10, TimeUnit.SECONDS);

        // Verify connection
        assertTrue(session.isConnected(), "WebSocket session should be connected");

        // Subscribe to topic (verify subscription doesn't throw)
        StompSession.Subscription subscription = session.subscribe("/topic/tracking/all",
                new StompSessionHandlerAdapter() {
                });
        assertNotNull(subscription, "Subscription should be created");

        // Clean up
        session.disconnect();
    }
}
