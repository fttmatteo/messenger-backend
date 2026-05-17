package app.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import com.redis.testcontainers.RedisContainer;

@SuppressWarnings("resource")
public abstract class BaseContainerTest {

    protected static final MySQLContainer<?> mysql;
    protected static final RedisContainer redis;

    static {
        mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("messenger_db_test")
                .withUsername("root")
                .withPassword("test")
                .withReuse(true);

        redis = new RedisContainer(DockerImageName.parse("redis:7.2-alpine"))
                .withReuse(true);

        Startables.deepStart(mysql, redis).join();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getFirstMappedPort().toString());
    }
}
