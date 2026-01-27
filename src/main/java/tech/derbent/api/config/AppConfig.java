package tech.derbent.api.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean
	Clock clock() {
		return Clock.systemDefaultZone();
	}
}
