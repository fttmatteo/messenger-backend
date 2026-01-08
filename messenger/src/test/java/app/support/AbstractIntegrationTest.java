package app.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

        @Container
        @ServiceConnection
        @SuppressWarnings("resource")
        static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
                        .withDatabaseName("messenger_db_test")
                        .withUsername("test")
                        .withPassword("test");

        @Container
        @ServiceConnection(name = "redis")
        @SuppressWarnings("resource")
        static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
                        .withExposedPorts(6379);
}
