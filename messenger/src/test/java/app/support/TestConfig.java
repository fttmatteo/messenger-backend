package app.support;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.TestConfiguration;
import java.util.TimeZone;

@TestConfiguration
public class TestConfig {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
    }
}
