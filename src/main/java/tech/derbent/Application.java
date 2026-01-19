package tech.derbent;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import tech.derbent.api.config.CBabComponentScanConfig;
import tech.derbent.api.config.CDerbentComponentScanConfig;

/** Main application class for the Derbent project management system.
 * <p>
 * This is a Spring Boot application that provides a collaborative project management platform built with Vaadin, inspired by Jira and ProjeQtOr. It's
 * designed for small to medium-sized offices.
 * </p>
 * <p>
 * Key features include:
 * </p>
 * <ul>
 * <li>Project and activity management</li>
 * <li>User and company administration</li>
 * <li>Meeting scheduling and management</li>
 * <li>Risk tracking and assessment</li>
 * <li>Collaborative workspaces</li>
 * </ul>
 * @see AppShellConfigurator
 * @since 1.0 */
@SpringBootConfiguration
@EnableAutoConfiguration
@Theme ("default")
@SuppressWarnings ("static-method")
@Import ({
		ServletWebServerFactoryAutoConfiguration.class, CDerbentComponentScanConfig.class, CBabComponentScanConfig.class
})
public class Application implements AppShellConfigurator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	private static final long serialVersionUID = 1L;
	/** Application startup time captured for performance metrics */
	public static final long startTime = System.nanoTime();

	/** Main entry point for the Derbent application.
	 * @param args command line arguments passed to the application */
	public static void main(final String[] args) {
		try {
			final SpringApplication app = new SpringApplication(Application.class);
			app.run(args);
		} catch (final Throwable e) {
			if (e.getClass().getName().contains("SilentExitException")) {
				LOGGER.debug("Spring is restarting the main thread - See spring-boot-devtools");
			} else {
				LOGGER.error("Application crashed!", e);
			}
		}
	}
	/** Provides a Clock bean that can be used throughout the application for time-related operations.
	 * @return a Clock instance set to the system default time zone */
	// @Bean
	// public Clock clock() {
	// return Clock.systemDefaultZone();
	// }

	/** Provides data initialization capabilities for the application.
	 * <p>
	 * This runner checks if initial data needs to be loaded into the database and executes the data.sql script if the user table is empty.
	 * </p>
	 * @param jdbcTemplate the JDBC template for database operations
	 * @return ApplicationRunner that handles data initialization */
	@SuppressWarnings ("unused")
	@Bean
	public ApplicationRunner dataInitializer(final JdbcTemplate jdbcTemplate) {
		return event -> {
			try {
				final Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cuser", Integer.class);
				// Data initialization is currently disabled (count < 0 condition)
				if (count != null && count == 0 && count < 0) {
					final String sql = StreamUtils.copyToString(new ClassPathResource("data.sql").getInputStream(), StandardCharsets.UTF_8);
					jdbcTemplate.execute(sql);
					LOGGER.info("Initial data loaded successfully");
				}
			} catch (final Exception ex) {
				// Table might not exist yet if Hibernate hasn't created it
				LOGGER.debug("Could not query cuser table - table may not exist yet: {}", ex.getMessage());
			}
		};
	}
}
