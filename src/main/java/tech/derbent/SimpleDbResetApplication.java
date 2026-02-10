package tech.derbent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/** Simple database reset application that runs minimal Spring context just to initialize database with sample data. */
@SpringBootApplication (exclude = {
		// Exclude web and Vaadin components
		WebMvcAutoConfiguration.class, ServletWebServerFactoryAutoConfiguration.class, SecurityAutoConfiguration.class,
		HttpEncodingAutoConfiguration.class, MultipartAutoConfiguration.class, ErrorMvcAutoConfiguration.class,
		WebSocketServletAutoConfiguration.class
}, scanBasePackages = {
		// Only include essential service packages for database operations
		"tech.derbent.api.config",
		// Core entity services needed for database reset
		"tech.derbent.activities.service", "tech.derbent.plm.comments.service", "tech.derbent.api.companies.service",
		"tech.derbent.plm.decisions.service", "tech.derbent.plm.meetings.service", "tech.derbent.plm.orders.service",
		"tech.derbent.api.projects.service", "tech.derbent.api.roles.service", "tech.derbent.plm.risks.risk.service",
		"tech.derbent.api.users.service",
		// Additional services that might be needed by CDataInitializer
		"tech.derbent.api.page.service", "tech.derbent.api.screens.service", "tech.derbent.plm.gannt.service", "tech.derbent.administration.service",
		"tech.derbent.api.setup.service",
		// Session service is needed by activity priority service
		"tech.derbent.api.session.service"
		// Explicitly exclude: tech.derbent.api.authentication, tech.derbent.api.ui, tech.derbent.api.services
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
		final SpringApplication app = new SpringApplication(SimpleDbResetApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE);
		try {
			app.run(args);
			LOGGER.info("Database reset completed successfully!");
			System.exit(0);
		} catch (final Exception e) {
			LOGGER.error("Error during database reset: ", e);
			System.exit(1);
		}
	}
}
