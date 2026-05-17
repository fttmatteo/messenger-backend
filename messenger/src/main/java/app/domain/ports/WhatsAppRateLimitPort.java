package app.domain.ports;

/**
 * Puerto para manejar el rate limiting de intentos de PIN en WhatsApp.
 */
public interface WhatsAppRateLimitPort {
    boolean isBlocked(String phoneNumber);

    int recordFailedAttempt(String phoneNumber);

    void clearFailedAttempts(String phoneNumber);
}
