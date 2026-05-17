package app.support;

import org.junit.jupiter.api.DisplayName;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public abstract 
@DisplayName("Pruebas unitarias de Abstract Integration")
class AbstractIntegrationTest extends BaseContainerTest {
}
