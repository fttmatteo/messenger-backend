package app.domain.ports;

/**
 * Puerto para manejar el rate limiting de intentos de PIN en WhatsApp.
 */
public interface WhatsAppRateLimitPort {

    /**
     * Verifica si un número de teléfono está bloqueado.
     */
    boolean isBlocked(String phoneNumber);

    /**
     * Registra un intento fallido y retorna los intentos restantes (sobre 3).
     * Si llega a 3, bloquea el número por 15 minutos.
     */
    int recordFailedAttempt(String phoneNumber);

    /**
     * Limpia los intentos fallidos al autenticarse exitosamente.
     */
    void clearFailedAttempts(String phoneNumber);
}
