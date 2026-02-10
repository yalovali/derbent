package tech.derbent.plm.setup.domain;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.setup.domain.CSystemSettings;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;

/** CSystemSettings_Derbent - Derbent PLM-specific system settings. Layer: Domain (MVC) Active when: default profile or 'derbent' profile (NOT 'bab'
 * profile) Full-featured configuration for comprehensive project management: - Complete application configuration - Advanced security and audit
 * settings - Comprehensive file and backup management - Email and notification system - Performance optimization settings - Maintenance and
 * monitoring tools - Project-specific configurations. Follows Derbent pattern: Concrete class marked final. */
@Entity
@Table (name = "csystemsettings_derbent", uniqueConstraints = {
		@jakarta.persistence.UniqueConstraint (columnNames = {
				"application_name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "system_settings_id"))
@DiscriminatorValue ("DERBENT")
public final class CSystemSettings_Derbent extends CSystemSettings<CSystemSettings_Derbent> implements IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#91856C"; // OpenWindows Border Dark - system settings
	public static final String DEFAULT_ICON = "vaadin:sliders";
	public static final String ENTITY_TITLE_PLURAL = "Derbent System Settings";
	public static final String ENTITY_TITLE_SINGULAR = "Derbent System Settings";
	public static final String VIEW_NAME = "Derbent System Settings View";
	@Column (name = "api_rate_limit_per_minute", nullable = false)
	@Min (value = 1, message = "API rate limit must be at least 1 request per minute")
	@Max (value = 10000, message = "API rate limit cannot exceed 10000 requests per minute")
	@AMetaData (
			displayName = "API Rate Limit (Per Minute)", required = true, readOnly = false, defaultValue = "100",
			description = "Maximum API requests per user per minute", hidden = false
	)
	private Integer apiRateLimitPerMinute = 100;
	// One-to-Many relationship with attachments - for system documentation
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "system_settings_id")
	@AMetaData (
			displayName = "System Documentation", required = false, readOnly = false, description = "System configuration documentation and files",
			hidden = false, dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@Column (name = "audit_log_retention_days", nullable = false)
	@Min (value = 1, message = "Audit log retention must be at least 1 day")
	@Max (value = 2555, message = "Audit log retention cannot exceed 2555 days (7 years)")
	@AMetaData (
			displayName = "Audit Log Retention (Days)", required = true, readOnly = false, defaultValue = "365",
			description = "Number of days to retain audit logs", hidden = false
	)
	private Integer auditLogRetentionDays = 365;
	// One-to-Many relationship with comments - for configuration notes
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "system_settings_id")
	@AMetaData (
			displayName = "Configuration Notes", required = false, readOnly = false, description = "Administrative notes about system configuration",
			hidden = false, dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	// Advanced reporting and analytics
	@Column (name = "enable_advanced_reporting", nullable = false)
	@AMetaData (
			displayName = "Enable Advanced Reporting", required = true, readOnly = false, defaultValue = "true",
			description = "Enable advanced reporting and analytics features", hidden = false
	)
	private Boolean enableAdvancedReporting = Boolean.TRUE;
	// Enhanced security and audit
	@Column (name = "enable_audit_logging", nullable = false)
	@AMetaData (
			displayName = "Enable Audit Logging", required = true, readOnly = false, defaultValue = "true",
			description = "Enable comprehensive audit logging for all user actions", hidden = false
	)
	private Boolean enableAuditLogging = Boolean.TRUE;
	// Notification system
	@Column (name = "enable_email_notifications", nullable = false)
	@AMetaData (
			displayName = "Enable Email Notifications", required = true, readOnly = false, defaultValue = "true",
			description = "Send email notifications for important events", hidden = false
	)
	private Boolean enableEmailNotifications = Boolean.TRUE;
	@Column (name = "enable_file_compression", nullable = false)
	@AMetaData (
			displayName = "Enable File Compression", required = true, readOnly = false, defaultValue = "true",
			description = "Automatically compress uploaded files to save space", hidden = false
	)
	private Boolean enableFileCompression = Boolean.TRUE;
	@Column (name = "enable_file_virus_scanning", nullable = false)
	@AMetaData (
			displayName = "Enable File Virus Scanning", required = true, readOnly = false, defaultValue = "false",
			description = "Scan uploaded files for viruses and malware", hidden = false
	)
	private Boolean enableFileVirusScanning = Boolean.FALSE;
	@Column (name = "enable_gantt_charts", nullable = false)
	@AMetaData (
			displayName = "Enable Gantt Charts", required = true, readOnly = false, defaultValue = "true",
			description = "Enable Gantt chart visualization for project timelines", hidden = false
	)
	private Boolean enableGanttCharts = Boolean.TRUE;
	@Column (name = "enable_kanban_boards", nullable = false)
	@AMetaData (
			displayName = "Enable Kanban Boards", required = true, readOnly = false, defaultValue = "true",
			description = "Enable Kanban board visualization for projects", hidden = false
	)
	private Boolean enableKanbanBoards = Boolean.TRUE;
	// Enhanced project management features
	@Column (name = "enable_project_templates", nullable = false)
	@AMetaData (
			displayName = "Enable Project Templates", required = true, readOnly = false, defaultValue = "true",
			description = "Allow creation and use of project templates", hidden = false
	)
	private Boolean enableProjectTemplates = Boolean.TRUE;
	@Column (name = "enable_push_notifications", nullable = false)
	@AMetaData (
			displayName = "Enable Push Notifications", required = true, readOnly = false, defaultValue = "false",
			description = "Enable browser push notifications", hidden = false
	)
	private Boolean enablePushNotifications = Boolean.FALSE;
	@Column (name = "enable_resource_planning", nullable = false)
	@AMetaData (
			displayName = "Enable Resource Planning", required = true, readOnly = false, defaultValue = "true",
			description = "Enable resource allocation and capacity planning", hidden = false
	)
	private Boolean enableResourcePlanning = Boolean.TRUE;
	// Integration and API settings
	@Column (name = "enable_rest_api", nullable = false)
	@AMetaData (
			displayName = "Enable REST API", required = true, readOnly = false, defaultValue = "true",
			description = "Enable REST API for third-party integrations", hidden = false
	)
	private Boolean enableRestApi = Boolean.TRUE;
	@Column (name = "enable_time_tracking", nullable = false)
	@AMetaData (
			displayName = "Enable Time Tracking", required = true, readOnly = false, defaultValue = "true",
			description = "Enable time tracking for activities and tasks", hidden = false
	)
	private Boolean enableTimeTracking = Boolean.TRUE;
	@Column (name = "enable_two_factor_auth", nullable = false)
	@AMetaData (
			displayName = "Enable Two-Factor Authentication", required = true, readOnly = false, defaultValue = "false",
			description = "Enable two-factor authentication for enhanced security", hidden = false
	)
	private Boolean enableTwoFactorAuth = Boolean.FALSE;
	// Enhanced file management
	@Column (name = "max_total_storage_gb", nullable = false, precision = 10, scale = 2)
	@DecimalMin (value = "1.0", message = "Max total storage must be at least 1.0 GB")
	@AMetaData (
			displayName = "Max Total Storage (GB)", required = true, readOnly = false, defaultValue = "100.0",
			description = "Maximum total storage space for all files in gigabytes", hidden = false
	)
	private BigDecimal maxTotalStorageGb = new BigDecimal("100.0");
	@Column (name = "notification_batch_size", nullable = false)
	@Min (value = 1, message = "Notification batch size must be at least 1")
	@Max (value = 1000, message = "Notification batch size cannot exceed 1000")
	@AMetaData (
			displayName = "Notification Batch Size", required = true, readOnly = false, defaultValue = "50",
			description = "Number of notifications to process in each batch", hidden = false
	)
	private Integer notificationBatchSize = 50;
	@Column (name = "report_generation_timeout_minutes", nullable = false)
	@Min (value = 1, message = "Report generation timeout must be at least 1 minute")
	@Max (value = 60, message = "Report generation timeout cannot exceed 60 minutes")
	@AMetaData (
			displayName = "Report Generation Timeout (Minutes)", required = true, readOnly = false, defaultValue = "15",
			description = "Maximum time allowed for report generation", hidden = false
	)
	private Integer reportGenerationTimeoutMinutes = 15;

	/** Default constructor for JPA. */
	protected CSystemSettings_Derbent() {}

	/** Business constructor for creating new Derbent system settings. */
	public CSystemSettings_Derbent(final String applicationName) {
		super(CSystemSettings_Derbent.class, applicationName);
		initializeDefaults();
	}


	public Integer getApiRateLimitPerMinute() { return apiRateLimitPerMinute; }

	// IHasAttachments interface methods
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	public Integer getAuditLogRetentionDays() { return auditLogRetentionDays; }

	// IHasComments interface methods
	@Override
	public Set<CComment> getComments() { return comments; }

	public Boolean getEnableAdvancedReporting() { return enableAdvancedReporting; }

	public Boolean getEnableAuditLogging() { return enableAuditLogging; }

	public Boolean getEnableEmailNotifications() { return enableEmailNotifications; }

	public Boolean getEnableFileCompression() { return enableFileCompression; }

	public Boolean getEnableFileVirusScanning() { return enableFileVirusScanning; }

	public Boolean getEnableGanttCharts() { return enableGanttCharts; }

	public Boolean getEnableKanbanBoards() { return enableKanbanBoards; }

	// Getters and setters for Derbent-specific fields
	public Boolean getEnableProjectTemplates() { return enableProjectTemplates; }

	public Boolean getEnablePushNotifications() { return enablePushNotifications; }

	public Boolean getEnableResourcePlanning() { return enableResourcePlanning; }

	public Boolean getEnableRestApi() { return enableRestApi; }

	public Boolean getEnableTimeTracking() { return enableTimeTracking; }

	public Boolean getEnableTwoFactorAuth() { return enableTwoFactorAuth; }

	public BigDecimal getMaxTotalStorageGb() { return maxTotalStorageGb; }

	public Integer getNotificationBatchSize() { return notificationBatchSize; }

	public Integer getReportGenerationTimeoutMinutes() { return reportGenerationTimeoutMinutes; }

	private final void initializeDefaults() {
		// Derbent-specific defaults for comprehensive PLM
		setApplicationName("Derbent Project Management");
		setApplicationDescription("Comprehensive project lifecycle management solution");
		setDefaultSystemTheme("lumo");
		setDefaultLoginView("home");
		// Enhanced settings for full PLM functionality
		setSessionTimeoutMinutes(60); // Standard session timeout
		setMaxLoginAttempts(3);
		setEnableAutomaticBackups(Boolean.TRUE); // Important for PLM data
		setEnableFileVersioning(Boolean.TRUE); // Critical for project documents
		setShowSystemInfo(Boolean.TRUE);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public Boolean isEnableAdvancedReporting() { return enableAdvancedReporting; }

	public Boolean isEnableAuditLogging() { return enableAuditLogging; }

	public Boolean isEnableEmailNotifications() { return enableEmailNotifications; }

	public Boolean isEnableFileCompression() { return enableFileCompression; }

	public Boolean isEnableFileVirusScanning() { return enableFileVirusScanning; }

	public Boolean isEnableGanttCharts() { return enableGanttCharts; }

	public Boolean isEnableKanbanBoards() { return enableKanbanBoards; }

	// Boolean convenience methods
	public Boolean isEnableProjectTemplates() { return enableProjectTemplates; }

	public Boolean isEnablePushNotifications() { return enablePushNotifications; }

	public Boolean isEnableResourcePlanning() { return enableResourcePlanning; }

	public Boolean isEnableRestApi() { return enableRestApi; }

	public Boolean isEnableTimeTracking() { return enableTimeTracking; }

	public Boolean isEnableTwoFactorAuth() { return enableTwoFactorAuth; }

	public void setApiRateLimitPerMinute(final Integer apiRateLimitPerMinute) { this.apiRateLimitPerMinute = apiRateLimitPerMinute; }

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setAuditLogRetentionDays(final Integer auditLogRetentionDays) { this.auditLogRetentionDays = auditLogRetentionDays; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setEnableAdvancedReporting(final Boolean enableAdvancedReporting) { this.enableAdvancedReporting = enableAdvancedReporting; }

	public void setEnableAuditLogging(final Boolean enableAuditLogging) { this.enableAuditLogging = enableAuditLogging; }

	public void setEnableEmailNotifications(final Boolean enableEmailNotifications) { this.enableEmailNotifications = enableEmailNotifications; }

	public void setEnableFileCompression(final Boolean enableFileCompression) { this.enableFileCompression = enableFileCompression; }

	public void setEnableFileVirusScanning(final Boolean enableFileVirusScanning) { this.enableFileVirusScanning = enableFileVirusScanning; }

	public void setEnableGanttCharts(final Boolean enableGanttCharts) { this.enableGanttCharts = enableGanttCharts; }

	public void setEnableKanbanBoards(final Boolean enableKanbanBoards) { this.enableKanbanBoards = enableKanbanBoards; }

	public void setEnableProjectTemplates(final Boolean enableProjectTemplates) { this.enableProjectTemplates = enableProjectTemplates; }

	public void setEnablePushNotifications(final Boolean enablePushNotifications) { this.enablePushNotifications = enablePushNotifications; }

	public void setEnableResourcePlanning(final Boolean enableResourcePlanning) { this.enableResourcePlanning = enableResourcePlanning; }

	public void setEnableRestApi(final Boolean enableRestApi) { this.enableRestApi = enableRestApi; }

	public void setEnableTimeTracking(final Boolean enableTimeTracking) { this.enableTimeTracking = enableTimeTracking; }

	public void setEnableTwoFactorAuth(final Boolean enableTwoFactorAuth) { this.enableTwoFactorAuth = enableTwoFactorAuth; }

	public void setMaxTotalStorageGb(final BigDecimal maxTotalStorageGb) { this.maxTotalStorageGb = maxTotalStorageGb; }

	public void setNotificationBatchSize(final Integer notificationBatchSize) { this.notificationBatchSize = notificationBatchSize; }

	public void setReportGenerationTimeoutMinutes(final Integer reportGenerationTimeoutMinutes) {
		this.reportGenerationTimeoutMinutes = reportGenerationTimeoutMinutes;
	}

	@Override
	public String toString() {
		return "CSystemSettings_Derbent{" + "applicationName='" + getApplicationName() + '\'' + ", enableProjectTemplates=" + enableProjectTemplates
				+ ", enableKanbanBoards=" + enableKanbanBoards + ", enableTimeTracking=" + enableTimeTracking + ", enableAdvancedReporting="
				+ enableAdvancedReporting + ", enableAuditLogging=" + enableAuditLogging + ", enableRestApi=" + enableRestApi + ", maxTotalStorageGb="
				+ maxTotalStorageGb + '}';
	}
}
