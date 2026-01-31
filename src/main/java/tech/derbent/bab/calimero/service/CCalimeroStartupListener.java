package tech.derbent.bab.calimero.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** CCalimeroStartupListener - Listens for application startup and starts Calimero service if enabled.
 * <p>
 * This component:
 * <ul>
 * <li>Listens for Spring ApplicationReadyEvent</li>
 * <li>Triggers CCalimeroProcessManager to start Calimero service</li>
 * <li>Only activates when 'bab' profile is active</li>
 * </ul>
 * <p>
 * The service will start after all Spring beans are initialized and the application is fully ready. */
@Component
@Profile("bab")
public class CCalimeroStartupListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCalimeroStartupListener.class);
	private final CCalimeroProcessManager processManager;

	public CCalimeroStartupListener(final CCalimeroProcessManager processManager) {
		this.processManager = processManager;
	}

	/** Handle application ready event - start Calimero service if enabled.
	 * @param event application ready event */
	@EventListener
	public void onApplicationReady(final ApplicationReadyEvent event) {
		LOGGER.info("Application ready - checking Calimero service configuration");

		final CCalimeroServiceStatus status = processManager.startCalimeroServiceIfEnabled();

		if (status.isEnabled() && status.isRunning()) {
			LOGGER.info("Calimero service started successfully on application startup");
		} else if (!status.isEnabled()) {
			LOGGER.info("Calimero service is disabled or not configured");
		} else {
			LOGGER.warn("Failed to start Calimero service - {}", status.getMessage());
		}
	}
}
