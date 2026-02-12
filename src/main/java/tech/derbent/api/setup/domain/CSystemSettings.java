package tech.derbent.api.setup.domain;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;

/** CSystemSettings - Abstract base class for system-wide configuration settings. Layer: Domain (MVC) This abstract entity stores core
 * application-level configurations that apply across the entire system regardless of company, including basic application metadata, security
 * settings, file management, email configuration, and system maintenance preferences. Concrete implementations: CSystemSettings_Derbent,
 * CSystemSettings_Bab */
@jakarta.persistence.MappedSuperclass
public abstract class CSystemSettings<EntityClass extends CSystemSettings<EntityClass>> extends CEntityDB<EntityClass> {

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
	@NotNull (message = "Database name cannot be null")
	@NotBlank (message = "Database name cannot be blank")
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
	// Email Configuration - Comprehensive email settings
	@Column (name = "email_administrator", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Administrator Email", required = false, readOnly = false, defaultValue = "admin@example.com",
			description = "Email address of system administrator for notifications and alerts", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String emailAdministrator = "yasin.yilmaz@ecemtag.com.tr";
	@Column (name = "email_end_of_line_format", nullable = true, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "End of Line Format", required = false, readOnly = false, defaultValue = "DEFAULT",
			description = "End of line format for email content (DEFAULT, CRLF, LF, CR)", hidden = false, maxLength = 50
	)
	private String emailEndOfLineFormat = "DEFAULT";
	@Column (name = "email_from", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "'From' Email", required = false, readOnly = false, defaultValue = "info@ecemtag.com.tr",
			description = "Default 'from' email address when sending system emails", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String emailFrom = "info@ecemtag.com.tr";
	@Column (name = "email_reply_to", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "'Reply-To' Email", required = false, readOnly = false, defaultValue = "info@ecemtag.com.tr",
			description = "'Reply-to' email address for system emails", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String emailReplyTo = "info@ecemtag.com.tr";
	@Column (name = "email_sender_name", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Sender Name", required = false, readOnly = false, defaultValue = "Derbent System",
			description = "Display name for sender and reply-to", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String emailSenderName = "Derbent System";
	@Column (name = "embed_images_in_emails", nullable = false)
	@AMetaData (
			displayName = "Embed Images in Emails", required = true, readOnly = false, defaultValue = "false",
			description = "Embed images inline in HTML emails instead of linking", hidden = false
	)
	private Boolean embedImagesInEmails = Boolean.FALSE;
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
	// LDAP Authentication Settings
	@Column (name = "enable_ldap_authentication", nullable = false)
	@AMetaData (
			displayName = "Enable LDAP Authentication", required = true, readOnly = false, defaultValue = "false",
			description = "Enable LDAP authentication for users marked as LDAP users", hidden = false
	)
	private Boolean enableLdapAuthentication = true;
	@Column (name = "file_storage_path", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "File Storage Path", required = false, readOnly = false, defaultValue = "./uploads",
			description = "Base path for file storage", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String fileStoragePath = "./uploads";
	@Column (name = "font_size_scale", nullable = false, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Font Size Scale", required = true, readOnly = false, defaultValue = "medium",
			description = "Font size scale for the application UI (small, medium, large)", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME, dataProviderBean = "CFontSizeService", dataProviderMethod = "getAvailableFontSizeScales"
	)
	private String fontSizeScale = "medium";
	@Column (name = "last_visited_view", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Last Visited View", required = false, readOnly = false, defaultValue = "home",
			description = "Last visited view route for quick access", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String lastVisitedView = "home";
	@Column (name = "ldap_auto_allocate_project_id", nullable = true)
	@AMetaData (
			displayName = "Project to Allocate Automatically", required = false, readOnly = false,
			description = "Project to automatically assign new LDAP users to", hidden = false, dataProviderBean = "CProjectService",
			dataProviderMethod = "getAllProjects"
	)
	private Long ldapAutoAllocateProjectId;
	@Column (name = "ldap_bind_dn", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "LDAP User", required = false, readOnly = false, defaultValue = "CN=ldap,CN=Users,DC=ECEMTAG,DC=LOCAL",
			description = "LDAP user credential for bind (e.g., CN=ldap,CN=Users,DC=ECEMTAG,DC=LOCAL)", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String ldapBindDn = "CN=ldap,CN=Users,DC=ECEMTAG,DC=LOCAL";
	@Column (name = "ldap_bind_password", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "LDAP Bind Password", required = false, readOnly = false, passwordField = true,
			description = "Password for LDAP bind DN (stored securely)", hidden = false, passwordRevealButton = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String ldapBindPassword = "ysn605ysn";
	@Column (name = "ldap_default_user_profile", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Default Profile for LDAP Users", required = false, readOnly = false, defaultValue = "Project Member",
			description = "Default user profile assigned to new LDAP users", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME,
			dataProviderBean = "CUserProfileService", dataProviderMethod = "getAllUserProfiles"
	)
	private String ldapDefaultUserProfile = "Project Member";
	@Column (name = "ldap_search_base", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "LDAP Base DN", required = false, readOnly = false, defaultValue = "cn=Users,dc=ECEMTAG,dc=LOCAL",
			description = "LDAP search base DN (e.g., cn=Users,dc=ECEMTAG,dc=LOCAL)", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String ldapSearchBase = "cn=Users,dc=ECEMTAG,dc=LOCAL";
	@Column (name = "ldap_server_url", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "LDAP Server URL", required = false, readOnly = false, defaultValue = "ldap://dc:389",
			description = "LDAP server URL (e.g., ldap://ldap.example.com:389 or ldaps://ldap.example.com:636)", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String ldapServerUrl = "ldap://dc:389";
	@Column (name = "ldap_user_creation_action", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "On LDAP User Creation", required = false, readOnly = false, defaultValue = "set the user as a resource",
			description = "Action to take when creating new LDAP user", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME,
			dataProviderBean = "CLdapUserActionService", dataProviderMethod = "getUserCreationActions"
	)
	private String ldapUserCreationAction = "set the user as a resource";
	@Column (name = "ldap_user_creation_message_type", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Message on Creation New User from LDAP", required = false, readOnly = false, defaultValue = "Internal alert",
			description = "Type of notification sent when creating new LDAP user", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME,
			dataProviderBean = "CNotificationTypeService", dataProviderMethod = "getMessageTypes"
	)
	private String ldapUserCreationMessageType = "Internal alert";
	@Column (name = "ldap_user_filter", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "LDAP User Filter", required = false, readOnly = false, defaultValue = "sAMAccountName=%USERNAME%",
			description = "LDAP user search filter (%USERNAME% is replaced with username, e.g., sAMAccountName=%USERNAME% or uid=%USERNAME%)",
			hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String ldapUserFilter = "sAMAccountName=%USERNAME%";
	@Column (name = "ldap_use_ssl_tls", nullable = false)
	@AMetaData (
			displayName = "Use SSL over TLS", required = true, readOnly = false, defaultValue = "false",
			description = "Use SSL/TLS for LDAP connection security", hidden = false
	)
	private Boolean ldapUseSslTls = Boolean.FALSE;
	@Column (name = "ldap_version", nullable = false)
	@Min (value = 2, message = "LDAP version must be at least 2")
	@Max (value = 3, message = "LDAP version must be 2 or 3")
	@AMetaData (
			displayName = "LDAP Version", required = true, readOnly = false, defaultValue = "3", description = "LDAP protocol version (2 or 3)",
			hidden = false
	)
	private Integer ldapVersion = 3;
	@Column (name = "mailer_type", nullable = true, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Mailer Type", required = false, readOnly = false, defaultValue = "SMTP",
			description = "Email sending method (SMTP, SENDMAIL, QUEUE_ONLY)", hidden = false, maxLength = 50
	)
	private String mailerType = "SMTP";
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
	@Column (name = "max_attachment_size_mb", nullable = false)
	@Min (value = 1, message = "Max attachment size must be at least 1 MB")
	@Max (value = 50, message = "Max attachment size cannot exceed 50 MB")
	@AMetaData (
			displayName = "Max Attachment Size (MB)", required = true, readOnly = false, defaultValue = "5",
			description = "Maximum total size of all attachments per email in megabytes", hidden = false
	)
	private Integer maxAttachmentSizeMb = 5;
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
	// Email Test Component - Transient placeholder for UI component
	@Transient
	@AMetaData (
			displayName = "Email Test", required = false, readOnly = false, description = "Test email configuration and SMTP settings",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentEmailTest", captionVisible = false
	)
	private final CSystemSettings<?> placeHolder_createComponentEmailTest = null;
	// LDAP Test Component - Transient placeholder for UI component
	@Transient
	@AMetaData (
			displayName = "LDAP Test", required = false, readOnly = false, description = "Test LDAP authentication settings", hidden = false,
			dataProviderBean = "pageservice", createComponentMethod = "createComponentLdapTest", captionVisible = false
	)
	private final CSystemSettings<?> placeHolder_createComponentLdapTest = null;
	@Column (name = "require_strong_passwords", nullable = false)
	@AMetaData (
			displayName = "Require Strong Passwords", required = true, readOnly = false, defaultValue = "true",
			description = "Enforce strong password requirements", hidden = false
	)
	private Boolean requireStrongPasswords = Boolean.TRUE;
	@Column (name = "send_emails_as_current_user", nullable = false)
	@AMetaData (
			displayName = "Send Emails as Current User", required = true, readOnly = false, defaultValue = "false",
			description = "Use current logged-in user's email as 'from' address", hidden = false
	)
	private Boolean sendEmailsAsCurrentUser = Boolean.FALSE;
	@Column (name = "sendmail_path", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "Sendmail Path", required = false, readOnly = false, defaultValue = "/usr/sbin/sendmail",
			description = "Path to sendmail binary (if using sendmail mailer)", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String sendmailPath = "/usr/sbin/sendmail";
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
	@Column (name = "smtp_login_name", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "SMTP Login Name", required = false, readOnly = false, defaultValue = "info@ecemtag.com.tr",
			description = "SMTP authentication username (usually email address)", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String smtpLoginName = "info@ecemtag.com.tr";
	@Column (name = "smtp_login_password", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "SMTP Login Password", required = false, readOnly = false, passwordField = true,
			description = "SMTP authentication password (will be encrypted in database)", hidden = false, passwordRevealButton = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String smtpLoginPassword = "b43J3URW!";
	@Column (name = "smtp_port", nullable = false)
	@Min (value = 1, message = "SMTP port must be positive")
	@Max (value = 65535, message = "SMTP port must be valid")
	@AMetaData (
			displayName = "SMTP Port", required = true, readOnly = false, defaultValue = "587",
			description = "SMTP server port (587 for TLS, 465 for SSL, 25 for unencrypted)", hidden = false
	)
	private Integer smtpPort = 587;
	@Column (name = "smtp_send_helo_with_ip", nullable = false)
	@AMetaData (
			displayName = "Send HELO with Current IP", required = true, readOnly = false, defaultValue = "false",
			description = "Send HELO command with current server IP address", hidden = false
	)
	private Boolean smtpSendHeloWithIp = Boolean.FALSE;
	@Column (name = "smtp_server", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "SMTP Server", required = false, readOnly = false, defaultValue = "smtp.office365.com",
			description = "SMTP server hostname (e.g., smtp.office365.com, smtp.gmail.com)", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String smtpServer = "smtp.office365.com";
	@Column (name = "smtp_use_tls", nullable = false)
	@AMetaData (
			displayName = "SMTP Use TLS", required = true, readOnly = false, defaultValue = "true",
			description = "Use TLS encryption for SMTP connection (recommended for port 587)", hidden = false
	)
	private Boolean smtpUseTls = Boolean.TRUE;
	@Column (name = "support_email", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Support Email", required = false, readOnly = false, defaultValue = "support@example.com",
			description = "Support contact email displayed to users", hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String supportEmail = "info@ecemtag.com.tr";
	@Column (name = "system_email_from", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "System Email From", required = false, readOnly = false, defaultValue = "noreply@example.com",
			description = "Legacy field - use 'From' Email instead", hidden = true, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String systemEmailFrom = "info@ecemtag.com.tr";

	/** Default constructor for JPA. */
	protected CSystemSettings() {}

	/** Business constructor for creating new system settings. */
	protected CSystemSettings(final Class<EntityClass> clazz, final String applicationName) {
		super(clazz);
		initializeDefaults();
		this.applicationName = applicationName;
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

	public String getEmailAdministrator() { return emailAdministrator; }

	public String getEmailEndOfLineFormat() { return emailEndOfLineFormat; }

	public String getEmailFrom() { return emailFrom; }

	public String getEmailReplyTo() { return emailReplyTo; }

	public String getEmailSenderName() { return emailSenderName; }

	public Boolean getEmbedImagesInEmails() { return embedImagesInEmails; }

	public Boolean getEnableAutomaticBackups() { return enableAutomaticBackups; }

	public Boolean getEnableCaching() { return enableCaching; }

	public Boolean getEnableDarkMode() { return enableDarkMode; }

	public Boolean getEnableDatabaseLogging() { return enableDatabaseLogging; }

	public Boolean getEnableFileVersioning() { return enableFileVersioning; }

	public Boolean getEnableLdapAuthentication() { return enableLdapAuthentication; }

	public String getFileStoragePath() { return fileStoragePath; }

	public String getFontSizeScale() { return fontSizeScale; }

	public String getLastVisitedView() { return lastVisitedView; }

	public Long getLdapAutoAllocateProjectId() { return ldapAutoAllocateProjectId; }

	public String getLdapBindDn() { return ldapBindDn; }

	public String getLdapBindPassword() { return ldapBindPassword; }

	public String getLdapDefaultUserProfile() { return ldapDefaultUserProfile; }

	public String getLdapSearchBase() { return ldapSearchBase; }

	public String getLdapServerUrl() { return ldapServerUrl; }

	public String getLdapUserCreationAction() { return ldapUserCreationAction; }

	public String getLdapUserCreationMessageType() { return ldapUserCreationMessageType; }

	public String getLdapUserFilter() { return ldapUserFilter; }

	public Boolean getLdapUseSslTls() { return ldapUseSslTls; }

	public Integer getLdapVersion() { return ldapVersion; }

	public String getMailerType() { return mailerType; }

	public String getMaintenanceMessage() { return maintenanceMessage; }

	public Boolean getMaintenanceModeEnabled() { return maintenanceModeEnabled; }

	public Integer getMaxAttachmentSizeMb() { return maxAttachmentSizeMb; }

	public BigDecimal getMaxFileUploadSizeMb() { return maxFileUploadSizeMb; }

	public Integer getMaxLoginAttempts() { return maxLoginAttempts; }

	public Integer getPasswordExpiryDays() { return passwordExpiryDays; }

	/** Getter for transient email test placeholder field - returns entity itself for component binding. Following CSystemSettings pattern: transient
	 * entity-typed field with getter returning 'this'. CRITICAL: Binder needs this getter to bind the component. Component receives full entity via
	 * initialization, not via setValue().
	 * @return this entity (for CFormBuilder binding to email test component) */
	public CSystemSettings<?> getPlaceHolder_createComponentEmailTest() {
		return this; // Returns entity itself, NOT the field value!
	}

	/** Getter for transient LDAP test placeholder field - returns entity itself for component binding. Following CSystemSettings pattern: transient
	 * entity-typed field with getter returning 'this'. CRITICAL: Binder needs this getter to bind the component. Component receives full entity via
	 * initialization, not via setValue().
	 * @return this entity (for CFormBuilder binding to LDAP test component) */
	public CSystemSettings<?> getPlaceHolder_createComponentLdapTest() {
		return this; // Returns entity itself, NOT the field value!
	}

	public Boolean getRequireStrongPasswords() { return requireStrongPasswords; }

	public Boolean getSendEmailsAsCurrentUser() { return sendEmailsAsCurrentUser; }

	public String getSendmailPath() { return sendmailPath; }

	public Integer getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }

	public Boolean getShowSystemInfo() { return showSystemInfo; }

	public String getSmtpLoginName() { return smtpLoginName; }

	public String getSmtpLoginPassword() { return smtpLoginPassword; }

	public Integer getSmtpPort() { return smtpPort; }

	public Boolean getSmtpSendHeloWithIp() { return smtpSendHeloWithIp; }

	public String getSmtpServer() { return smtpServer; }

	public Boolean getSmtpUseTls() { return smtpUseTls; }

	public String getSupportEmail() { return supportEmail; }

	public String getSystemEmailFrom() { return systemEmailFrom; }
	// Abstract initializeDefaults - implemented by subclasses
	// No implementation here - each concrete class implements

	private final void initializeDefaults() {}

	public Boolean isAutoLoginEnabled() { return autoLoginEnabled; }

	public Boolean isEmbedImagesInEmails() { return embedImagesInEmails; }

	public Boolean isEnableAutomaticBackups() { return enableAutomaticBackups; }

	public Boolean isEnableCaching() { return enableCaching; }

	public Boolean isEnableDarkMode() { return enableDarkMode; }

	public Boolean isEnableDatabaseLogging() { return enableDatabaseLogging; }

	public Boolean isEnableFileVersioning() { return enableFileVersioning; }

	public Boolean isEnableLdapAuthentication() { return enableLdapAuthentication; }

	public Boolean isLdapUseSslTls() { return ldapUseSslTls; }

	public Boolean isMaintenanceModeEnabled() { return maintenanceModeEnabled; }

	public Boolean isRequireStrongPasswords() { return requireStrongPasswords; }

	public Boolean isSendEmailsAsCurrentUser() { return sendEmailsAsCurrentUser; }

	public Boolean isShowSystemInfo() { return showSystemInfo; }

	public Boolean isSmtpSendHeloWithIp() { return smtpSendHeloWithIp; }

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

	public void setEmailAdministrator(final String emailAdministrator) { this.emailAdministrator = emailAdministrator; }

	public void setEmailEndOfLineFormat(final String emailEndOfLineFormat) { this.emailEndOfLineFormat = emailEndOfLineFormat; }

	public void setEmailFrom(final String emailFrom) { this.emailFrom = emailFrom; }

	public void setEmailReplyTo(final String emailReplyTo) { this.emailReplyTo = emailReplyTo; }

	public void setEmailSenderName(final String emailSenderName) { this.emailSenderName = emailSenderName; }

	public void setEmbedImagesInEmails(final Boolean embedImagesInEmails) { this.embedImagesInEmails = embedImagesInEmails; }

	public void setEnableAutomaticBackups(final Boolean enableAutomaticBackups) { this.enableAutomaticBackups = enableAutomaticBackups; }

	public void setEnableCaching(final Boolean enableCaching) { this.enableCaching = enableCaching; }

	public void setEnableDarkMode(final Boolean enableDarkMode) { this.enableDarkMode = enableDarkMode; }

	public void setEnableDatabaseLogging(final Boolean enableDatabaseLogging) { this.enableDatabaseLogging = enableDatabaseLogging; }

	public void setEnableFileVersioning(final Boolean enableFileVersioning) { this.enableFileVersioning = enableFileVersioning; }

	public void setEnableLdapAuthentication(final Boolean enableLdapAuthentication) { this.enableLdapAuthentication = enableLdapAuthentication; }

	public void setFileStoragePath(final String fileStoragePath) { this.fileStoragePath = fileStoragePath; }

	public void setFontSizeScale(final String fontSizeScale) { this.fontSizeScale = fontSizeScale; }

	public void setLastVisitedView(final String lastVisitedView) { this.lastVisitedView = lastVisitedView; }

	public void setLdapAutoAllocateProjectId(final Long ldapAutoAllocateProjectId) { this.ldapAutoAllocateProjectId = ldapAutoAllocateProjectId; }

	public void setLdapBindDn(final String ldapBindDn) { this.ldapBindDn = ldapBindDn; }

	public void setLdapBindPassword(final String ldapBindPassword) { this.ldapBindPassword = ldapBindPassword; }

	public void setLdapDefaultUserProfile(final String ldapDefaultUserProfile) { this.ldapDefaultUserProfile = ldapDefaultUserProfile; }

	public void setLdapSearchBase(final String ldapSearchBase) { this.ldapSearchBase = ldapSearchBase; }

	public void setLdapServerUrl(final String ldapServerUrl) { this.ldapServerUrl = ldapServerUrl; }

	public void setLdapUserCreationAction(final String ldapUserCreationAction) { this.ldapUserCreationAction = ldapUserCreationAction; }

	public void setLdapUserCreationMessageType(final String ldapUserCreationMessageType) {
		this.ldapUserCreationMessageType = ldapUserCreationMessageType;
	}

	public void setLdapUserFilter(final String ldapUserFilter) { this.ldapUserFilter = ldapUserFilter; }

	public void setLdapUseSslTls(final Boolean ldapUseSslTls) { this.ldapUseSslTls = ldapUseSslTls; }

	public void setLdapVersion(final Integer ldapVersion) { this.ldapVersion = ldapVersion; }

	public void setMailerType(final String mailerType) { this.mailerType = mailerType; }

	public void setMaintenanceMessage(final String maintenanceMessage) { this.maintenanceMessage = maintenanceMessage; }

	public void setMaintenanceModeEnabled(final Boolean maintenanceModeEnabled) { this.maintenanceModeEnabled = maintenanceModeEnabled; }

	public void setMaxAttachmentSizeMb(final Integer maxAttachmentSizeMb) { this.maxAttachmentSizeMb = maxAttachmentSizeMb; }

	public void setMaxFileUploadSizeMb(final BigDecimal maxFileUploadSizeMb) { this.maxFileUploadSizeMb = maxFileUploadSizeMb; }

	public void setMaxLoginAttempts(final Integer maxLoginAttempts) { this.maxLoginAttempts = maxLoginAttempts; }

	public void setPasswordExpiryDays(final Integer passwordExpiryDays) { this.passwordExpiryDays = passwordExpiryDays; }

	public void setRequireStrongPasswords(final Boolean requireStrongPasswords) { this.requireStrongPasswords = requireStrongPasswords; }

	public void setSendEmailsAsCurrentUser(final Boolean sendEmailsAsCurrentUser) { this.sendEmailsAsCurrentUser = sendEmailsAsCurrentUser; }

	public void setSendmailPath(final String sendmailPath) { this.sendmailPath = sendmailPath; }

	public void setSessionTimeoutMinutes(final Integer sessionTimeoutMinutes) { this.sessionTimeoutMinutes = sessionTimeoutMinutes; }

	public void setShowSystemInfo(final Boolean showSystemInfo) { this.showSystemInfo = showSystemInfo; }

	public void setSmtpLoginName(final String smtpLoginName) { this.smtpLoginName = smtpLoginName; }

	public void setSmtpLoginPassword(final String smtpLoginPassword) { this.smtpLoginPassword = smtpLoginPassword; }

	public void setSmtpPort(final Integer smtpPort) { this.smtpPort = smtpPort; }

	public void setSmtpSendHeloWithIp(final Boolean smtpSendHeloWithIp) { this.smtpSendHeloWithIp = smtpSendHeloWithIp; }

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
