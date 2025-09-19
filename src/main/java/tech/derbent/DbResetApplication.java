package tech.derbent;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import tech.derbent.config.CSampleDataInitializer;

/** Standalone application for database reset operations. This application excludes all web/Vaadin components and focuses solely on database
 * operations. Usage: mvn clean compile spring-boot:run -Preset-db */
@SpringBootApplication
@ComponentScan (basePackages = {
		"tech.derbent.config", "tech.derbent.activities.service", "tech.derbent.activities.domain", "tech.derbent.users.service",
		"tech.derbent.users.domain", "tech.derbent.projects.service", "tech.derbent.projects.domain", "tech.derbent.companies.service",
		"tech.derbent.companies.domain", "tech.derbent.meetings.service", "tech.derbent.meetings.domain", "tech.derbent.comments.service",
		"tech.derbent.comments.domain", "tech.derbent.risks.service", "tech.derbent.risks.domain", "tech.derbent.decisions.service",
		"tech.derbent.decisions.domain", "tech.derbent.orders.service", "tech.derbent.orders.domain", "tech.derbent.abstracts.service",
		"tech.derbent.abstracts.domain", "tech.derbent.abstracts.repository", "tech.derbent.screens.service", "tech.derbent.screens.domain",
		"tech.derbent.page.service", "tech.derbent.page.domain", "tech.derbent.gannt.service", "tech.derbent.gannt.domain",
		"tech.derbent.setup.service", "tech.derbent.setup.domain"
})
public class DbResetApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(DbResetApplication.class);

	/** Provides a Clock bean that can be used throughout the application.
	 * @return a Clock instance set to the system default time zone. */
	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}

	public static void main(String[] args) {
		LOGGER.info("Starting database reset application...");
		try {
			ConfigurableApplicationContext context = new SpringApplicationBuilder(DbResetApplication.class)
					.web(org.springframework.boot.WebApplicationType.NONE).profiles("reset-db").run(args);
			LOGGER.info("Performing database reset...");
			CSampleDataInitializer initializer = new CSampleDataInitializer();
			initializer.reloadForced();
			LOGGER.info("Database reset completed successfully");
			context.close();
			System.exit(0);
		} catch (Exception e) {
			LOGGER.error("Database reset failed", e);
			System.exit(1);
		}
	}
}
