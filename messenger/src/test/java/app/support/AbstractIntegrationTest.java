package app.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Clase base para tests de integración de contexto completo (@SpringBootTest).
 * Hereda la infraestructura de contenedores de BaseContainerTest.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest extends BaseContainerTest {
}
