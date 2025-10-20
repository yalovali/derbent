package tech.derbent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/** Simple database reset application that runs minimal Spring context just to initialize database with sample data. */
@SpringBootApplication (exclude = {
		// Exclude web and Vaadin components
		org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class,
		org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration.class,
		org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration.class,
		org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration.class,
		org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
}, scanBasePackages = {
		// Only include essential service packages for database operations
		"tech.derbent.api.config",
		// Core entity services needed for database reset
		"tech.derbent.activities.service", "tech.derbent.app.comments.service", "tech.derbent.app.companies.service", "tech.derbent.app.decisions.service",
		"tech.derbent.app.meetings.service", "tech.derbent.app.orders.service", "tech.derbent.app.projects.service", "tech.derbent.app.risks.service",
		"tech.derbent.base.users.service",
		// Additional services that might be needed by CDataInitializer
		"tech.derbent.app.page.service", "tech.derbent.api.screens.service", "tech.derbent.app.gannt.service", "tech.derbent.administration.service",
		"tech.derbent.base.setup.service",
		// Session service is needed by activity priority service
		"tech.derbent.base.session.service"
		// Explicitly exclude: tech.derbent.base.login, tech.derbent.api.ui, tech.derbent.api.services
})
@EntityScan ("tech.derbent")
@EnableJpaRepositories ("tech.derbent")
public class SimpleDbResetApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDbResetApplication.class);

	public static void main(String[] args) {
		// Set properties to disable web components entirely
		System.setProperty("spring.main.web-application-type", "none");
		System.setProperty("vaadin.autoconfigure.enabled", "false");
		System.setProperty("spring.jmx.enabled", "false");
		System.setProperty("management.endpoints.enabled", "false");
		SpringApplication app = new SpringApplication(SimpleDbResetApplication.class);
		app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
		try {
			@SuppressWarnings ("unused")
			ConfigurableApplicationContext context = app.run(args);
			LOGGER.info("Database reset completed successfully!");
			System.exit(0);
		} catch (Exception e) {
			LOGGER.error("Error during database reset: ", e);
			System.exit(1);
		}
	}
}
