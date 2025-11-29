package tech.derbent.base.setup.domain;

import java.math.BigDecimal;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;

/** CSystemSettings - Domain entity representing system-wide configuration settings. Layer: Domain (MVC) This entity stores application-level
 * configurations that apply across the entire system regardless of company, including application metadata, security settings, file management, email
 * configuration, and system maintenance preferences. */
@Entity
@Table (name = "csystemsettings")
@AttributeOverride (name = "id", column = @Column (name = "system_settings_id"))
public class CSystemSettings extends CEntityDB<CSystemSettings> {

	public static final String DEFAULT_COLOR = "#C3B79F"; // OpenWindows 3D Shadow - system settings
	public static final String DEFAULT_ICON = "vaadin:sliders";
	public static final String VIEW_NAME = "System Settings View";
	@Column (name = "account_lockout_duration_minutes", nullable = false)
	@Min (value = 1, message = "Lockout duration must be at least 1 minute")
	@Max (value = 1440, message = "Lockout duration cannot exceed 1440 minutes")
	@AMetaData (
			displayName = "Account Lockout Duration (Minutes)", required = true, readOnly = false, defaultValue = "15",
			description = "Account lockout duration after failed attempts", hidden = false
	)
	private Integer accountLockoutDurationMinutes = 15;
	@Column (name = "allowed_file_extensions", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "Allowed File Extensions", required = false, readOnly = false,
			defaultValue = ".pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg,.txt,.zip", description = "Comma-separated list of allowed file extensions",
			hidden = false, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String allowedFileExtensions = ".pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg,.txt,.zip";
	@Column (name = "application_description", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "Application Description", required = false, readOnly = false, defaultValue = "Comprehensive project management solution",
			description = "Brief description of the application", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String applicationDescription = "Comprehensive project management solution";
	// Application Configuration
	@Column (name = "application_name", nullable = false, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Application Name", required = true, readOnly = false, defaultValue = "Derbent Project Management",
			description = "Name of the application", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String applicationName = "Derbent Project Management";
	@Column (name = "application_version", nullable = false, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Application Version", required = true, readOnly = false, defaultValue = "1.0.0",
			description = "Current version of the application", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String applicationVersion = "1.0.0";
	// Auto-Login Settings
	@Column (name = "auto_login_enabled", nullable = false)
	@AMetaData (
			displayName = "Auto Login Enabled", required = true, readOnly = false, defaultValue = "false",
			description = "Enable automatic login after 2 seconds", hidden = false
	)
	private Boolean autoLoginEnabled = Boolean.FALSE;
	@Column (name = "backup_retention_days", nullable = false)
	@Min (value = 1, message = "Backup retention must be at least 1 day")
	@Max (value = 365, message = "Backup retention cannot exceed 365 days")
	@AMetaData (
			displayName = "Backup Retention (Days)", required = true, readOnly = false, defaultValue = "30",
			description = "Number of days to retain backups", hidden = false
	)
	private Integer backupRetentionDays = 30;
	@Column (name = "backup_schedule_cron", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Backup Schedule (Cron)", required = false, readOnly = false, defaultValue = "0 2 * * *",
			description = "Cron expression for backup schedule", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String backupScheduleCron = "0 2 * * *";
	@Column (name = "cache_ttl_minutes", nullable = false)
	@Min (value = 1, message = "Cache TTL must be at least 1 minute")
	@Max (value = 1440, message = "Cache TTL cannot exceed 1440 minutes")
	@AMetaData (
			displayName = "Cache TTL (Minutes)", required = true, readOnly = false, defaultValue = "30",
			description = "Cache time-to-live in minutes", hidden = false
	)
	private Integer cacheTtlMinutes = 30;
	@Column (name = "database_connection_pool_size", nullable = false)
	@Min (value = 1, message = "Connection pool size must be at least 1")
	@Max (value = 100, message = "Connection pool size cannot exceed 100")
	@AMetaData (
			displayName = "Database Connection Pool Size", required = true, readOnly = false, defaultValue = "10",
			description = "Maximum database connections in pool", hidden = false
	)
	private Integer databaseConnectionPoolSize = 10;
	// Database and Performance Settings
	@Column (name = "database_name", nullable = false, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME, message = "Database name cannot exceed " + CEntityConstants.MAX_LENGTH_NAME + " characters")
	@jakarta.validation.constraints.NotNull (message = "Database name cannot be null")
	@jakarta.validation.constraints.NotBlank (message = "Database name cannot be blank")
	@AMetaData (
			displayName = "Database Name", required = true, readOnly = false, defaultValue = "derbent",
			description = "Name of the database to connect to", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String databaseName = "derbent";
	@Column (name = "default_login_view", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Default Login View", required = false, readOnly = false, defaultValue = "home",
			description = "Default view to navigate to after login", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String defaultLoginView = "home";
	// UI and Theming Settings
	@Column (name = "default_system_theme", nullable = false, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Default System Theme", required = true, readOnly = false, defaultValue = "lumo",
			description = "Default UI theme for the application", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String defaultSystemTheme = "lumo";
	// Backup and Maintenance Settings
	@Column (name = "enable_automatic_backups", nullable = false)
	@AMetaData (
			displayName = "Enable Automatic Backups", required = true, readOnly = false, defaultValue = "true",
			description = "Enable scheduled database backups", hidden = false
	)
	private Boolean enableAutomaticBackups = Boolean.TRUE;
	@Column (name = "enable_caching", nullable = false)
	@AMetaData (
			displayName = "Enable Caching", required = true, readOnly = false, defaultValue = "true",
			description = "Enable application-level caching", hidden = false
	)
	private Boolean enableCaching = Boolean.TRUE;
	@Column (name = "enable_dark_mode", nullable = false)
	@AMetaData (
			displayName = "Enable Dark Mode", required = true, readOnly = false, defaultValue = "true",
			description = "Allow users to switch to dark mode", hidden = false
	)
	private Boolean enableDarkMode = Boolean.TRUE;
	@Column (name = "enable_database_logging", nullable = false)
	@AMetaData (
			displayName = "Enable Database Logging", required = true, readOnly = false, defaultValue = "false",
			description = "Enable detailed database query logging", hidden = false
	)
	private Boolean enableDatabaseLogging = Boolean.FALSE;
	@Column (name = "enable_file_versioning", nullable = false)
	@AMetaData (
			displayName = "Enable File Versioning", required = true, readOnly = false, defaultValue = "true",
			description = "Enable file version tracking", hidden = false
	)
	private Boolean enableFileVersioning = Boolean.TRUE;
	@Column (name = "file_storage_path", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "File Storage Path", required = false, readOnly = false, defaultValue = "./uploads",
			description = "Base path for file storage", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String fileStoragePath = "./uploads";
	@Column (name = "last_visited_view", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Last Visited View", required = false, readOnly = false, defaultValue = "home",
			description = "Last visited view route for quick access", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String lastVisitedView = "home";
	@Column (name = "maintenance_message", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "Maintenance Message", required = false, readOnly = false,
			defaultValue = "System is under maintenance. Please try again later.", description = "Message displayed during maintenance",
			hidden = false, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String maintenanceMessage = "System is under maintenance. Please try again later.";
	@Column (name = "maintenance_mode_enabled", nullable = false)
	@AMetaData (
			displayName = "Maintenance Mode", required = true, readOnly = false, defaultValue = "false", description = "Enable maintenance mode",
			hidden = false
	)
	private Boolean maintenanceModeEnabled = Boolean.FALSE;
	// File Management Settings
	@Column (name = "max_file_upload_size_mb", nullable = false, precision = 8, scale = 2)
	@DecimalMin (value = "0.1", message = "Max file size must be at least 0.1 MB")
	@AMetaData (
			displayName = "Max File Upload Size (MB)", required = true, readOnly = false, defaultValue = "50.0",
			description = "Maximum file upload size in megabytes", hidden = false
	)
	private BigDecimal maxFileUploadSizeMb = new BigDecimal("50.0");
	@Column (name = "max_login_attempts", nullable = false)
	@Min (value = 1, message = "Max login attempts must be at least 1")
	@Max (value = 10, message = "Max login attempts cannot exceed 10")
	@AMetaData (
			displayName = "Max Login Attempts", required = true, readOnly = false, defaultValue = "3",
			description = "Maximum failed login attempts before lockout", hidden = false
	)
	private Integer maxLoginAttempts = 3;
	@Column (name = "password_expiry_days", nullable = true)
	@Min (value = 1, message = "Password expiry must be at least 1 day")
	@Max (value = 365, message = "Password expiry cannot exceed 365 days")
	@AMetaData (
			displayName = "Password Expiry (Days)", required = false, readOnly = false, defaultValue = "90",
			description = "Password expiration in days (null for no expiry)", hidden = false
	)
	private Integer passwordExpiryDays = 90;
	@Column (name = "require_strong_passwords", nullable = false)
	@AMetaData (
			displayName = "Require Strong Passwords", required = true, readOnly = false, defaultValue = "true",
			description = "Enforce strong password requirements", hidden = false
	)
	private Boolean requireStrongPasswords = Boolean.TRUE;
	// Security and Session Settings
	@Column (name = "session_timeout_minutes", nullable = false)
	@Min (value = 5, message = "Session timeout must be at least 5 minutes")
	@Max (value = 1440, message = "Session timeout cannot exceed 1440 minutes (24 hours)")
	@AMetaData (
			displayName = "Session Timeout (Minutes)", required = true, readOnly = false, defaultValue = "60",
			description = "User session timeout in minutes", hidden = false
	)
	private Integer sessionTimeoutMinutes = 60;
	@Column (name = "show_system_info", nullable = false)
	@AMetaData (
			displayName = "Show System Info", required = true, readOnly = false, defaultValue = "true",
			description = "Display system information to administrators", hidden = false
	)
	private Boolean showSystemInfo = Boolean.TRUE;
	@Column (name = "smtp_port", nullable = false)
	@Min (value = 1, message = "SMTP port must be positive")
	@Max (value = 65535, message = "SMTP port must be valid")
	@AMetaData (displayName = "SMTP Port", required = true, readOnly = false, defaultValue = "587", description = "SMTP server port", hidden = false)
	private Integer smtpPort = 587;
	// Email Configuration
	@Column (name = "smtp_server", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "SMTP Server", required = false, readOnly = false, defaultValue = "localhost", description = "SMTP server hostname",
			hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String smtpServer = "localhost";
	@Column (name = "smtp_use_tls", nullable = false)
	@AMetaData (
			displayName = "SMTP Use TLS", required = true, readOnly = false, defaultValue = "true", description = "Use TLS for SMTP connection",
			hidden = false
	)
	private Boolean smtpUseTls = Boolean.TRUE;
	@Column (name = "support_email", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Support Email", required = false, readOnly = false, defaultValue = "support@derbent.tech",
			description = "Support contact email", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String supportEmail = "support@derbent.tech";
	@Column (name = "system_email_from", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "System Email From", required = false, readOnly = false, defaultValue = "noreply@derbent.tech",
			description = "Default from email address", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String systemEmailFrom = "noreply@derbent.tech";

	/** Default constructor required by JPA. Initializes entity with default values. */
	public CSystemSettings() {
		super(CSystemSettings.class);
		initializeDefaults();
	}

	public Integer getAccountLockoutDurationMinutes() { return accountLockoutDurationMinutes; }
	// Getters and setters following the existing pattern

	public String getAllowedFileExtensions() { return allowedFileExtensions; }

	public String getApplicationDescription() { return applicationDescription; }

	public String getApplicationName() { return applicationName; }

	public String getApplicationVersion() { return applicationVersion; }

	public Boolean getAutoLoginEnabled() { return autoLoginEnabled; }

	public Integer getBackupRetentionDays() { return backupRetentionDays; }

	public String getBackupScheduleCron() { return backupScheduleCron; }

	public Integer getCacheTtlMinutes() { return cacheTtlMinutes; }

	public Integer getDatabaseConnectionPoolSize() { return databaseConnectionPoolSize; }

	public String getDatabaseName() { return databaseName; }

	public String getDefaultLoginView() { return defaultLoginView; }

	public String getDefaultSystemTheme() { return defaultSystemTheme; }

	public Boolean getEnableAutomaticBackups() { return enableAutomaticBackups; }

	public Boolean getEnableCaching() { return enableCaching; }

	public Boolean getEnableDarkMode() { return enableDarkMode; }

	public Boolean getEnableDatabaseLogging() { return enableDatabaseLogging; }

	public Boolean getEnableFileVersioning() { return enableFileVersioning; }

	public String getFileStoragePath() { return fileStoragePath; }

	public String getLastVisitedView() { return lastVisitedView; }

	public String getMaintenanceMessage() { return maintenanceMessage; }

	public Boolean getMaintenanceModeEnabled() { return maintenanceModeEnabled; }

	public BigDecimal getMaxFileUploadSizeMb() { return maxFileUploadSizeMb; }

	public Integer getMaxLoginAttempts() { return maxLoginAttempts; }

	public Integer getPasswordExpiryDays() { return passwordExpiryDays; }

	public Boolean getRequireStrongPasswords() { return requireStrongPasswords; }

	public Integer getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }

	public Boolean getShowSystemInfo() { return showSystemInfo; }

	public Integer getSmtpPort() { return smtpPort; }

	public String getSmtpServer() { return smtpServer; }

	public Boolean getSmtpUseTls() { return smtpUseTls; }

	public String getSupportEmail() { return supportEmail; }

	public String getSystemEmailFrom() { return systemEmailFrom; }

	@Override
	protected void initializeDefaults() {
		if (maxFileUploadSizeMb == null) {
			maxFileUploadSizeMb = new BigDecimal("50.0");
		}
		if ((databaseName == null) || databaseName.trim().isEmpty()) {
			databaseName = "derbent";
		}
	}

	public Boolean isAutoLoginEnabled() { return autoLoginEnabled; }

	public Boolean isEnableAutomaticBackups() { return enableAutomaticBackups; }

	public Boolean isEnableCaching() { return enableCaching; }

	public Boolean isEnableDarkMode() { return enableDarkMode; }

	public Boolean isEnableDatabaseLogging() { return enableDatabaseLogging; }

	public Boolean isEnableFileVersioning() { return enableFileVersioning; }

	public Boolean isMaintenanceModeEnabled() { return maintenanceModeEnabled; }

	public Boolean isRequireStrongPasswords() { return requireStrongPasswords; }

	public Boolean isShowSystemInfo() { return showSystemInfo; }

	public Boolean isSmtpUseTls() { return smtpUseTls; }

	public void setAccountLockoutDurationMinutes(final Integer accountLockoutDurationMinutes) {
		this.accountLockoutDurationMinutes = accountLockoutDurationMinutes;
	}

	public void setAllowedFileExtensions(final String allowedFileExtensions) { this.allowedFileExtensions = allowedFileExtensions; }

	public void setApplicationDescription(final String applicationDescription) { this.applicationDescription = applicationDescription; }

	public void setApplicationName(final String applicationName) { this.applicationName = applicationName; }

	public void setApplicationVersion(final String applicationVersion) { this.applicationVersion = applicationVersion; }

	public void setAutoLoginEnabled(final Boolean autoLoginEnabled) { this.autoLoginEnabled = autoLoginEnabled; }

	public void setBackupRetentionDays(final Integer backupRetentionDays) { this.backupRetentionDays = backupRetentionDays; }

	public void setBackupScheduleCron(final String backupScheduleCron) { this.backupScheduleCron = backupScheduleCron; }

	public void setCacheTtlMinutes(final Integer cacheTtlMinutes) { this.cacheTtlMinutes = cacheTtlMinutes; }

	public void setDatabaseConnectionPoolSize(final Integer databaseConnectionPoolSize) {
		this.databaseConnectionPoolSize = databaseConnectionPoolSize;
	}

	public void setDatabaseName(final String databaseName) { this.databaseName = databaseName; }

	public void setDefaultLoginView(final String defaultLoginView) { this.defaultLoginView = defaultLoginView; }

	public void setDefaultSystemTheme(final String defaultSystemTheme) { this.defaultSystemTheme = defaultSystemTheme; }

	public void setEnableAutomaticBackups(final Boolean enableAutomaticBackups) { this.enableAutomaticBackups = enableAutomaticBackups; }

	public void setEnableCaching(final Boolean enableCaching) { this.enableCaching = enableCaching; }

	public void setEnableDarkMode(final Boolean enableDarkMode) { this.enableDarkMode = enableDarkMode; }

	public void setEnableDatabaseLogging(final Boolean enableDatabaseLogging) { this.enableDatabaseLogging = enableDatabaseLogging; }

	public void setEnableFileVersioning(final Boolean enableFileVersioning) { this.enableFileVersioning = enableFileVersioning; }

	public void setFileStoragePath(final String fileStoragePath) { this.fileStoragePath = fileStoragePath; }

	public void setLastVisitedView(final String lastVisitedView) { this.lastVisitedView = lastVisitedView; }

	public void setMaintenanceMessage(final String maintenanceMessage) { this.maintenanceMessage = maintenanceMessage; }

	public void setMaintenanceModeEnabled(final Boolean maintenanceModeEnabled) { this.maintenanceModeEnabled = maintenanceModeEnabled; }

	public void setMaxFileUploadSizeMb(final BigDecimal maxFileUploadSizeMb) { this.maxFileUploadSizeMb = maxFileUploadSizeMb; }

	public void setMaxLoginAttempts(final Integer maxLoginAttempts) { this.maxLoginAttempts = maxLoginAttempts; }

	public void setPasswordExpiryDays(final Integer passwordExpiryDays) { this.passwordExpiryDays = passwordExpiryDays; }

	public void setRequireStrongPasswords(final Boolean requireStrongPasswords) { this.requireStrongPasswords = requireStrongPasswords; }

	public void setSessionTimeoutMinutes(final Integer sessionTimeoutMinutes) { this.sessionTimeoutMinutes = sessionTimeoutMinutes; }

	public void setShowSystemInfo(final Boolean showSystemInfo) { this.showSystemInfo = showSystemInfo; }

	public void setSmtpPort(final Integer smtpPort) { this.smtpPort = smtpPort; }

	public void setSmtpServer(final String smtpServer) { this.smtpServer = smtpServer; }

	public void setSmtpUseTls(final Boolean smtpUseTls) { this.smtpUseTls = smtpUseTls; }

	public void setSupportEmail(final String supportEmail) { this.supportEmail = supportEmail; }

	public void setSystemEmailFrom(final String systemEmailFrom) { this.systemEmailFrom = systemEmailFrom; }

	@Override
	public String toString() {
		return "CSystemSettings{" + "applicationName='" + applicationName + '\'' + ", applicationVersion='" + applicationVersion + '\''
				+ ", sessionTimeoutMinutes=" + sessionTimeoutMinutes + ", maxLoginAttempts=" + maxLoginAttempts + ", maxFileUploadSizeMb="
				+ maxFileUploadSizeMb + ", databaseName='" + databaseName + '\'' + ", enableDatabaseLogging=" + enableDatabaseLogging
				+ ", maintenanceModeEnabled=" + maintenanceModeEnabled + '}';
	}
}
