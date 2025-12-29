package app;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication(excludeName = {
		"org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration" })
@EnableScheduling
public class MessengerApplication {

	@PostConstruct
	public void init() {
		// Force JVM timezone to Colombia (UTC-5)
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
	}

	public static void main(String[] args) {
		SpringApplication.run(MessengerApplication.class, args);
	}
}
