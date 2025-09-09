package tech.derbent;

import java.util.Arrays;
import org.springframework.boot.SpringApplication;
// mvn clean compile spring-boot:run -Preset-db
// or
// ./mvnw clean compile spring-boot:run -Preset-db
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DbResetApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DbResetApplication.class, args);
		if (Arrays.asList(context.getEnvironment().getActiveProfiles()).contains("reset-db")) {
			System.exit(0);
		}
	}
}
