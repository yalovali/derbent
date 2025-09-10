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
import tech.derbent.session.service.CSessionService;
import tech.derbent.setup.domain.CSystemSettings;

/** CSystemSettingsService - Business logic layer for CSystemSettings entities. Layer: Service (MVC) Provides comprehensive business logic for
 * managing system-wide configuration settings. Since system settings are singleton by design, this service ensures only one settings record exists
 * and provides specialized methods for system configuration management. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CSystemSettingsService extends CAbstractService<CSystemSettings> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettingsService.class);

	/** Constructor for CSystemSettingsService.
	 * @param repository the CSystemSettingsRepository instance
	 * @param clock      the Clock instance for time-related operations */
	public CSystemSettingsService(final CSystemSettingsRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Creates default system settings. This method is called during initial system setup.
	 * @return the created CSystemSettings */
	@Transactional
	public CSystemSettings createDefaultSystemSettings() {
		// Check if settings already exist
		if (((CSystemSettingsRepository) repository).existsSystemSettings()) {
			LOGGER.warn("Attempt to create default settings when settings already exist");
			throw new IllegalStateException("System settings already exist. Use getOrCreateSystemSettings() instead.");
		}
		try {
			final CSystemSettings newSettings = new CSystemSettings();
			final CSystemSettings savedSettings = repository.saveAndFlush(newSettings);
			return savedSettings;
		} catch (final Exception e) {
			LOGGER.error("Failed to create default system settings", e);
			throw new RuntimeException("Failed to create system settings: " + e.getMessage(), e);
		}
	}

	/** Gets the allowed file extensions.
	 * @return array of allowed file extensions */
	public String[] getAllowedFileExtensions() {
		try {
			final Optional<String> result = Optional.ofNullable(((CSystemSettings) repository).getAllowedFileExtensions());
			final String extensions = result.orElse(".pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg,.txt,.zip");
			final String[] extensionArray = extensions.split(",");
			// Trim whitespace from each extension
			for (int i = 0; i < extensionArray.length; i++) {
				extensionArray[i] = extensionArray[i].trim();
			}
			return extensionArray;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving allowed file extensions", e);
			return new String[] {
					".pdf", ".doc", ".docx", ".xls", ".xlsx", ".png", ".jpg", ".jpeg", ".txt", ".zip"
			};
		}
	}

	@Override
	protected Class<CSystemSettings> getEntityClass() { return CSystemSettings.class; }

	/** Gets the current maintenance message if maintenance mode is enabled.
	 * @return Optional containing the maintenance message if available */
	public Optional<String> getMaintenanceMessage() {
		try {
			final Optional<String> result = Optional.ofNullable(((CSystemSettings) repository).getMaintenanceMessage());
			return result;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving maintenance message", e);
			return Optional.empty();
		}
	}

	/** Gets the maximum file upload size in MB.
	 * @return the max file upload size, or default value if not found */
	public double getMaxFileUploadSizeMb() {
		try {
			final Optional<java.math.BigDecimal> result = Optional.ofNullable(((CSystemSettings) repository).getMaxFileUploadSizeMb());
			final double size = result.orElse(new java.math.BigDecimal("50.0")).doubleValue();
			return size;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving max file upload size", e);
			return 50.0; // Default fallback
		}
	}

	/** Gets the maximum number of login attempts allowed.
	 * @return the max login attempts, or default value if not found */
	public int getMaxLoginAttempts() {
		try {
			final Integer result = ((CSystemSettings) repository).getMaxLoginAttempts();
			final int maxAttempts = (result != null) ? result : 3; // Default to 3
			return maxAttempts;// attemptsret
		} catch (final Exception e) {
			LOGGER.error("Error retrieving max login attempts", e);
			return 3; // Default fallback
		}
	}

	/** Gets the current system settings. If no settings exist, creates default system settings.
	 * @return the current CSystemSettings */
	@Transactional
	public CSystemSettings getOrCreateSystemSettings() {
		final Optional<CSystemSettings> existingSettings = ((CSystemSettingsRepository) repository).findSystemSettings();
		if (existingSettings.isPresent()) {
			return existingSettings.get();
		} else {
			return createDefaultSystemSettings();
		}
	}

	/** Gets the session timeout in minutes.
	 * @return the session timeout value, or default value if not found */
	public int getSessionTimeoutMinutes() {
		try {
			final Optional<Integer> result = Optional.ofNullable(((CSystemSettings) repository).getSessionTimeoutMinutes());
			final int timeout = result.orElse(60); // Default to 60 minutes
			return timeout;
		} catch (final Exception e) {
			return 60; // Default fallback
		}
	}

	/** Gets the current system settings without creating if they don't exist.
	 * @return Optional containing the CSystemSettings if found, empty otherwise */
	public Optional<CSystemSettings> getSystemSettings() {
		try {
			final Optional<CSystemSettings> result = ((CSystemSettingsRepository) repository).findSystemSettings();
			return result;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving system settings", e);
			throw new RuntimeException("Failed to retrieve system settings: " + e.getMessage(), e);
		}
	}

	/** Checks if maintenance mode is currently enabled.
	 * @return true if maintenance mode is enabled, false otherwise */
	public boolean isMaintenanceModeEnabled() {
		LOGGER.debug("isMaintenanceModeEnabled called");
		try {
			final Optional<Boolean> result = Optional.ofNullable(((CSystemSettings) repository).isMaintenanceModeEnabled());
			final boolean maintenanceMode = result.orElse(false);
			return maintenanceMode;
		} catch (final Exception e) {
			LOGGER.error("Error checking maintenance mode status", e);
			return false; // Default to false for safety
		}
	}

	/** Checks if strong passwords are required.
	 * @return true if strong passwords are required, false otherwise */
	public boolean isStrongPasswordsRequired() {
		LOGGER.debug("isStrongPasswordsRequired called");
		try {
			final Optional<Boolean> result = Optional.of(((CSystemSettingsService) repository).isStrongPasswordsRequired());
			final boolean required = result.orElse(true); // Default to true for security
			return required;
		} catch (final Exception e) {
			LOGGER.error("Error checking strong password requirement", e);
			return true; // Default to true for security
		}
	}

	/** Checks if system settings have been initialized. Used to determine if initial setup is needed.
	 * @return true if system settings exist, false otherwise */
	public boolean isSystemInitialized() {
		try {
			final boolean initialized = ((CSystemSettingsRepository) repository).existsSystemSettings();
			return initialized;
		} catch (final Exception e) {
			LOGGER.error("Error checking system initialization status", e);
			return false;
		}
	}

	/** Enables or disables maintenance mode.
	 * @param enabled true to enable maintenance mode, false to disable
	 * @param message the maintenance message to display (optional)
	 * @return the updated CSystemSettings */
	@Transactional
	public CSystemSettings setMaintenanceMode(final boolean enabled, final String message) {
		final CSystemSettings settings = getOrCreateSystemSettings();
		settings.setMaintenanceModeEnabled(enabled);
		if ((message != null) && !message.trim().isEmpty()) {
			settings.setMaintenanceMessage(message.trim());
		}
		final CSystemSettings updatedSettings = updateSystemSettings(settings);
		return updatedSettings;
	}

	/** Updates system settings with validation.
	 * @param settings the settings to update
	 * @return the updated CSystemSettings
	 * @throws IllegalArgumentException if settings is null or invalid
	 * @throws EntityNotFoundException  if settings don't exist in database */
	@Transactional
	public CSystemSettings updateSystemSettings(final CSystemSettings settings) {
		if (settings == null) {
			LOGGER.warn("Attempt to update null system settings");
			throw new IllegalArgumentException("System settings cannot be null");
		}
		if (settings.getId() == null) {
			LOGGER.warn("Attempt to update system settings without ID");
			throw new IllegalArgumentException("System settings must have an ID for update operation");
		}
		// Validate business rules
		validateSystemSettingsBusinessRules(settings);
		try {
			// Check if entity exists
			if (!repository.existsById(settings.getId())) {
				LOGGER.warn("Attempt to update non-existent system settings with ID: {}", settings.getId());
				throw new EntityNotFoundException("System settings not found with ID: " + settings.getId());
			}
			final CSystemSettings updatedSettings = repository.saveAndFlush(settings);
			return updatedSettings;
		} catch (final EntityNotFoundException e) {
			throw e; // Re-throw EntityNotFoundException as-is
		} catch (final Exception e) {
			LOGGER.error("Failed to update system settings", e);
			throw new RuntimeException("Failed to update system settings: " + e.getMessage(), e);
		}
	}

	/** Gets the auto-login enabled setting.
	 * @return true if auto-login is enabled, false otherwise */
	public boolean isAutoLoginEnabled() {
		try {
			final CSystemSettings settings = getOrCreateSystemSettings();
			return settings.getAutoLoginEnabled() != null ? settings.getAutoLoginEnabled() : false;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving auto-login enabled setting", e);
			return false; // Default to false for security
		}
	}

	/** Gets the default login view setting.
	 * @return the default view to navigate to after login */
	public String getDefaultLoginView() {
		try {
			final CSystemSettings settings = getOrCreateSystemSettings();
			return settings.getDefaultLoginView() != null ? settings.getDefaultLoginView() : "home";
		} catch (final Exception e) {
			LOGGER.error("Error retrieving default login view setting", e);
			return "home"; // Default fallback
		}
	}

	/** Updates the auto-login settings.
	 * @param autoLoginEnabled true to enable auto-login, false to disable
	 * @param defaultView      the default view to navigate to after login
	 * @return the updated CSystemSettings */
	@Transactional
	public CSystemSettings updateAutoLoginSettings(final boolean autoLoginEnabled, final String defaultView) {
		final CSystemSettings settings = getOrCreateSystemSettings();
		settings.setAutoLoginEnabled(autoLoginEnabled);
		if ((defaultView != null) && !defaultView.trim().isEmpty()) {
			settings.setDefaultLoginView(defaultView.trim());
		}
		return updateSystemSettings(settings);
	}

	/** Validates business rules for system settings.
	 * @param settings the settings to validate
	 * @throws IllegalArgumentException if validation fails */
	private void validateSystemSettingsBusinessRules(final CSystemSettings settings) {
		if ((settings.getSessionTimeoutMinutes() != null)
				&& ((settings.getSessionTimeoutMinutes() < 5) || (settings.getSessionTimeoutMinutes() > 1440))) {
			throw new IllegalArgumentException("Session timeout must be between 5 and 1440 minutes");
		}
		// Validate login attempts
		if ((settings.getMaxLoginAttempts() != null) && ((settings.getMaxLoginAttempts() < 1) || (settings.getMaxLoginAttempts() > 10))) {
			throw new IllegalArgumentException("Max login attempts must be between 1 and 10");
		}
		// Validate lockout duration
		if ((settings.getAccountLockoutDurationMinutes() != null)
				&& ((settings.getAccountLockoutDurationMinutes() < 1) || (settings.getAccountLockoutDurationMinutes() > 1440))) {
			throw new IllegalArgumentException("Account lockout duration must be between 1 and 1440 minutes");
		}
		// Validate file upload size
		if ((settings.getMaxFileUploadSizeMb() != null) && (settings.getMaxFileUploadSizeMb().doubleValue() <= 0)) {
			throw new IllegalArgumentException("Max file upload size must be positive");
		}
		// Validate SMTP port
		if ((settings.getSmtpPort() != null) && ((settings.getSmtpPort() < 1) || (settings.getSmtpPort() > 65535))) {
			throw new IllegalArgumentException("SMTP port must be between 1 and 65535");
		}
		// Validate connection pool size
		if ((settings.getDatabaseConnectionPoolSize() != null)
				&& ((settings.getDatabaseConnectionPoolSize() < 1) || (settings.getDatabaseConnectionPoolSize() > 100))) {
			throw new IllegalArgumentException("Database connection pool size must be between 1 and 100");
		}
		// Validate cache TTL
		if ((settings.getCacheTtlMinutes() != null) && ((settings.getCacheTtlMinutes() < 1) || (settings.getCacheTtlMinutes() > 1440))) {
			throw new IllegalArgumentException("Cache TTL must be between 1 and 1440 minutes");
		}
		// Validate backup retention
		if ((settings.getBackupRetentionDays() != null) && ((settings.getBackupRetentionDays() < 1) || (settings.getBackupRetentionDays() > 365))) {
			throw new IllegalArgumentException("Backup retention must be between 1 and 365 days");
		}
		// Validate password expiry
		if ((settings.getPasswordExpiryDays() != null) && ((settings.getPasswordExpiryDays() < 1) || (settings.getPasswordExpiryDays() > 365))) {
			throw new IllegalArgumentException("Password expiry must be between 1 and 365 days");
		}
		// Validate email format
		if ((settings.getSupportEmail() != null) && !settings.getSupportEmail().trim().isEmpty()) {
			final String email = settings.getSupportEmail().trim();
			if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
				throw new IllegalArgumentException("Support email must be a valid email address");
			}
		}
		if ((settings.getSystemEmailFrom() != null) && !settings.getSystemEmailFrom().trim().isEmpty()) {
			final String email = settings.getSystemEmailFrom().trim();
			if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
				throw new IllegalArgumentException("System email from must be a valid email address");
			}
		}
		LOGGER.debug("System settings validation passed successfully");
	}
}
