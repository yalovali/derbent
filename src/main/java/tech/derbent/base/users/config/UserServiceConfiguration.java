package tech.derbent.base.users.config;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.service.CUserService;

/** Configuration class to handle the circular dependency between UserService and SessionService. */
@Configuration
public class UserServiceConfiguration {

	private final ISessionService sessionService;
	private final CUserService userService;

	public UserServiceConfiguration(final CUserService userService, final ISessionService sessionService) {
		this.userService = userService;
		this.sessionService = sessionService;
	}

	@PostConstruct
	public void configureServices() {
		userService.setSessionService(sessionService);
		// LOGGER.info("UserServiceConfiguration: Successfully configured CUserService with CSessionService");
	}
}
