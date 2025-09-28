package tech.derbent.setup.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.IAbstractRepository;
import tech.derbent.setup.domain.CSystemSettings;

/** CSystemSettingsRepository - Data access layer for CSystemSettings entities. Layer: Service (MVC) - Repository interface Provides database access
 * methods for system-wide configuration settings. Since system settings are singleton by design, most operations focus on retrieving and updating the
 * single system settings record. */
public interface ISystemSettingsRepository extends IAbstractRepository<CSystemSettings> {

	/** Finds the current system settings. Since there should be only one system settings record, this returns the first one.
	 * @return Optional containing the CSystemSettings if found, empty otherwise */
	@Query ("SELECT ss FROM CSystemSettings ss ORDER BY ss.id LIMIT 1")
	Optional<CSystemSettings> findSystemSettings();
	/** Finds system settings by application name.
	 * @param applicationName the name of the application
	 * @return Optional containing the CSystemSettings if found, empty otherwise */
	@Query ("SELECT ss FROM CSystemSettings ss WHERE ss.applicationName = :applicationName")
	Optional<CSystemSettings> findByApplicationName(@Param ("applicationName") String applicationName);
	/** Finds system settings by application version.
	 * @param applicationVersion the version of the application
	 * @return Optional containing the CSystemSettings if found, empty otherwise */
	@Query ("SELECT ss FROM CSystemSettings ss WHERE ss.applicationVersion = :applicationVersion")
	Optional<CSystemSettings> findByApplicationVersion(@Param ("applicationVersion") String applicationVersion);
	/** Checks if maintenance mode is currently enabled.
	 * @return true if maintenance mode is enabled, false otherwise */
	@Query ("SELECT ss.maintenanceModeEnabled FROM CSystemSettings ss ORDER BY ss.id LIMIT 1")
	Optional<Boolean> isMaintenanceModeEnabled();
	/** Gets the current maintenance message if maintenance mode is enabled.
	 * @return Optional containing the maintenance message if available */
	@Query ("SELECT ss.maintenanceMessage FROM CSystemSettings ss WHERE ss.maintenanceModeEnabled = true ORDER BY ss.id LIMIT 1")
	Optional<String> getMaintenanceMessage();
	/** Checks if automatic backups are enabled.
	 * @return true if automatic backups are enabled, false otherwise */
	@Query ("SELECT ss.enableAutomaticBackups FROM CSystemSettings ss ORDER BY ss.id LIMIT 1")
	Optional<Boolean> isAutomaticBackupsEnabled();
	/** Gets the backup schedule cron expression.
	 * @return Optional containing the backup schedule cron expression if available */
	@Query ("SELECT ss.backupScheduleCron FROM CSystemSettings ss ORDER BY ss.id LIMIT 1")
	Optional<String> getBackupScheduleCron();
	/** Gets the session timeout in minutes.
	 * @return Optional containing the session timeout value if available */
	@Query ("SELECT ss.sessionTimeoutMinutes FROM CSystemSettings ss ORDER BY ss.id LIMIT 1")
	Optional<Integer> getSessionTimeoutMinutes();
	/** Gets the maximum file upload size in MB.
	 * @return Optional containing the max file upload size if available */
	@Query ("SELECT ss.maxFileUploadSizeMb FROM CSystemSettings ss ORDER BY ss.id LIMIT 1")
	Optional<java.math.BigDecimal> getMaxFileUploadSizeMb();
	/** Gets the allowed file extensions as a comma-separated string.
	 * @return Optional containing the allowed file extensions if available */
	@Query ("SELECT ss.allowedFileExtensions FROM CSystemSettings ss ORDER BY ss.id LIMIT 1")
	Optional<String> getAllowedFileExtensions();
	/** Checks if strong passwords are required.
	 * @return true if strong passwords are required, false otherwise */
	@Query ("SELECT ss.requireStrongPasswords FROM CSystemSettings ss ORDER BY ss.id LIMIT 1")
	Optional<Boolean> isStrongPasswordsRequired();
	/** Gets the maximum number of login attempts allowed.
	 * @return Optional containing the max login attempts if available */
	@Query ("SELECT ss.maxLoginAttempts FROM CSystemSettings ss ORDER BY ss.id LIMIT 1")
	Optional<Integer> getMaxLoginAttempts();
	/** Checks if any system settings record exists. Used to determine if initial setup is needed.
	 * @return true if system settings exist, false otherwise */
	@Query ("SELECT COUNT(ss) > 0 FROM CSystemSettings ss")
	boolean existsSystemSettings();
	/** Gets the system email configuration for sending notifications.
	 * @return Optional containing the system email from address if available */
	@Query ("SELECT ss.systemEmailFrom FROM CSystemSettings ss ORDER BY ss.id LIMIT 1")
	Optional<String> getSystemEmailFrom();
	/** Checks if database logging is enabled.
	 * @return true if database logging is enabled, false otherwise */
	@Query ("SELECT ss.enableDatabaseLogging FROM CSystemSettings ss ORDER BY ss.id LIMIT 1")
	Optional<Boolean> isDatabaseLoggingEnabled();
}
