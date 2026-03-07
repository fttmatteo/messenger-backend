package app.support;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.TestConfiguration;
import java.util.TimeZone;

@TestConfiguration
/**
 * Clase de configuración para tests que requieren configuración adicional.
 */
public class TestConfig {

    @PostConstruct
    /**
     * Configura la zona horaria para tests.
     */
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
    }
}
