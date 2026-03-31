package app.infrastructure.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integración para auditar los headers de seguridad.
 * Verifica que el sistema cumpla con estándares profesionales de seguridad Web.
 */
@AutoConfigureMockMvc
@DisplayName("Security Headers Audit Integration Tests")
class SecurityHeadersIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should contain standard security headers on performance monitoring endpoint")
    void shouldContainSecurityHeaders() throws Exception {
        mockMvc.perform(get("/settings/status-colors"))
                .andExpect(status().isOk())
                // Cache-Control: no-store para evitar cacheo de datos de monitoreo
                .andExpect(header().string("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
                // X-Content-Type-Options: nosniff para prevenir sniffing de tipo de contenido
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                // X-Frame-Options: DENY para prevenir Clickjacking
                .andExpect(header().string("X-Frame-Options", "DENY"))
                // X-XSS-Protection: 1; mode=block para activar protección XSS del navegador
                .andExpect(header().string("X-XSS-Protection", "1; mode=block"))
                // Pragma: no-cache para compatibilidad con HTTP/1.0
                .andExpect(header().string("Pragma", "no-cache"));
    }
}
