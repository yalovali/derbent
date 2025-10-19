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
import tech.derbent.config.CDataInitializer;
import tech.derbent.session.service.ISessionService;

@SpringBootApplication (scanBasePackages = {
		"tech.derbent.abstracts", "tech.derbent.activities", "tech.derbent.comments", "tech.derbent.companies", "tech.derbent.decisions",
		"tech.derbent.gannt", "tech.derbent.meetings", "tech.derbent.orders", "tech.derbent.page", "tech.derbent.projects", "tech.derbent.risks",
		"tech.derbent.screens", "tech.derbent.users", "tech.derbent.config", "tech.derbent.session"
})
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
				CDataInitializer initializer = new CDataInitializer(sessionService);
				// Check if database needs reset
				if (initializer.isDatabaseEmpty()) {
					LOGGER.info("Database is empty, loading sample data...");
					initializer.loadSampleData(false);
				} else {
					LOGGER.info("Database has data, performing forced reset...");
					initializer.reloadForced(false);
				}
				LOGGER.info("Database reset completed successfully!");
			} catch (Exception e) {
				LOGGER.error("Error during database reset: ", e);
				System.exit(1);
			}
			System.exit(0);
		}
	}
}
