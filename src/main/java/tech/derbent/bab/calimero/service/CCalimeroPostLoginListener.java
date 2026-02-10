package tech.derbent.bab.calimero.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.vaadin.flow.server.VaadinSession;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.calimero.CCalimeroConstants;

/** CCalimeroPostLoginListener - Handles Calimero service startup after user login.
 * <p>
 * This component:
 * <ul>
 * <li>Starts Calimero service after user authentication when autostart is enabled</li>
 * <li>Respects user's autostart preference from login screen</li>
 * <li>Replaces ApplicationReadyEvent startup to follow proper login flow</li>
 * <li>Allows restart attempts when users enable autostart on subsequent logins</li>
 * <li>Only activates when 'bab' profile is active</li>
 * </ul>
 * <p>
 * The service starts when user enables autostart and Calimero is not running.
 * This ensures Calimero follows our user-controlled startup flow.
 * Once started, Calimero runs for all users until manually stopped. */
@Component
@Profile("bab")
public class CCalimeroPostLoginListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCalimeroPostLoginListener.class);
	
	// Session key for Calimero autostart preference (must match CCustomLoginView)

	
	// Thread safety for concurrent login attempts
	private static final Object startupLock = new Object();
	
	private final CCalimeroProcessManager processManager;
	private final ISessionService sessionService;

	public CCalimeroPostLoginListener(final CCalimeroProcessManager processManager,
									  final ISessionService sessionService) {
		this.processManager = processManager;
		this.sessionService = sessionService;
	}
	
	/**
	 * Check if user wants Calimero to autostart.
	 * This checks the VaadinSession for the preference set during login.
	 * 
	 * @return true if autostart is enabled (default), false if disabled by user
	 */
	private boolean shouldAutostartCalimero() {
		try {
			// Try to get preference from VaadinSession (set during login)
			final VaadinSession session = VaadinSession.getCurrent();
			if (session != null) {
				final Boolean autostartPreference = (Boolean) session.getAttribute(CCalimeroConstants.SESSION_KEY_AUTOSTART_CALIMERO);
				if (autostartPreference != null) {
					LOGGER.debug("🔧 Found user autostart preference: {}", autostartPreference);
					return autostartPreference.booleanValue();
				}
			}
			
			// Try to get preference from session service as fallback
			final Optional<Boolean> sessionPreferenceOpt = sessionService.getSessionValue(CCalimeroConstants.SESSION_KEY_AUTOSTART_CALIMERO);
			if (sessionPreferenceOpt.isPresent()) {
				final Boolean sessionPreference = sessionPreferenceOpt.get();
				LOGGER.debug("🔧 Found session service autostart preference: {}", sessionPreference);
				return sessionPreference.booleanValue();
			}
			
			// Default to true (backward compatibility)
			LOGGER.debug("🔧 No autostart preference found - defaulting to true");
			return true;
		} catch (final Exception e) {
			LOGGER.warn("Error checking autostart preference - defaulting to true: {}", e.getMessage());
			return true;
		}
	}

	/** Handle post-login startup - start Calimero service if enabled and if user wants autostart.
	 * This method should be called after successful login and session setup.
	 * 
	 * ENHANCED LOGIC: Check if Calimero is actually running, allow restart attempts when users enable autostart.
	 * Shows notifications to users about startup success/failure. */
	public void onUserLoginComplete() {
		LOGGER.info("🔐 User login complete - checking Calimero service status and user preference");

		// Check user autostart preference first
		if (!shouldAutostartCalimero()) {
			LOGGER.info("🔌 Calimero autostart disabled by user preference - service will not start automatically");
			LOGGER.info("ℹ️ Calimero can be started manually via system settings or restart button");
			return;
		}

		// User wants autostart - check if Calimero is already running
		final CCalimeroServiceStatus currentStatus = processManager.getCurrentStatus();
		
		if (currentStatus.isEnabled() && currentStatus.isRunning()) {
			LOGGER.info("✅ Calimero autostart requested - service is already running");
			LOGGER.info("🌐 Application-wide: Calimero continues running for ALL users");
			
			// Show info notification that Calimero is already running
			try {
				CNotificationService.showInfo(
					"Calimero service is already running and available for all users");
			} catch (final Exception e) {
				LOGGER.debug("Could not show info notification: {}", e.getMessage());
			}
			return;
		}
		
		// User wants autostart and Calimero is not running - attempt to start
		LOGGER.info("🔌 Calimero autostart enabled and service not running - attempting to start");
		
		synchronized (startupLock) {
			// Double-check in synchronized block to prevent race conditions
			final CCalimeroServiceStatus recheckStatus = processManager.getCurrentStatus();
			if (recheckStatus.isEnabled() && recheckStatus.isRunning()) {
				LOGGER.info("✅ Calimero service started by another thread - service is now running");
				
				try {
					CNotificationService.showSuccess(
						"Calimero service is now running and available for all users");
				} catch (final Exception e) {
					LOGGER.debug("Could not show success notification: {}", e.getMessage());
				}
				return;
			}
			
			// Attempt to start Calimero
			LOGGER.info("🚀 Starting Calimero service for user with autostart enabled...");
			final CCalimeroServiceStatus status = processManager.startCalimeroServiceIfEnabled();

			if (status.isEnabled() && status.isRunning()) {
				LOGGER.info("✅ Calimero service started successfully after user login");
				LOGGER.info("🌐 Application-wide: Calimero is now running for ALL users");
				
				// Show success notification to user
				try {
					CNotificationService.showSuccess(
						"Calimero service started successfully and is now available for all users");
				} catch (final Exception e) {
					LOGGER.debug("Could not show success notification: {}", e.getMessage());
				}
			} else if (!status.isEnabled()) {
				LOGGER.warn("🔧 Calimero service is disabled or not configured");
				
				// Show warning notification to user
				try {
					CNotificationService.showWarning(
						"Calimero service is disabled in system settings. Please enable it in System Settings to use Calimero features.");
				} catch (final Exception e) {
					LOGGER.debug("Could not show warning notification: {}", e.getMessage());
				}
			} else {
				LOGGER.error("⚠️ Failed to start Calimero service - {}", status.getMessage());
				
				// Show error dialog with details - this is what the user requested
				try {
					CNotificationService.showError(
						"Failed to start Calimero service: " + status.getMessage() + 
						". Please check system settings and try manual start via System Settings.");
				} catch (final Exception e) {
					LOGGER.error("Could not show error notification: {}", e.getMessage());
					// If notification fails, at least log the error prominently
					LOGGER.error("🚨 CALIMERO STARTUP FAILED 🚨 User will not see notification: {}", status.getMessage());
				}
			}
		}
	}
	
	/**
	 * Reset method for testing purposes only.
	 * This method is mainly for testing scenarios or during application restart.
	 * In normal operation, Calimero state is checked dynamically via getCurrentStatus().
	 */
	public static void resetForTesting() {
		synchronized (startupLock) {
			// In the new implementation, there's no persistent state to reset
			// The service status is checked dynamically each login
			LOGGER.debug("🔄 Calimero post-login listener reset for testing (no persistent state)");
		}
	}
	
	/**
	 * Check if Calimero process is currently running.
	 * This is a convenience method that delegates to the process manager.
	 * 
	 * @return true if Calimero is running, false otherwise
	 */
	public boolean isCalimeroRunning() {
		try {
			final CCalimeroServiceStatus status = processManager.getCurrentStatus();
			return status.isEnabled() && status.isRunning();
		} catch (final Exception e) {
			LOGGER.warn("Could not check Calimero status: {}", e.getMessage());
			return false;
		}
	}
}
