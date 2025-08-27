package tech.derbent;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import tech.derbent.abstracts.components.CTimer;

@SpringBootApplication (scanBasePackages = "tech.derbent")
@Theme ("default")
public class Application implements AppShellConfigurator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	private static final long serialVersionUID = 1L;
	// capture startup time
	public static final long startTime = System.nanoTime();

	public static void main(final String[] args) {
		try {
			CTimer.stamp();
			// LOGGER.info("Hello world!");
			final SpringApplication app = new SpringApplication(Application.class);
			// BU ADD LISTNER CALISMIYOR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			app.addListeners((final ApplicationReadyEvent event) -> {
				final long endTime = System.nanoTime();
				final long durationMs = (endTime - startTime) / 1_000_000;
				LOGGER.info("Application started in {} ms", durationMs);
			});
			app.run(args);
			CTimer.print();
		} catch (final Throwable e) {
			if (e.getClass().getName().contains("SilentExitException")) {
				LOGGER.debug("Spring is restarting the main thread - See spring-boot-devtools");
			} else {
				LOGGER.error("Application crashed!", e);
			}
		}
	}

	/** Provides a Clock bean that can be used throughout the application.
	 * @return a Clock instance set to the system default time zone. */
	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone(); // You can also use Clock.systemUTC()
	}

	@Bean
	public ApplicationRunner dataInitializer(final JdbcTemplate jdbcTemplate) {
		return args -> {
			// Temporarily disable data initialization due to SQL syntax issues
			// LOGGER.info("Data initialization temporarily disabled");
			try {
				final Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cuser", Integer.class);
				// disabled by count <0
				if ((count != null) && (count == 0) && (count < 0)) {
					final String sql = StreamUtils.copyToString(new ClassPathResource("data.sql").getInputStream(), StandardCharsets.UTF_8);
					jdbcTemplate.execute(sql);
				}
			} catch (final Exception e) {
				// Table might not exist yet if Hibernate hasn't created it
				LOGGER.debug("Could not query cuser table - table may not exist yet: {}", e.getMessage());
			}
		};
	}
}
