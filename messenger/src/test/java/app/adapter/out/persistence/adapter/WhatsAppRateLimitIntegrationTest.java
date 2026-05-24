package app.adapter.out.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@DisplayName("Pruebas unitarias de WhatsAppRateLimit Integration")
class WhatsAppRateLimitIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RedisWhatsAppRateLimitAdapter rateLimitAdapter;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String PHONE_NUMBER = "573111111111";

    @BeforeEach
    void setUp() {
        rateLimitAdapter.clearFailedAttempts(PHONE_NUMBER);
        redisTemplate.delete("wa:pin:block:" + PHONE_NUMBER);
    }

    @Test
    @DisplayName("Debe bloquear después de los intentos máximos")

    void shouldBlockAfterMaxAttempts() {
        assertFalse(rateLimitAdapter.isBlocked(PHONE_NUMBER), "No debe estar bloqueado al inicio");

        int remaining1 = rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        assertEquals(2, remaining1, "Deben quedar 2 intentos");
        assertFalse(rateLimitAdapter.isBlocked(PHONE_NUMBER), "No debe estar bloqueado tras 1 intento");

        int remaining2 = rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        assertEquals(1, remaining2, "Debe quedar 1 intento");
        assertFalse(rateLimitAdapter.isBlocked(PHONE_NUMBER), "No debe estar bloqueado tras 2 intentos");

        int remaining3 = rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        assertEquals(0, remaining3, "Deben quedar 0 intentos");
        assertTrue(rateLimitAdapter.isBlocked(PHONE_NUMBER), "DEBE estar bloqueado tras 3 intentos");
    }

    @Test
    @DisplayName("Debe limpiar intentos")

    void shouldClearAttempts() {
        rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        
        rateLimitAdapter.clearFailedAttempts(PHONE_NUMBER);
        
        int remaining = rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        assertEquals(2, remaining, "Los intentos deberían haberse reseteado");
    }

    @Test
    @DisplayName("Debe verificar la clave de bloqueo en Redis")

    void shouldVerifyRedisBlockKey() {
        for (int i = 0; i < 3; i++) {
            rateLimitAdapter.recordFailedAttempt(PHONE_NUMBER);
        }

        String blockKey = "wa:pin:block:" + PHONE_NUMBER;
        assertTrue(redisTemplate.hasKey(blockKey), "La clave de bloqueo debe existir en Redis");
        
        redisTemplate.delete(blockKey);
        assertFalse(rateLimitAdapter.isBlocked(PHONE_NUMBER), "El bloqueo debe desaparecer si se borra la clave");
    }
}
