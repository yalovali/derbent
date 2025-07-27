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
/* build dizini altinda degilse, sprintboot app ilgili classlari gezip sistemleri yaratmiyor.
 *
 * Bu durumda, build dizini altinda olmasi icin, src/main/java/tech/derbent/Application.java
 * olarak konumlandirildi.
 *
 * yada su sekilde tanimlar yapilmali:
@ComponentScan(basePackages = "users.service")
@EnableJpaRepositories(basePackages = "users.service")
@EntityScan(basePackages = "users.domain")
 *
 */
//@Slf4j
//@ComponentScan(basePackages = "tech.derbent") // This is not needed as Spring Boot will scan the package of the main

@SpringBootApplication
@Theme ("default")
public class Application implements AppShellConfigurator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private static final long serialVersionUID = 1L;

	// capture startup time
	public static final long startTime = System.nanoTime();

	public static void main(final String[] args) {

		try {
			// LOGGER.info("Hello world!");
			final SpringApplication app = new SpringApplication(Application.class);
			// BU ADD LISTNER CALISMIYOR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			app.addListeners((final ApplicationReadyEvent event) -> {
				final long endTime = System.nanoTime();
				final long durationMs = (endTime - startTime) / 1_000_000;
				LOGGER.info("Application started in {} ms", durationMs);
			});
			app.run(args);
		} catch (final Throwable e) {

			if (e.getClass().getName().contains("SilentExitException")) {
				LOGGER.debug(
					"Spring is restarting the main thread - See spring-boot-devtools");
			}
			else {
				LOGGER.error("Application crashed!", e);
			}
		}
	}

	/**
	 * Provides a Clock bean that can be used throughout the application.
	 * @return a Clock instance set to the system default time zone.
	 */
	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone(); // You can also use Clock.systemUTC()
	}

	@Bean
	public ApplicationRunner dataInitializer(final JdbcTemplate jdbcTemplate) {
		return args -> {
			// Temporarily disable data initialization due to SQL syntax issues
			// LOGGER.info("Data initialization temporarily disabled");
			final Integer count =
				jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cuser", Integer.class);

			if ((count != null) && (count == 0)) {
				final String sql = StreamUtils.copyToString(
					new ClassPathResource("data.sql").getInputStream(),
					StandardCharsets.UTF_8);
				jdbcTemplate.execute(sql);
			}
		};
	}
}
