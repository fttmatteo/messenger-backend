package app.domain.model.auth;

/**
 * Respuesta para la solicitud de token temporal de WebSocket.
 */
public class WsTokenResponse {
    private String wsToken;

    public WsTokenResponse() {
    }

    public WsTokenResponse(String wsToken) {
        this.wsToken = wsToken;
    }

    public String getWsToken() {
        return wsToken;
    }

    public void setWsToken(String wsToken) {
        this.wsToken = wsToken;
    }
}
