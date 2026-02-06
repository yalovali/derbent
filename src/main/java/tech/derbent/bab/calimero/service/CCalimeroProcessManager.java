package tech.derbent.bab.calimero.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.PreDestroy;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.calimero.CCalimeroConstants;
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;
import tech.derbent.bab.setup.service.CSystemSettings_BabService;
import tech.derbent.base.session.service.ISessionService;

/** CCalimeroProcessManager - Manages the Calimero HTTP server process lifecycle.
 * <p>
 * This service:
 * <ul>
 * <li>Detects existing Calimero processes on port 8077 before starting new ones</li>
 * <li>Starts Calimero executable on application startup (if enabled)</li>
 * <li>Monitors process health and logs output (stdout/stderr)</li>
 * <li>Notifies on process crashes</li>
 * <li>Terminates process on application shutdown</li>
 * <li>Respects user autostart preferences from login screen</li>
 * </ul>
 * <p>
 * Configuration from CSystemSettings_Bab:
 * <ul>
 * <li>enableCalimeroService - Enable/disable automatic process management</li>
 * <li>calimeroExecutablePath - Path to Calimero binary (default: ~/git/calimero/build/calimero)</li>
 * </ul>
 * <p>
 * Port Detection: Checks port 8077 before starting to avoid duplicate processes.
 * <p>
 * Active when: 'bab' profile is active */
@Service
@Profile ("bab")
public class CCalimeroProcessManager {

