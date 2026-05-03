package tech.derbent;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
// mvn clean compile spring-boot:run -Preset-db
// or
// ./mvnw clean compile spring-boot:run -Preset-db
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import tech.derbent.api.config.CDataInitializer;
import tech.derbent.api.session.service.ISessionService;

@SpringBootApplication
public class DbResetApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(DbResetApplication.class);

	public static void main(String[] args) {
		System.setProperty("spring.main.web-application-type", "none");
		ConfigurableApplicationContext context = SpringApplication.run(DbResetApplication.class, args);
		if (Arrays.asList(context.getEnvironment().getActiveProfiles()).contains("reset-db")) {
			try {
				LOGGER.info("Resetting database with initial data...");
				// Get session service bean from Spring context
				ISessionService sessionService = context.getBean(ISessionService.class);
				LOGGER.debug("Retrieved ISessionService bean from Spring context");
				final CDataInitializer initializer = new CDataInitializer(sessionService);
				initializer.reloadForcedExcel(false);
				LOGGER.info("Database reset completed successfully!");
			} catch (Exception e) {
				LOGGER.error("Error during database reset:  reason={}", e.getMessage());
				System.exit(1);
			}
			System.exit(0);
		}
	}
}
