package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Messenger Backend.
 * Esta es la clase de entrada que inicia la aplicación Spring Boot.
 * Gestiona el sistema de entregas de placas vehiculares, permitiendo
 * el registro, tracking y gestión de servicios de mensajería.
 */

@SpringBootApplication
public class MessengerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessengerApplication.class, args);
	}
}
