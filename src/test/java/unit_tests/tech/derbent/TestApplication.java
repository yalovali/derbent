package unit_tests.tech.derbent;

import org.springframework.boot.SpringApplication;
import tech.derbent.Application;

/** Run this application class to start your application locally, using Testcontainers for all external services. You have to configure the containers
 * in {@link TestcontainersConfiguration}. */
public class TestApplication {

	public static void main(final String[] args) {
		SpringApplication.from(Application::main).with(TestcontainersConfiguration.class).run(args);
	}
}
