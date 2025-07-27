package tech.derbent.setup.service;

import java.time.Clock;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.setup.domain.CSystemSettings;

/**
 * CSystemSettingsService - Business logic layer for CSystemSettings entities. Layer:
 * Service (MVC) Provides comprehensive business logic for managing system-wide
 * configuration settings. Since system settings are singleton by design, this service
 * ensures only one settings record exists and provides specialized methods for system
 * configuration management.
 */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true) // Default to read-only transactions for better
									// performance
public class CSystemSettingsService extends CAbstractService<CSystemSettings> {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CSystemSettingsService.class);

	private final CSystemSettingsRepository systemSettingsRepository;

	/**
	 * Constructor for CSystemSettingsService.
	 * @param repository the CSystemSettingsRepository instance
	 * @param clock      the Clock instance for time-related operations
	 */
	public CSystemSettingsService(final CSystemSettingsRepository repository,
		final Clock clock) {
		super(repository, clock);
		this.systemSettingsRepository = repository;
	}

	/**
	 * Creates default system settings. This method is called during initial system setup.
	 * @return the created CSystemSettings
	 */
	@Transactional
	public CSystemSettings createDefaultSystemSettings() {
		LOGGER.debug("createDefaultSystemSettings called");

		// Check if settings already exist
		if (systemSettingsRepository.existsSystemSettings()) {
			LOGGER.warn("Attempt to create default settings when settings already exist");
			throw new IllegalStateException(
				"System settings already exist. Use getOrCreateSystemSettings() instead.");
		}

		try {
			final CSystemSettings newSettings = new CSystemSettings();
			final CSystemSettings savedSettings =
				systemSettingsRepository.saveAndFlush(newSettings);
			LOGGER.info("Default system settings created successfully with ID: {}",
				savedSettings.getId());
			return savedSettings;
		} catch (final Exception e) {
			LOGGER.error("Failed to create default system settings", e);
			throw new RuntimeException(
				"Failed to create system settings: " + e.getMessage(), e);
		}
	}

	/**
	 * Gets the allowed file extensions.
	 * @return array of allowed file extensions
	 */
	public String[] getAllowedFileExtensions() {
		LOGGER.debug("getAllowedFileExtensions called");

		try {
			final Optional<String> result =
				systemSettingsRepository.getAllowedFileExtensions();
			final String extensions =
				result.orElse(".pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg,.txt,.zip");
			final String[] extensionArray = extensions.split(",");

			// Trim whitespace from each extension
			for (int i = 0; i < extensionArray.length; i++) {
				extensionArray[i] = extensionArray[i].trim();
			}
			LOGGER.debug("Allowed file extensions: {}",
				java.util.Arrays.toString(extensionArray));
			return extensionArray;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving allowed file extensions", e);
			return new String[] {
				".pdf", ".doc", ".docx", ".xls", ".xlsx", ".png", ".jpg", ".jpeg", ".txt",
				".zip" };
		}
	}

	/**
	 * Gets the current maintenance message if maintenance mode is enabled.
	 * @return Optional containing the maintenance message if available
	 */
	public Optional<String> getMaintenanceMessage() {
		LOGGER.debug("getMaintenanceMessage called");

		try {
			final Optional<String> result =
				systemSettingsRepository.getMaintenanceMessage();
			LOGGER.debug("Maintenance message found: {}", result.isPresent());
			return result;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving maintenance message", e);
			return Optional.empty();
		}
	}

	/**
	 * Gets the maximum file upload size in MB.
	 * @return the max file upload size, or default value if not found
	 */
	public double getMaxFileUploadSizeMb() {
		LOGGER.debug("getMaxFileUploadSizeMb called");

		try {
			final Optional<java.math.BigDecimal> result =
				systemSettingsRepository.getMaxFileUploadSizeMb();
			final double size =
				result.orElse(new java.math.BigDecimal("50.0")).doubleValue();
			LOGGER.debug("Max file upload size: {} MB", size);
			return size;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving max file upload size", e);
			return 50.0; // Default fallback
		}
	}

	/**
	 * Gets the maximum number of login attempts allowed.
	 * @return the max login attempts, or default value if not found
	 */
	public int getMaxLoginAttempts() {
		LOGGER.debug("getMaxLoginAttempts called");

		try {
			final Optional<Integer> result =
				systemSettingsRepository.getMaxLoginAttempts();
			final int attempts = result.orElse(3); // Default to 3 attempts
			LOGGER.debug("Max login attempts: {}", attempts);
			return attempts;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving max login attempts", e);
			return 3; // Default fallback
		}
	}

	/**
	 * Gets the current system settings. If no settings exist, creates default system
	 * settings.
	 * @return the current CSystemSettings
	 */
	@Transactional
	public CSystemSettings getOrCreateSystemSettings() {
		LOGGER.debug("getOrCreateSystemSettings called");
		final Optional<CSystemSettings> existingSettings =
			systemSettingsRepository.findSystemSettings();

		if (existingSettings.isPresent()) {
			LOGGER.debug("Returning existing system settings");
			return existingSettings.get();
		}
		else {
			LOGGER.info("Creating default system settings");
			return createDefaultSystemSettings();
		}
	}

	/**
	 * Gets the session timeout in minutes.
	 * @return the session timeout value, or default value if not found
	 */
	public int getSessionTimeoutMinutes() {
		LOGGER.debug("getSessionTimeoutMinutes called");

		try {
			final Optional<Integer> result =
				systemSettingsRepository.getSessionTimeoutMinutes();
			final int timeout = result.orElse(60); // Default to 60 minutes
			LOGGER.debug("Session timeout: {} minutes", timeout);
			return timeout;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving session timeout", e);
			return 60; // Default fallback
		}
	}

	/**
	 * Gets the current system settings without creating if they don't exist.
	 * @return Optional containing the CSystemSettings if found, empty otherwise
	 */
	public Optional<CSystemSettings> getSystemSettings() {
		LOGGER.debug("getSystemSettings called");

		try {
			final Optional<CSystemSettings> result =
				systemSettingsRepository.findSystemSettings();
			LOGGER.debug("System settings found: {}", result.isPresent());
			return result;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving system settings", e);
			throw new RuntimeException(
				"Failed to retrieve system settings: " + e.getMessage(), e);
		}
	}

	/**
	 * Checks if maintenance mode is currently enabled.
	 * @return true if maintenance mode is enabled, false otherwise
	 */
	public boolean isMaintenanceModeEnabled() {
		LOGGER.debug("isMaintenanceModeEnabled called");

		try {
			final Optional<Boolean> result =
				systemSettingsRepository.isMaintenanceModeEnabled();
			final boolean maintenanceMode = result.orElse(false);
			LOGGER.debug("Maintenance mode enabled: {}", maintenanceMode);
			return maintenanceMode;
		} catch (final Exception e) {
			LOGGER.error("Error checking maintenance mode status", e);
			return false; // Default to false for safety
		}
	}

	/**
	 * Checks if strong passwords are required.
	 * @return true if strong passwords are required, false otherwise
	 */
	public boolean isStrongPasswordsRequired() {
		LOGGER.debug("isStrongPasswordsRequired called");

		try {
			final Optional<Boolean> result =
				systemSettingsRepository.isStrongPasswordsRequired();
			final boolean required = result.orElse(true); // Default to true for security
			LOGGER.debug("Strong passwords required: {}", required);
			return required;
		} catch (final Exception e) {
			LOGGER.error("Error checking strong password requirement", e);
			return true; // Default to true for security
		}
	}

	/**
	 * Checks if system settings have been initialized. Used to determine if initial setup
	 * is needed.
	 * @return true if system settings exist, false otherwise
	 */
	public boolean isSystemInitialized() {
		LOGGER.debug("isSystemInitialized called");

		try {
			final boolean initialized = systemSettingsRepository.existsSystemSettings();
			LOGGER.debug("System initialized: {}", initialized);
			return initialized;
		} catch (final Exception e) {
			LOGGER.error("Error checking system initialization status", e);
			return false;
		}
	}

	/**
	 * Enables or disables maintenance mode.
	 * @param enabled true to enable maintenance mode, false to disable
	 * @param message the maintenance message to display (optional)
	 * @return the updated CSystemSettings
	 */
	@Transactional
	public CSystemSettings setMaintenanceMode(final boolean enabled,
		final String message) {
		LOGGER.debug("setMaintenanceMode called with enabled: {}, message: {}", enabled,
			message);
		final CSystemSettings settings = getOrCreateSystemSettings();
		settings.setMaintenanceModeEnabled(enabled);

		if ((message != null) && !message.trim().isEmpty()) {
			settings.setMaintenanceMessage(message.trim());
		}
		final CSystemSettings updatedSettings = updateSystemSettings(settings);
		LOGGER.info("Maintenance mode set to: {} with message: {}", enabled, message);
		return updatedSettings;
	}

	/**
	 * Updates system settings with validation.
	 * @param settings the settings to update
	 * @return the updated CSystemSettings
	 * @throws IllegalArgumentException if settings is null or invalid
	 * @throws EntityNotFoundException  if settings don't exist in database
	 */
	@Transactional
	public CSystemSettings updateSystemSettings(final CSystemSettings settings) {
		LOGGER.debug("updateSystemSettings called with settings ID: {}",
			settings != null ? settings.getId() : "null");

		if (settings == null) {
			LOGGER.warn("Attempt to update null system settings");
			throw new IllegalArgumentException("System settings cannot be null");
		}

		if (settings.getId() == null) {
			LOGGER.warn("Attempt to update system settings without ID");
			throw new IllegalArgumentException(
				"System settings must have an ID for update operation");
		}
		// Validate business rules
		validateSystemSettingsBusinessRules(settings);

		try {

			// Check if entity exists
			if (!systemSettingsRepository.existsById(settings.getId())) {
				LOGGER.warn("Attempt to update non-existent system settings with ID: {}",
					settings.getId());
				throw new EntityNotFoundException(
					"System settings not found with ID: " + settings.getId());
			}
			final CSystemSettings updatedSettings =
				systemSettingsRepository.saveAndFlush(settings);
			LOGGER.info("System settings updated successfully with ID: {}",
				updatedSettings.getId());
			return updatedSettings;
		} catch (final EntityNotFoundException e) {
			throw e; // Re-throw EntityNotFoundException as-is
		} catch (final Exception e) {
			LOGGER.error("Failed to update system settings", e);
			throw new RuntimeException(
				"Failed to update system settings: " + e.getMessage(), e);
		}
	}

	/**
	 * Validates business rules for system settings.
	 * @param settings the settings to validate
	 * @throws IllegalArgumentException if validation fails
	 */
	private void validateSystemSettingsBusinessRules(final CSystemSettings settings) {
		LOGGER.debug("validateSystemSettingsBusinessRules called");

		// Validate session timeout
		if ((settings.getSessionTimeoutMinutes() != null)
			&& ((settings.getSessionTimeoutMinutes() < 5)
				|| (settings.getSessionTimeoutMinutes() > 1440))) {
			throw new IllegalArgumentException(
				"Session timeout must be between 5 and 1440 minutes");
		}

		// Validate login attempts
		if ((settings.getMaxLoginAttempts() != null)
			&& ((settings.getMaxLoginAttempts() < 1)
				|| (settings.getMaxLoginAttempts() > 10))) {
			throw new IllegalArgumentException(
				"Max login attempts must be between 1 and 10");
		}

		// Validate lockout duration
		if ((settings.getAccountLockoutDurationMinutes() != null)
			&& ((settings.getAccountLockoutDurationMinutes() < 1)
				|| (settings.getAccountLockoutDurationMinutes() > 1440))) {
			throw new IllegalArgumentException(
				"Account lockout duration must be between 1 and 1440 minutes");
		}

		// Validate file upload size
		if ((settings.getMaxFileUploadSizeMb() != null)
			&& (settings.getMaxFileUploadSizeMb().doubleValue() <= 0)) {
			throw new IllegalArgumentException("Max file upload size must be positive");
		}

		// Validate SMTP port
		if ((settings.getSmtpPort() != null)
			&& ((settings.getSmtpPort() < 1) || (settings.getSmtpPort() > 65535))) {
			throw new IllegalArgumentException("SMTP port must be between 1 and 65535");
		}

		// Validate connection pool size
		if ((settings.getDatabaseConnectionPoolSize() != null)
			&& ((settings.getDatabaseConnectionPoolSize() < 1)
				|| (settings.getDatabaseConnectionPoolSize() > 100))) {
			throw new IllegalArgumentException(
				"Database connection pool size must be between 1 and 100");
		}

		// Validate cache TTL
		if ((settings.getCacheTtlMinutes() != null)
			&& ((settings.getCacheTtlMinutes() < 1)
				|| (settings.getCacheTtlMinutes() > 1440))) {
			throw new IllegalArgumentException(
				"Cache TTL must be between 1 and 1440 minutes");
		}

		// Validate backup retention
		if ((settings.getBackupRetentionDays() != null)
			&& ((settings.getBackupRetentionDays() < 1)
				|| (settings.getBackupRetentionDays() > 365))) {
			throw new IllegalArgumentException(
				"Backup retention must be between 1 and 365 days");
		}

		// Validate password expiry
		if ((settings.getPasswordExpiryDays() != null)
			&& ((settings.getPasswordExpiryDays() < 1)
				|| (settings.getPasswordExpiryDays() > 365))) {
			throw new IllegalArgumentException(
				"Password expiry must be between 1 and 365 days");
		}

		// Validate email format
		if ((settings.getSupportEmail() != null)
			&& !settings.getSupportEmail().trim().isEmpty()) {
			final String email = settings.getSupportEmail().trim();

			if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
				throw new IllegalArgumentException(
					"Support email must be a valid email address");
			}
		}

		if ((settings.getSystemEmailFrom() != null)
			&& !settings.getSystemEmailFrom().trim().isEmpty()) {
			final String email = settings.getSystemEmailFrom().trim();

			if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
				throw new IllegalArgumentException(
					"System email from must be a valid email address");
			}
		}
		LOGGER.debug("System settings validation passed successfully");
	}
}