package tech.derbent.bab.calimero.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;
import tech.derbent.bab.setup.service.CSystemSettings_BabService;

/** CCalimeroProcessManager - Manages the Calimero HTTP server process lifecycle.
 * <p>
 * This service:
 * <ul>
 * <li>Starts Calimero executable on application startup (if enabled)</li>
 * <li>Monitors process health and logs output (stdout/stderr)</li>
 * <li>Notifies on process crashes</li>
 * <li>Terminates process on application shutdown</li>
 * </ul>
 * <p>
 * Configuration from CSystemSettings_Bab:
 * <ul>
 * <li>enableCalimeroService - Enable/disable automatic process management</li>
 * <li>calimeroExecutablePath - Path to Calimero binary (default: ~/git/calimero/build/calimero)</li>
 * </ul>
 * <p>
 * Active when: 'bab' profile is active */
@Service
@Profile ("bab")
public class CCalimeroProcessManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(CCalimeroProcessManager.class);
	private final CSystemSettings_BabService settingsService;
	private Process calimeroProcess;
	private ExecutorService executorService = Executors.newFixedThreadPool(3);
	private final AtomicBoolean isRunning = new AtomicBoolean(false);
	private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);

	public CCalimeroProcessManager(final CSystemSettings_BabService settingsService) {
		this.settingsService = settingsService;
	}

	private synchronized ExecutorService ensureExecutorService() {
		if ((executorService == null) || executorService.isShutdown() || executorService.isTerminated()) {
			executorService = Executors.newFixedThreadPool(3);
		}
		return executorService;
	}

	public synchronized CCalimeroServiceStatus getCurrentStatus() {
		try {
			final CSystemSettings_Bab settings = settingsService.getSystemSettings();
			final boolean enabled = (settings != null) && Boolean.TRUE.equals(settings.getEnableCalimeroService());
			if (!enabled) {
				return CCalimeroServiceStatus.of(false, false, "Calimero service disabled");
			}
			return CCalimeroServiceStatus.of(true, isRunning(), isRunning() ? "Calimero service is running" : "Calimero service is stopped");
		} catch (final Exception e) {
			LOGGER.warn("Unable to determine Calimero service status: {}", e.getMessage());
			return CCalimeroServiceStatus.of(false, false, "Calimero status unavailable: " + e.getMessage());
		}
	}

	/** Check if Calimero service is currently running.
	 * @return true if running */
	public boolean isRunning() { return isRunning.get() && (calimeroProcess != null) && calimeroProcess.isAlive(); }

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
		stopCalimeroService();
		return startCalimeroServiceIfEnabled();
	}

	/** Start Calimero process and monitor its output.
	 * @param executablePath path to Calimero executable
	 * @return true if started successfully */
	private boolean startCalimeroProcess(final Path executablePath) {
		if (isRunning.get()) {
			LOGGER.warn("Calimero process is already running");
			return true;
		}
		try {
			LOGGER.info("Starting Calimero process: {}", executablePath);
			shutdownRequested.set(false);
			final File workingDir = executablePath.getParent().toFile();
			final ProcessBuilder processBuilder = new ProcessBuilder(executablePath.toString());
			processBuilder.directory(workingDir);
			processBuilder.redirectErrorStream(false);
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

	/** Start Calimero service if enabled in settings. Called during application startup.
	 * @return true if started successfully or disabled, false if start failed */
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
}
