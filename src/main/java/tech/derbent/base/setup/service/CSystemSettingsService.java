package tech.derbent.base.setup.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.setup.domain.CSystemSettings;

/** CSystemSettingsService - Business logic layer for CSystemSettings entities. Layer: Service (MVC) Provides comprehensive business logic for
 * managing system-wide configuration settings. Since system settings are singleton by design, this service ensures only one settings record exists
 * and provides specialized methods for system configuration management. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CSystemSettingsService extends CAbstractService<CSystemSettings> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettingsService.class);

	/** Validates business rules for system settings.
	 * @param settings the settings to validate
	 * @throws IllegalArgumentException if validation fails */
	private static void validateSystemSettingsBusinessRules(final CSystemSettings settings) {
		if (settings.getSessionTimeoutMinutes() != null && (settings.getSessionTimeoutMinutes() < 5 || settings.getSessionTimeoutMinutes() > 1440)) {
			throw new IllegalArgumentException("Session timeout must be between 5 and 1440 minutes");
		}
		// Validate login attempts
		if (settings.getMaxLoginAttempts() != null && (settings.getMaxLoginAttempts() < 1 || settings.getMaxLoginAttempts() > 10)) {
			throw new IllegalArgumentException("Max login attempts must be between 1 and 10");
		}
		// Validate lockout duration
		if (settings.getAccountLockoutDurationMinutes() != null
				&& (settings.getAccountLockoutDurationMinutes() < 1 || settings.getAccountLockoutDurationMinutes() > 1440)) {
			throw new IllegalArgumentException("Account lockout duration must be between 1 and 1440 minutes");
		}
		// Validate file upload size
		if (settings.getMaxFileUploadSizeMb() != null && settings.getMaxFileUploadSizeMb().doubleValue() <= 0) {
			throw new IllegalArgumentException("Max file upload size must be positive");
		}
		// Validate SMTP port
		if (settings.getSmtpPort() != null && (settings.getSmtpPort() < 1 || settings.getSmtpPort() > 65535)) {
			throw new IllegalArgumentException("SMTP port must be between 1 and 65535");
		}
		// Validate connection pool size
		if (settings.getDatabaseConnectionPoolSize() != null
				&& (settings.getDatabaseConnectionPoolSize() < 1 || settings.getDatabaseConnectionPoolSize() > 100)) {
			throw new IllegalArgumentException("Database connection pool size must be between 1 and 100");
		}
		// Validate cache TTL
		if (settings.getCacheTtlMinutes() != null && (settings.getCacheTtlMinutes() < 1 || settings.getCacheTtlMinutes() > 1440)) {
			throw new IllegalArgumentException("Cache TTL must be between 1 and 1440 minutes");
		}
		// Validate backup retention
		if (settings.getBackupRetentionDays() != null && (settings.getBackupRetentionDays() < 1 || settings.getBackupRetentionDays() > 365)) {
			throw new IllegalArgumentException("Backup retention must be between 1 and 365 days");
		}
		// Validate password expiry
		if (settings.getPasswordExpiryDays() != null && (settings.getPasswordExpiryDays() < 1 || settings.getPasswordExpiryDays() > 365)) {
			throw new IllegalArgumentException("Password expiry must be between 1 and 365 days");
		}
		// Validate email format
		if (settings.getSupportEmail() != null && !settings.getSupportEmail().trim().isEmpty()) {
			final String email = settings.getSupportEmail().trim();
			if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
				throw new IllegalArgumentException("Support email must be a valid email address");
			}
		}
		if (settings.getSystemEmailFrom() != null && !settings.getSystemEmailFrom().trim().isEmpty()) {
			final String email = settings.getSystemEmailFrom().trim();
			if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
				throw new IllegalArgumentException("System email from must be a valid email address");
			}
		}
		LOGGER.debug("System settings validation passed successfully");
	}

	/** Constructor for CSystemSettingsService.
	 * @param repository the CSystemSettingsRepository instance
	 * @param clock      the Clock instance for time-related operations */
	public CSystemSettingsService(final ISystemSettingsRepository repository, final Clock clock, @Lazy final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing system settings deletion. Note: System settings should not be deleted, but this method provides
	 * protection.
	 * @param systemSettings the system settings entity to check
	 * @return error message preventing deletion */
	@Override
	public String checkDeleteAllowed(final CSystemSettings systemSettings) {
		return super.checkDeleteAllowed(systemSettings);
		// System settings should not be deleted - it's a singleton
		// Uncomment to prevent deletion: return "System settings cannot be deleted. It is a required system configuration.";
	}

	/** Creates default system settings. This method is called during initial system setup.
	 * @return the created CSystemSettings */
	@Transactional
	public CSystemSettings createDefaultSystemSettings() {
		// Check if settings already exist
		Check.notNull(repository, "Repository cannot be null");
		Check.instanceOf(repository, ISystemSettingsRepository.class, "Repository must implement ISystemSettingsRepository");
		Check.isTrue(!((ISystemSettingsRepository) repository).existsSystemSettings(),
				"System settings already exist. Use getOrCreateSystemSettings() instead.");
		try {
			final CSystemSettings newSettings = new CSystemSettings();
			final CSystemSettings savedSettings = repository.saveAndFlush(newSettings);
			return savedSettings;
		} catch (final Exception e) {
			LOGGER.error("Failed to create default system settings", e);
			throw e;
		}
	}

	/** Gets the allowed file extensions.
	 * @return array of allowed file extensions */
	public String[] getAllowedFileExtensions() {
		final Optional<String> result = Optional.ofNullable(((CSystemSettings) repository).getAllowedFileExtensions());
		final String extensions = result.orElse(".pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg,.txt,.zip");
		final String[] extensionArray = extensions.split(",");
		// Trim whitespace from each extension
		for (int i = 0; i < extensionArray.length; i++) {
			extensionArray[i] = extensionArray[i].trim();
		}
		return extensionArray;
	}

	/** Gets the default login view setting.
	 * @return the default view to navigate to after login */
	@Transactional (readOnly = false)
	public String getDefaultLoginView() {
		final CSystemSettings settings = getOrCreateSystemSettings();
		return settings.getDefaultLoginView() != null ? settings.getDefaultLoginView() : "home";
	}

	@Override
	public Class<CSystemSettings> getEntityClass() { return CSystemSettings.class; }

	/** Gets the font size scale setting.
	 * @return the font size scale ("small", "medium", or "large") */
	@Transactional (readOnly = false)
	public String getFontSizeScale() {
		final CSystemSettings settings = getOrCreateSystemSettings();
		final String scale = settings.getFontSizeScale();
		// Validate and return with default fallback
		if ("small".equals(scale) || "medium".equals(scale) || "large".equals(scale)) {
			return scale;
		}
		return "medium"; // Default fallback
	}

	@Override
	public Class<?> getInitializerServiceClass() { return CSystemSettingsInitializerService.class; }

	/** Gets the last visited view setting.
	 * @return the last visited view route */
	@Transactional (readOnly = false)
	public String getLastVisitedView() {
		try {
			final CSystemSettings settings = getOrCreateSystemSettings();
			return settings.getLastVisitedView() != null ? settings.getLastVisitedView() : "home";
		} catch (final Exception e) {
			LOGGER.error("Error retrieving last visited view setting", e);
			return "home"; // Default fallback
		}
	}

	/** Gets the current maintenance message if maintenance mode is enabled.
	 * @return Optional containing the maintenance message if available */
	public Optional<String> getMaintenanceMessage() {
		final Optional<String> result = Optional.ofNullable(((CSystemSettings) repository).getMaintenanceMessage());
		return result;
	}

	/** Gets the maximum file upload size in MB.
	 * @return the max file upload size, or default value if not found */
	public double getMaxFileUploadSizeMb() {
		final Optional<java.math.BigDecimal> result = Optional.ofNullable(((CSystemSettings) repository).getMaxFileUploadSizeMb());
		final double size = result.orElse(new java.math.BigDecimal("50.0")).doubleValue();
		return size;
	}

	/** Gets the maximum number of login attempts allowed.
	 * @return the max login attempts, or default value if not found */
	public int getMaxLoginAttempts() {
		final Integer result = ((CSystemSettings) repository).getMaxLoginAttempts();
		final int maxAttempts = result != null ? result : 3; // Default to 3
		return maxAttempts;// attemptsret
	}

	/** Gets the current system settings. If no settings exist, creates default system settings.
	 * @return the current CSystemSettings */
	@Transactional
	public CSystemSettings getOrCreateSystemSettings() {
		final Optional<CSystemSettings> existingSettings = ((ISystemSettingsRepository) repository).findSystemSettings();
		if (existingSettings.isPresent()) {
			return existingSettings.get();
		}
		return createDefaultSystemSettings();
	}

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceSystemSettings.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Gets the session timeout in minutes.
	 * @return the session timeout value, or default value if not found */
	public int getSessionTimeoutMinutes() {
		final Optional<Integer> result = Optional.ofNullable(((CSystemSettings) repository).getSessionTimeoutMinutes());
		final int timeout = result.orElse(60); // Default to 60 minutes
		return timeout;
	}

	/** Gets the current system settings without creating if they don't exist.
	 * @return Optional containing the CSystemSettings if found, empty otherwise */
	public Optional<CSystemSettings> getSystemSettings() {
		try {
			final Optional<CSystemSettings> result = ((ISystemSettingsRepository) repository).findSystemSettings();
			return result;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving system settings", e);
			throw e;
		}
	}

	/** Initializes a new system settings entity with default values.
	 * @param entity the newly created system settings to initialize */
	@Override
	public void initializeNewEntity(final CSystemSettings entity) {
		super.initializeNewEntity(entity);
		// System settings initialization is handled by the domain class constructor
		// Additional entity-specific initialization can be added here if needed
	}

	/** Gets the auto-login enabled setting.
	 * @return true if auto-login is enabled, false otherwise */
	@Transactional (readOnly = false)
	public boolean isAutoLoginEnabled() {
		final CSystemSettings settings = getOrCreateSystemSettings();
		return settings.getAutoLoginEnabled() != null ? settings.getAutoLoginEnabled() : false;
	}

	/** Checks if maintenance mode is currently enabled.
	 * @return true if maintenance mode is enabled, false otherwise */
	public boolean isMaintenanceModeEnabled() {
		final Optional<Boolean> result = Optional.ofNullable(((CSystemSettings) repository).isMaintenanceModeEnabled());
		final boolean maintenanceMode = result.orElse(false);
		return maintenanceMode;
	}

	/** Checks if strong passwords are required.
	 * @return true if strong passwords are required, false otherwise */
	public boolean isStrongPasswordsRequired() {
		final Optional<Boolean> result = Optional.of(((CSystemSettingsService) repository).isStrongPasswordsRequired());
		final boolean required = result.orElse(true); // Default to true for security
		return required;
	}

	/** Checks if system settings have been initialized. Used to determine if initial setup is needed.
	 * @return true if system settings exist, false otherwise */
	public boolean isSystemInitialized() {
		final boolean initialized = ((ISystemSettingsRepository) repository).existsSystemSettings();
		return initialized;
	}

	/** Enables or disables maintenance mode.
	 * @param enabled true to enable maintenance mode, false to disable
	 * @param message the maintenance message to display (optional)
	 * @return the updated CSystemSettings */
	@Transactional
	public CSystemSettings setMaintenanceMode(final boolean enabled, final String message) {
		final CSystemSettings settings = getOrCreateSystemSettings();
		settings.setMaintenanceModeEnabled(enabled);
		if (message != null && !message.trim().isEmpty()) {
			settings.setMaintenanceMessage(message.trim());
		}
		final CSystemSettings updatedSettings = updateSystemSettings(settings);
		return updatedSettings;
	}

	/** Updates the auto-login settings.
	 * @param autoLoginEnabled true to enable auto-login, false to disable
	 * @param defaultView      the default view to navigate to after login
	 * @return the updated CSystemSettings */
	@Transactional
	public CSystemSettings updateAutoLoginSettings(final boolean autoLoginEnabled, final String defaultView) {
		final CSystemSettings settings = getOrCreateSystemSettings();
		settings.setAutoLoginEnabled(autoLoginEnabled);
		if (defaultView != null && !defaultView.trim().isEmpty()) {
			settings.setDefaultLoginView(defaultView.trim());
		}
		return updateSystemSettings(settings);
	}

	/** Updates the last visited view setting.
	 * @param lastVisitedView the route of the last visited view
	 * @return the updated CSystemSettings */
	@Transactional
	public CSystemSettings updateLastVisitedView(final String lastVisitedView) {
		final CSystemSettings settings = getOrCreateSystemSettings();
		if (lastVisitedView != null && !lastVisitedView.trim().isEmpty()) {
			settings.setLastVisitedView(lastVisitedView.trim());
		}
		return updateSystemSettings(settings);
	}

	/** Updates system settings with validation.
	 * @param settings the settings to update
	 * @return the updated CSystemSettings
	 * @throws IllegalArgumentException if settings is null or invalid
	 * @throws EntityNotFoundException  if settings don't exist in database */
	@Transactional
	public CSystemSettings updateSystemSettings(final CSystemSettings settings) {
		Check.notNull(repository, "Repository cannot be null");
		Check.instanceOf(repository, ISystemSettingsRepository.class, "Repository must implement ISystemSettingsRepository");
		Check.notNull(settings, "System settings cannot be null");
		Check.notNull(settings.getId(), "System settings must have an ID for update operation");
		// Validate business rules
		validateSystemSettingsBusinessRules(settings);
		try {
			// Check if entity exists
			Check.isTrue(repository.existsById(settings.getId()), "System settings do not exist. Use createDefaultSystemSettings() to create.");
			final CSystemSettings updatedSettings = repository.saveAndFlush(settings);
			return updatedSettings;
		} catch (final Exception e) {
			LOGGER.error("Failed to update system settings", e);
			throw e;
		}
	}
}
