package tech.derbent.api.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@SuppressWarnings ("static-method")
	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}
}
