package app.infrastructure.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Pruebas de integración para RedisWhatsAppRateLimitAdapter.
 * Verifica que el bloqueo de PIN funcione correctamente en Redis.
 */
@DisplayName("WhatsApp Rate Limit Integration Tests")
class WhatsAppRateLimitIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RedisWhatsAppRateLimitAdapter rateLimitAdapter;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String PHONE_NUMBER = "573111111111";

    @BeforeEach
    void setUp() {
        // Limpiar Redis antes de cada test para el número de prueba
        rateLimitAdapter.clearFailedAttempts(PHONE_NUMBER);
        redisTemplate.delete("wa:pin:block:" + PHONE_NUMBER);
    }

    @Test
    @DisplayName("Should block phone number after 3 failed attempts")
    void shouldBlockAfterMaxAttempts() {
        // El teléfono no debe estar bloqueado inicialmente
        assertFalse(rateLimitAdapter.isBlocked(PHONE_NUMBER), "No debe estar bloqueado al inicio");

        // Intento 1
        int remaining1 = rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        assertEquals(2, remaining1, "Deben quedar 2 intentos");
        assertFalse(rateLimitAdapter.isBlocked(PHONE_NUMBER), "No debe estar bloqueado tras 1 intento");

        // Intento 2
        int remaining2 = rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        assertEquals(1, remaining2, "Debe quedar 1 intento");
        assertFalse(rateLimitAdapter.isBlocked(PHONE_NUMBER), "No debe estar bloqueado tras 2 intentos");

        // Intento 3 - Aquí debería bloquearse
        int remaining3 = rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        assertEquals(0, remaining3, "Deben quedar 0 intentos");
        assertTrue(rateLimitAdapter.isBlocked(PHONE_NUMBER), "DEBE estar bloqueado tras 3 intentos");
    }

    @Test
    @DisplayName("Should clear failed attempts successfully")
    void shouldClearAttempts() {
        rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        
        rateLimitAdapter.clearFailedAttempts(PHONE_NUMBER);
        
        // El siguiente intento debería volver a decir que quedan 2
        int remaining = rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        assertEquals(2, remaining, "Los intentos deberían haberse reseteado");
    }

    @Test
    @DisplayName("Should respect block TTL (verification of Redis key existence)")
    void shouldVerifyRedisBlockKey() {
        // Forzar bloqueo
        for (int i = 0; i < 3; i++) {
            rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        }

        String blockKey = "wa:pin:block:" + PHONE_NUMBER;
        assertTrue(redisTemplate.hasKey(blockKey), "La clave de bloqueo debe existir en Redis");
        
        // Si borramos la clave manualmente, el adaptador debe decir que ya no está bloqueado
        redisTemplate.delete(blockKey);
        assertFalse(rateLimitAdapter.isBlocked(PHONE_NUMBER), "El bloqueo debe desaparecer si se borra la clave");
    }
}
