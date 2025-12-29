package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication(excludeName = {
		"org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration" })
@EnableScheduling
public class MessengerApplication {

	@PostConstruct
	public void init() {
		// Sincronizar toda la aplicación a la hora de Colombia
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
	}

	public static void main(String[] args) {
		SpringApplication.run(MessengerApplication.class, args);
	}
}
