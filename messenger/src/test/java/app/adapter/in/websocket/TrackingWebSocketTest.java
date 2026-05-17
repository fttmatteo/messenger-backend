package app.adapter.in.websocket;

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

import app.support.AbstractIntegrationTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Pruebas unitarias de TrackingWebSocket")
class TrackingWebSocketTest extends AbstractIntegrationTest {

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

    @Autowired
    private org.springframework.boot.test.web.client.TestRestTemplate restTemplate;

    @Test
    @DisplayName("Debe conectar y suscribirse exitosamente")

    void shouldConnectAndSubscribeSuccessfully() throws Exception {
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

        assertTrue(session.isConnected(), "WebSocket session should be connected");

        StompSession.Subscription subscription = session.subscribe("/topic/tracking/all",
                new StompSessionHandlerAdapter() {
                });
        assertNotNull(subscription, "Subscription should be created");

        session.disconnect();
    }

    @Test
    @DisplayName("Debe conectar con cookie exitosamente")

    /**
     * Verifica que el WebSocket se conecte correctamente usando una cookie.
     */
    void shouldConnectWithCookieSuccessfully() throws Exception {
        app.domain.model.auth.AuthCredentials credentials = new app.domain.model.auth.AuthCredentials();
        credentials.setDocument(87654321L);
        String token = jwtAdapter.authenticate(credentials, "MESSENGER", 2L).getToken();

        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add("Cookie", "accessToken=" + token);

        StompHeaders stompHeaders = new StompHeaders();

        String url = "ws://localhost:" + port + "/ws/tracking";
        StompSession session = stompClient
                .connectAsync(url, httpHeaders, stompHeaders, new StompSessionHandlerAdapter() {
                })
                .get(10, TimeUnit.SECONDS);

        assertTrue(session.isConnected(), "WebSocket session should be connected via cookie");

        session.disconnect();
    }

    @Test
    @DisplayName("Debe conectar con token WS desde endpoint exitosamente")

    /**
     * Verifica que el WebSocket se conecte correctamente usando un token WS
     * obtenido desde el endpoint.
     */
    void shouldConnectWithWsTokenFromEndpointSuccessfully() throws Exception {
        app.domain.model.auth.AuthCredentials credentials = new app.domain.model.auth.AuthCredentials();
        credentials.setDocument(55554444L);
        String sessionToken = jwtAdapter.authenticate(credentials, "MESSENGER", 3L).getToken();

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Cookie", "accessToken=" + sessionToken);
        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

        org.springframework.http.ResponseEntity<app.domain.model.auth.WsTokenResponse> response = restTemplate
                .postForEntity(
                        "/auth/ws-token", entity, app.domain.model.auth.WsTokenResponse.class);

        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        String wsToken = response.getBody().getWsToken();
        assertNotNull(wsToken);

        WebSocketHttpHeaders wsHttpHeaders = new WebSocketHttpHeaders();
        wsHttpHeaders.add("Authorization", "Bearer " + wsToken);
        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", "Bearer " + wsToken);

        String url = "ws://localhost:" + port + "/ws/tracking";
        StompSession session = stompClient
                .connectAsync(url, wsHttpHeaders, stompHeaders, new StompSessionHandlerAdapter() {
                })
                .get(10, TimeUnit.SECONDS);

        assertTrue(session.isConnected(), "WebSocket session should be connected via WS Token");
        session.disconnect();
    }

    @Test
    @DisplayName("Debe conectar con token WS en parámetro de consulta exitosamente")

    /**
     * Verifica que el WebSocket se conecte correctamente usando un token WS en el
     * parámetro de consulta.
     */
    void shouldConnectWithWsTokenInQueryParamSuccessfully() throws Exception {
        app.domain.model.auth.AuthCredentials credentials = new app.domain.model.auth.AuthCredentials();
        credentials.setDocument(11223344L);
        String wsToken = jwtAdapter.authenticate(credentials, "MESSENGER", 4L).getToken();

        WebSocketHttpHeaders wsHttpHeaders = new WebSocketHttpHeaders();
        StompHeaders stompHeaders = new StompHeaders();

        String url = "ws://localhost:" + port + "/ws/tracking?token=" + wsToken;

        StompSession session = stompClient
                .connectAsync(url, wsHttpHeaders, stompHeaders, new StompSessionHandlerAdapter() {
                })
                .get(10, TimeUnit.SECONDS);

        assertTrue(session.isConnected(), "WebSocket session should be connected via Query Token");
        session.disconnect();
    }
}