	/** Default Calimero HTTP API port. */
	private static final int CALIMERO_PORT = 8077;
	private static final Logger LOGGER = LoggerFactory.getLogger(CCalimeroProcessManager.class);
	/** Timeout for port availability check (milliseconds). */
	private static final int PORT_CHECK_TIMEOUT_MS = 2000;
	private Process calimeroProcess;
	private ExecutorService executorService = Executors.newFixedThreadPool(3);
	private final AtomicBoolean isRunning = new AtomicBoolean(false);
	private final ISessionService sessionService;
	private final CSystemSettings_BabService settingsService;
	private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);

	public CCalimeroProcessManager(final CSystemSettings_BabService settingsService, final ISessionService sessionService) {
		this.settingsService = settingsService;
		this.sessionService = sessionService;
	}

	private synchronized ExecutorService ensureExecutorService() {
		if ((executorService == null) || executorService.isShutdown() || executorService.isTerminated()) {
			executorService = Executors.newFixedThreadPool(3);
		}
		return executorService;
	}

	/** Force start Calimero service regardless of autostart preference. Used for manual restarts via system settings or restart buttons.
	 * <p>
	 * Will detect and use existing Calimero processes instead of starting duplicates.
	 * @return status of the service after start attempt */
	public synchronized CCalimeroServiceStatus forceStartCalimeroService() {
		LOGGER.info("Force start of Calimero service requested (ignoring autostart preference)");
		try {
			final CSystemSettings_Bab settings = settingsService.getSystemSettings();
			if (settings == null) {
				LOGGER.warn("No BAB system settings found - Calimero service will not start");
				return CCalimeroServiceStatus.of(false, false, "No system settings found for BAB profile");
			}
			if (Boolean.FALSE.equals(settings.getEnableCalimeroService())) {
				LOGGER.info("Calimero service is disabled in system settings");
				return CCalimeroServiceStatus.of(false, false, "Calimero service disabled in gateway settings");
			}
			// Check if external process is already running - use it instead of starting new one
			if (isCalimeroPortListening()) {
				LOGGER.info("🔍 Existing Calimero service detected on port {} - will use existing process", CALIMERO_PORT);
				isRunning.set(true); // Mark as running for status reporting
				return CCalimeroServiceStatus.of(true, true, "Using existing Calimero service on port " + CALIMERO_PORT);
			}
			// Skip autostart preference check for manual/force start
			String executablePath = settings.getCalimeroExecutablePath();
			if ((executablePath == null) || executablePath.isBlank()) {
				executablePath = "~/git/calimero/build/calimero";
			}
			if (executablePath.startsWith("~")) {
				executablePath = System.getProperty("user.home") + executablePath.substring(1);
			}
			final Path execPath = Paths.get(executablePath);
			if (!Files.exists(execPath)) {
				final String errorMsg = "Calimero executable not found at: " + executablePath;
				LOGGER.error(errorMsg);
				CNotificationService.showError(errorMsg);
				return CCalimeroServiceStatus.of(true, false, errorMsg);
			}
			if (!Files.isExecutable(execPath)) {
				final String errorMsg = "Calimero binary is not executable: " + executablePath;
				LOGGER.error(errorMsg);
				CNotificationService.showError(errorMsg);
				return CCalimeroServiceStatus.of(true, false, errorMsg);
			}
			final boolean started = startCalimeroProcess(execPath);
			if (started && isRunning()) {
				return CCalimeroServiceStatus.of(true, true, "Calimero service is running");
			}
			return CCalimeroServiceStatus.of(true, false, "Calimero process failed to start - check logs");
		} catch (final Exception e) {
			LOGGER.error("Failed to force start Calimero service: {}", e.getMessage(), e);
			return CCalimeroServiceStatus.of(true, false, "Failed to start Calimero service: " + e.getMessage());
		}
	}

	public synchronized CCalimeroServiceStatus getCurrentStatus() {
		try {
			final CSystemSettings_Bab settings = settingsService.getSystemSettings();
			final boolean enabled = (settings != null) && Boolean.TRUE.equals(settings.getEnableCalimeroService());
			if (!enabled) {
				return CCalimeroServiceStatus.of(false, false, "Calimero service disabled");
			}
			// Check both our process AND port listening status
			final boolean portListening = isCalimeroPortListening();
			final boolean processRunning = isRunning();
			if (portListening) {
				if (processRunning) {
					return CCalimeroServiceStatus.of(true, true, "Calimero service is running (managed process)");
				}
				// External Calimero process detected
				return CCalimeroServiceStatus.of(true, true, "Calimero service is running (external process)");
			}
			return CCalimeroServiceStatus.of(true, false,
					processRunning ? "Calimero process running but not responding on port " + CALIMERO_PORT : "Calimero service is stopped");
		} catch (final Exception e) {
			LOGGER.warn("Unable to determine Calimero service status: {}", e.getMessage());
			return CCalimeroServiceStatus.of(false, false, "Calimero status unavailable: " + e.getMessage());
		}
	}

	/** Check if user has enabled autostart for Calimero process. This checks both VaadinSession and session service for the preference.
	 * @return true if autostart is enabled (default), false if user disabled it */
	public boolean isAutostartEnabled() {
		try {
			// Try VaadinSession first (set during login)
			final VaadinSession vaadinSession = VaadinSession.getCurrent();
			if (vaadinSession != null) {
				final Boolean preference = (Boolean) vaadinSession.getAttribute(CCalimeroConstants.SESSION_KEY_AUTOSTART_CALIMERO);
				if (preference != null) {
					LOGGER.debug("Found VaadinSession autostart preference: {}", preference);
					return preference.booleanValue();
				}
			}
			// Try session service as fallback
			final Optional<Boolean> sessionPreferenceOpt = sessionService.getSessionValue(CCalimeroConstants.SESSION_KEY_AUTOSTART_CALIMERO);
			if (sessionPreferenceOpt.isPresent()) {
				final Boolean sessionPreference = sessionPreferenceOpt.get();
				LOGGER.debug("Found session service autostart preference: {}", sessionPreference);
				return sessionPreference.booleanValue();
			}
			// Default to true for backward compatibility
			LOGGER.debug("No autostart preference found - defaulting to true");
			return true;
		} catch (final Exception e) {
			LOGGER.warn("Error checking autostart preference: {}", e.getMessage());
			return true; // Safe default
		}
	}

	/** Check if Calimero service is listening on port 8077.
	 * <p>
	 * This method checks if ANY process (not just ours) is listening on the Calimero port. Prevents starting duplicate Calimero instances.
	 * @return true if port 8077 is occupied (Calimero is likely running), false otherwise */
	public boolean isCalimeroPortListening() {
		try (Socket socket = new Socket()) {
			socket.setSoTimeout(PORT_CHECK_TIMEOUT_MS);
			socket.connect(new InetSocketAddress("localhost", CALIMERO_PORT), PORT_CHECK_TIMEOUT_MS);
			LOGGER.debug("✅ Port {} is listening (Calimero service detected)", CALIMERO_PORT);
			return true;
		} catch (final SocketTimeoutException e) {
			LOGGER.debug("⏰ Port {} check timeout - service not responding", CALIMERO_PORT);
			return false;
		} catch (final IOException e) {
			LOGGER.debug("🔍 Port {} not listening - no service detected", CALIMERO_PORT);
			return false;
		}
	}

	/** Check if Calimero service is currently running.
	 * <p>
	 * Checks both managed process status AND port listening status. Returns true if either our managed process is running OR if an external Calimero
	 * service is detected on port 8077.
	 * @return true if Calimero service is available (managed or external process) */
	public boolean isRunning() {
		// Check if we have a managed process that's alive
		final boolean managedProcessRunning = isRunning.get() && (calimeroProcess != null) && calimeroProcess.isAlive();
		// If managed process is running, return true
		if (managedProcessRunning) {
			return true;
		}
		// If no managed process, check if external Calimero is listening on port
		return isCalimeroPortListening();
	}

	/** Monitor process health and notify on crashes. */
	private void monitorProcessHealth() {
		try {
			final int exitCode = calimeroProcess.waitFor();
			isRunning.set(false);
			if (!shutdownRequested.get()) {
				final String errorMsg = "Calimero process terminated unexpectedly with exit code: " + exitCode;
				LOGGER.error(errorMsg);
				CNotificationService.showError(errorMsg);
			} else {
				LOGGER.info("Calimero process terminated normally with exit code: {}", exitCode);
			}
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.warn("Calimero process monitoring interrupted: {}", e.getMessage());
		}
	}

	/** Monitor process output stream (stdout or stderr).
	 * @param process    the process to monitor
	 * @param streamName name for logging (STDOUT or STDERR) */
	private void monitorProcessOutput(final Process process, final String streamName) {
		try (BufferedReader reader =
				new BufferedReader(new InputStreamReader("STDERR".equals(streamName) ? process.getErrorStream() : process.getInputStream()))) {
			reader.lines().forEach(line -> {
				if ("STDERR".equals(streamName)) {
					LOGGER.warn("[Calimero STDERR] {}", line);
				} else {
					LOGGER.info("[Calimero STDOUT] {}", line);
				}
			});
		} catch (final IOException e) {
			if (!shutdownRequested.get()) {
				LOGGER.error("Error reading Calimero {} stream: {}", streamName, e.getMessage(), e);
			}
		}
	}

	/** Event listener for application context closed - cleanup resources. */
	@SuppressWarnings ("unused")
	@EventListener
	public void onApplicationContextClosed(final ContextClosedEvent event) {
		LOGGER.info("Application context closed - stopping Calimero service");
		stopCalimeroService();
	}

	public synchronized CCalimeroServiceStatus restartCalimeroService() {
		LOGGER.info("Manual restart of Calimero service requested");
		// Check if external process is running
		if (isCalimeroPortListening() && (calimeroProcess == null || !calimeroProcess.isAlive())) {
			LOGGER.warn("⚠️ Cannot restart - external Calimero process detected on port {}. Please stop it manually first.", CALIMERO_PORT);
			return CCalimeroServiceStatus.of(true, true, "Cannot restart - external Calimero process detected. Stop it manually first.");
		}
		stopCalimeroService();
		return forceStartCalimeroService(); // Use force start for manual restarts
	}

	/** Set the user's autostart preference for Calimero process. This stores the preference in both VaadinSession and session service.
	 * @param enabled true to enable autostart, false to disable */
	public void setAutostartEnabled(final boolean enabled) {
		try {
			// Store in VaadinSession if available
			final VaadinSession vaadinSession = VaadinSession.getCurrent();
			if (vaadinSession != null) {
				vaadinSession.setAttribute(CCalimeroConstants.SESSION_KEY_AUTOSTART_CALIMERO, Boolean.valueOf(enabled));
				LOGGER.debug("Stored autostart preference in VaadinSession: {}", enabled);
			}
			// Store in session service as well
			sessionService.setSessionValue(CCalimeroConstants.SESSION_KEY_AUTOSTART_CALIMERO, Boolean.valueOf(enabled));
			LOGGER.debug("Stored autostart preference in session service: {}", enabled);
		} catch (final Exception e) {
			LOGGER.warn("Error setting autostart preference: {}", e.getMessage());
		}
	}

	/** Start Calimero process and monitor its output.
	 * @param executablePath path to Calimero executable
	 * @return true if started successfully */
	private boolean startCalimeroProcess(final Path executablePath) {
		// Check if we already have a managed process running
		if (isRunning.get()) {
			LOGGER.warn("Calimero process is already running (managed)");
			return true;
		}
		// Check if port is already occupied by external process
		if (isCalimeroPortListening()) {
			LOGGER.info("🔍 Calimero service already running on port {} (external process) - will not start duplicate", CALIMERO_PORT);
			// Mark as running so status reports correctly, but don't manage the process
			isRunning.set(true);
			return true;
		}
		try {
			LOGGER.info("Starting Calimero process: {}", executablePath);
			shutdownRequested.set(false);
			final File workingDir = executablePath.getParent().toFile();
			final ProcessBuilder processBuilder = new ProcessBuilder(executablePath.toString());
			processBuilder.directory(workingDir);
			processBuilder.redirectErrorStream(false);
			
			// Configure environment for Calimero config path support
			configureCalimeroEnvironment(processBuilder);
			
			calimeroProcess = processBuilder.start();
			isRunning.set(true);
			LOGGER.info("Calimero process started successfully (PID: {})", calimeroProcess.pid());
			final ExecutorService executor = ensureExecutorService();
			executor.submit(() -> monitorProcessOutput(calimeroProcess, "STDOUT"));
			executor.submit(() -> monitorProcessOutput(calimeroProcess, "STDERR"));
			executor.submit(this::monitorProcessHealth);
			return true;
		} catch (final IOException e) {
			LOGGER.error("Failed to start Calimero process: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to start Calimero process", e);
			isRunning.set(false);
			return false;
		}
	}

	/** Start Calimero service if enabled in settings and if user wants autostart. Called during application startup and after database resets.
	 * <p>
	 * Will detect and use existing Calimero processes instead of starting duplicates.
	 * @return status of the service after start attempt */
	public synchronized CCalimeroServiceStatus startCalimeroServiceIfEnabled() {
		try {
			final CSystemSettings_Bab settings = settingsService.getSystemSettings();
			if (settings == null) {
				LOGGER.warn("No BAB system settings found - Calimero service will not start");
				return CCalimeroServiceStatus.of(false, false, "No system settings found for BAB profile");
			}
			if (Boolean.FALSE.equals(settings.getEnableCalimeroService())) {
				LOGGER.info("Calimero service is disabled in system settings");
				return CCalimeroServiceStatus.of(false, false, "Calimero service disabled in gateway settings");
			}
			// Check if external process is already running - use it instead of checking autostart preference
			if (isCalimeroPortListening()) {
				LOGGER.info("🔍 Existing Calimero service detected on port {} - will use existing process", CALIMERO_PORT);
				isRunning.set(true); // Mark as running for status reporting
				return CCalimeroServiceStatus.of(true, true, "Using existing Calimero service on port " + CALIMERO_PORT);
			}
			// Check user autostart preference only if no external process detected
			if (!isAutostartEnabled()) {
				LOGGER.info("🔌 Calimero autostart disabled by user preference - service will not start automatically");
				return CCalimeroServiceStatus.of(true, false, "Calimero autostart disabled by user preference");
			}
			String executablePath = settings.getCalimeroExecutablePath();
			if ((executablePath == null) || executablePath.isBlank()) {
				executablePath = "~/git/calimero/build/calimero";
			}
			if (executablePath.startsWith("~")) {
				executablePath = System.getProperty("user.home") + executablePath.substring(1);
			}
			final Path execPath = Paths.get(executablePath);
			if (!Files.exists(execPath)) {
				final String errorMsg = "Calimero executable not found at: " + executablePath;
				LOGGER.error(errorMsg);
				CNotificationService.showError(errorMsg);
				return CCalimeroServiceStatus.of(true, false, errorMsg);
			}
			if (!Files.isExecutable(execPath)) {
				final String errorMsg = "Calimero binary is not executable: " + executablePath;
				LOGGER.error(errorMsg);
				CNotificationService.showError(errorMsg);
				return CCalimeroServiceStatus.of(true, false, errorMsg);
			}
			final boolean started = startCalimeroProcess(execPath);
			if (started && isRunning()) {
				return CCalimeroServiceStatus.of(true, true, "Calimero service is running");
			}
			return CCalimeroServiceStatus.of(true, false, "Calimero process failed to start - check logs");
		} catch (final Exception e) {
			LOGGER.error("Failed to start Calimero service: {}", e.getMessage(), e);
			return CCalimeroServiceStatus.of(true, false, "Failed to start Calimero service: " + e.getMessage());
		}
	}

	/** Stop Calimero service and cleanup resources. */
	@PreDestroy
	public synchronized void stopCalimeroService() {
		if (!isRunning.get()) {
			shutdownRequested.set(false);
			return;
		}
		try {
			LOGGER.info("Stopping Calimero service...");
			shutdownRequested.set(true);
			if ((calimeroProcess != null) && calimeroProcess.isAlive()) {
				calimeroProcess.destroy();
				final boolean terminated = calimeroProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
				if (!terminated) {
					LOGGER.warn("Calimero process did not terminate gracefully, forcing shutdown");
					calimeroProcess.destroyForcibly();
				}
				LOGGER.info("Calimero process stopped successfully");
			}
			isRunning.set(false);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Error stopping Calimero service: {}", e.getMessage(), e);
		} finally {
			if (executorService != null) {
				executorService.shutdownNow();
				executorService = null;
			}
			shutdownRequested.set(false);
		}
	}

	/**
	 * Configure environment variables for Calimero process to support custom config path.
	 * 
	 * Sets HTTP_SETTINGS_FILE environment variable based on system settings to support
	 * the C++ pattern from CNodeHttp::CNodeHttp() that checks:
	 * 1. HTTP_SETTINGS_FILE environment variable 
	 * 2. Falls back to HTTP_DEFAULT_SETTINGS_FILE ("config/http_server.json")
	 * 
	 * @param processBuilder the process builder to configure environment for
	 */
	private void configureCalimeroEnvironment(final ProcessBuilder processBuilder) {
		try {
			final CSystemSettings_Bab settings = settingsService.getSystemSettings();
			if (settings == null) {
				LOGGER.warn("No BAB system settings found - using default Calimero config path");
				return;
			}
			
			String configPath = settings.getCalimeroConfigPath();
			if (configPath == null || configPath.isBlank()) {
				LOGGER.debug("No custom config path set - using Calimero default (config/http_server.json)");
				return;
			}
			
			// Expand tilde notation
			if (configPath.startsWith("~")) {
				configPath = System.getProperty("user.home") + configPath.substring(1);
			}
			
			// Ensure path ends with / for directory
			if (!configPath.endsWith("/") && !configPath.endsWith("\\")) {
				configPath += "/";
			}
			
			// Build full path to HTTP settings file
			final String httpSettingsFile = configPath + CCalimeroConstants.DEFAULT_HTTP_SETTINGS_FILENAME;
			
			// Verify the settings file exists
			final Path settingsPath = Paths.get(httpSettingsFile);
			if (!Files.exists(settingsPath)) {
				LOGGER.warn("HTTP settings file not found at: {} - using default path", httpSettingsFile);
				return;
			}
			
			// Set environment variable for Calimero C++ process
			processBuilder.environment().put(CCalimeroConstants.ENV_HTTP_SETTINGS_FILE, httpSettingsFile);
			LOGGER.info("🔧 Configured Calimero HTTP_SETTINGS_FILE environment variable: {}", httpSettingsFile);
			
			// Also log other relevant environment for debugging
			LOGGER.debug("Calimero working directory: {}", processBuilder.directory());
			LOGGER.debug("Environment variables set for Calimero process:");
			processBuilder.environment().entrySet().stream()
				.filter(entry -> entry.getKey().contains("HTTP") || entry.getKey().contains("CONFIG"))
				.forEach(entry -> LOGGER.debug("  {}={}", entry.getKey(), entry.getValue()));
			
		} catch (final Exception e) {
			LOGGER.warn("Failed to configure Calimero environment - using defaults: {}", e.getMessage());
		}
	}
}
