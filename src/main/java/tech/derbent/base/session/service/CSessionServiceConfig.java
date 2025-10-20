package tech.derbent.base.session.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/** Configuration to provide the correct session service bean for different environments. This ensures that the primary ISessionService bean is
 * correctly selected based on the active profile. */
@Configuration
public class CSessionServiceConfig {

	/** Provides the CWebSessionService as the primary ISessionService implementation for web applications. This bean is automatically selected when
	 * the web application context is active and the reset-db profile is not active.
	 * @param webSessionService the web-based session service implementation
	 * @return the web session service as the primary ISessionService implementation */
	@Bean
	@Primary
	@ConditionalOnWebApplication
	@Profile ("!reset-db")
	public ISessionService primarySessionService(final CWebSessionService webSessionService) {
		return webSessionService;
	}
}
