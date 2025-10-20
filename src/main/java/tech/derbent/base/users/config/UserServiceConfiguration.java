package tech.derbent.base.users.config;

import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.service.CUserService;

/** Configuration class to handle the circular dependency between UserService and SessionService. */
@Configuration
public class UserServiceConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceConfiguration.class);
	private final CUserService userService;
	private final ISessionService sessionService;

	public UserServiceConfiguration(final CUserService userService, final ISessionService sessionService) {
		this.userService = userService;
		this.sessionService = sessionService;
	}

	@PostConstruct
	public void configureServices() {
		userService.setSessionService(sessionService);
		LOGGER.info("UserServiceConfiguration: Successfully configured CUserService with CSessionService");
	}
}
