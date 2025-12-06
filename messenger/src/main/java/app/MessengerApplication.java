package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MessengerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessengerApplication.class, args);
	}
	//INSERT INTO employees (document,full_name,password,phone,role,user_name,zone)
	//VALUES (1000000001,'nombre','A!123456789',3000000001,'ADMIN','ADMIN',null);
}
