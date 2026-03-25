package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication(excludeName = {
		"org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration" })
@EnableScheduling
@EnableCaching
@EnableAsync
public class MessengerApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
		SpringApplication.run(MessengerApplication.class, args);
	}
}
