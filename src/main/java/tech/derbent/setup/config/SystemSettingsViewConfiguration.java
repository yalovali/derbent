package tech.derbent.setup.config;

import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import tech.derbent.session.service.ISessionService;
import tech.derbent.setup.view.CSystemSettingsView;

/** Configuration class to inject session service into CSystemSettingsView. This ensures the view has access to session service for database reset
 * functionality. */
@Configuration
public class SystemSettingsViewConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemSettingsViewConfiguration.class);
	private final CSystemSettingsView systemSettingsView;
	private final ISessionService sessionService;

	public SystemSettingsViewConfiguration(final CSystemSettingsView systemSettingsView, final ISessionService sessionService) {
		this.systemSettingsView = systemSettingsView;
		this.sessionService = sessionService;
	}

	@PostConstruct
	public void configureView() {
		systemSettingsView.setSessionService(sessionService);
		LOGGER.info("SystemSettingsViewConfiguration: Successfully configured CSystemSettingsView with ISessionService");
	}
}
