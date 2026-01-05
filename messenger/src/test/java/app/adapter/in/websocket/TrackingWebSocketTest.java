package app.adapter.in.websocket;

import app.adapter.in.rest.request.LiveTrackingRequest;
import app.adapter.in.rest.response.LiveTrackingResponse;
import app.domain.model.enums.TrackingStatus;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Tracking WebSocket Integration Tests")
class TrackingWebSocketTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @BeforeEach
    void setup() {
        List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        this.stompClient = new WebSocketStompClient(new SockJsClient(transports));
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(jsonMapper);
        this.stompClient.setMessageConverter(converter);
    }

    @Autowired
    private app.adapter.out.security.JwtAdapter jwtAdapter;

    @Test
    @DisplayName("Should receive tracking updates via WebSocket")
    void shouldReceiveTrackingUpdate() throws Exception {
        BlockingQueue<LiveTrackingResponse> blockingQueue = new LinkedBlockingDeque<>();
        Long testMessengerId = 999L;

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

        // Subscribe to relevant topic
        session.subscribe("/topic/tracking/all", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LiveTrackingResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((LiveTrackingResponse) payload);
            }
        });

        // Send an update through the WebSocket
        LiveTrackingRequest request = new LiveTrackingRequest();
        request.setMessengerId(testMessengerId);
        request.setLatitude(4.6);
        request.setLongitude(-74.0);
        request.setStatus(TrackingStatus.ACTIVE);

        session.send("/app/tracking/update", request);

        // Wait for the broadcast back
        LiveTrackingResponse received = blockingQueue.poll(15, TimeUnit.SECONDS);

        assertNotNull(session);
        assertNotNull(received, "Should have received a message within timeout");
        org.junit.jupiter.api.Assertions.assertEquals(testMessengerId, received.getMessengerId());

        session.disconnect();
    }
}
