package tech.derbent.api.session.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import jakarta.annotation.PostConstruct;
import tech.derbent.api.session.service.CLayoutService;
import tech.derbent.api.session.service.CWebSessionService;

/** Configuration class to handle the circular dependency between SessionService and LayoutService. */
@Configuration
@Profile ("!reset-db")
public class SessionConfiguration {

	private final CWebSessionService sessionService;
	private final CLayoutService layoutService;

	public SessionConfiguration(final CWebSessionService sessionService, final CLayoutService layoutService) {
		this.sessionService = sessionService;
		this.layoutService = layoutService;
	}

	@PostConstruct
	public void configureServices() {
		sessionService.setLayoutService(layoutService);
	}
}
