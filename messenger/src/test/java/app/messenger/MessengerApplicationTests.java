package app.messenger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MessengerApplicationTests {

	@Test
	/**
	 * Verifica que el contexto de Spring Boot cargue correctamente sin errores.
	 */
	void contextLoads() {
	}

}
