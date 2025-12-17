package app.messenger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de integración que verifica que el contexto de Spring Boot
 * se carga correctamente con todas las configuraciones y beans.
 */
@SpringBootTest
@ActiveProfiles("test")
class MessengerApplicationTests {

	@Test
	void contextLoads() {
		// Este test verifica que el contexto de Spring se inicializa sin errores
	}

}
