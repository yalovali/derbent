package tech.derbent.administration.domain;

import java.math.BigDecimal;
import java.time.ZoneId;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityConstants;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.companies.domain.CCompany;

/**
 * CCompanySettings - Domain entity representing company-wide administration settings.
 * Layer: Domain (MVC) This entity stores administrative configurations that apply across
 * all projects within a specific company, including workflow defaults, working hours,
 * notifications, and project management preferences.
 */
@Entity
@Table (name = "ccompanysettings")
@AttributeOverride (name = "id", column = @Column (name = "company_settings_id"))
public class CCompanySettings extends CEntityDB {

	@ManyToOne
	@JoinColumn (name = "company_id", nullable = false)
	@MetaData (
		displayName = "Company", required = true, readOnly = false,
		description = "Company these settings belong to", hidden = false, order = 1
	)
	private CCompany company;

	// Workflow and Project Management Settings
	@Column (
		name = "default_project_status", nullable = false,
		length = CEntityConstants.MAX_LENGTH_NAME
	)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@MetaData (
		displayName = "Default Project Status", required = true, readOnly = false,
		defaultValue = "PLANNED", description = "Default status for new projects",
		hidden = false, order = 2, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String defaultProjectStatus = "PLANNED";

	@Column (
		name = "default_activity_status", nullable = false,
		length = CEntityConstants.MAX_LENGTH_NAME
	)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@MetaData (
		displayName = "Default Activity Status", required = true, readOnly = false,
		defaultValue = "TODO", description = "Default status for new activities",
		hidden = false, order = 3, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String defaultActivityStatus = "TODO";

	@Column (
		name = "default_activity_priority", nullable = false,
		length = CEntityConstants.MAX_LENGTH_NAME
	)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@MetaData (
		displayName = "Default Activity Priority", required = true, readOnly = false,
		defaultValue = "MEDIUM", description = "Default priority for new activities",
		hidden = false, order = 4, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String defaultActivityPriority = "MEDIUM";

	// Time and Schedule Settings
	@Column (
		name = "company_timezone", nullable = false,
		length = CEntityConstants.MAX_LENGTH_NAME
	)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@MetaData (
		displayName = "Company Timezone", required = true, readOnly = false,
		defaultValue = "UTC", description = "Primary timezone for the company",
		hidden = false, order = 5, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String companyTimezone = ZoneId.systemDefault().getId();

	@Column (name = "working_hours_per_day", nullable = false, precision = 4, scale = 2)
	@DecimalMin (value = "1.0", message = "Working hours must be at least 1.0")
	@DecimalMax (value = "24.0", message = "Working hours cannot exceed 24.0")
	@MetaData (
		displayName = "Working Hours Per Day", required = true, readOnly = false,
		defaultValue = "8.0", description = "Standard working hours per day",
		hidden = false, order = 6
	)
	private BigDecimal workingHoursPerDay = new BigDecimal("8.0");

	@Column (name = "working_days_per_week", nullable = false)
	@Min (value = 1, message = "Working days must be at least 1")
	@Max (value = 7, message = "Working days cannot exceed 7")
	@MetaData (
		displayName = "Working Days Per Week", required = true, readOnly = false,
		defaultValue = "5", description = "Standard working days per week",
		hidden = false, order = 7
	)
	private Integer workingDaysPerWeek = 5;

	@Column (name = "start_work_hour", nullable = false)
	@Min (value = 0, message = "Start work hour must be between 0 and 23")
	@Max (value = 23, message = "Start work hour must be between 0 and 23")
	@MetaData (
		displayName = "Start Work Hour", required = true, readOnly = false,
		defaultValue = "9", description = "Standard work start hour (24-hour format)",
		hidden = false, order = 8
	)
	private Integer startWorkHour = 9;

	@Column (name = "end_work_hour", nullable = false)
	@Min (value = 0, message = "End work hour must be between 0 and 23")
	@Max (value = 23, message = "End work hour must be between 0 and 23")
	@MetaData (
		displayName = "End Work Hour", required = true, readOnly = false,
		defaultValue = "17", description = "Standard work end hour (24-hour format)",
		hidden = false, order = 9
	)
	private Integer endWorkHour = 17;

	// Notification Settings
	@Column (name = "email_notifications_enabled", nullable = false)
	@MetaData (
		displayName = "Email Notifications", required = true, readOnly = false,
		defaultValue = "true", description = "Enable email notifications", hidden = false,
		order = 10
	)
	private boolean emailNotificationsEnabled = true;

	@Column (name = "due_date_reminder_days", nullable = false)
	@Min (value = 0, message = "Reminder days must be non-negative")
	@Max (value = 30, message = "Reminder days cannot exceed 30")
	@MetaData (
		displayName = "Due Date Reminder (Days)", required = true, readOnly = false,
		defaultValue = "3", description = "Days before due date to send reminders",
		hidden = false, order = 11
	)
	private Integer dueDateReminderDays = 3;

	@Column (name = "overdue_notification_enabled", nullable = false)
	@MetaData (
		displayName = "Overdue Notifications", required = true, readOnly = false,
		defaultValue = "true", description = "Enable overdue task notifications",
		hidden = false, order = 12
	)
	private boolean overdueNotificationEnabled = true;

	// Project Management Preferences
	@Column (name = "auto_assign_project_manager", nullable = false)
	@MetaData (
		displayName = "Auto-assign Project Manager", required = true, readOnly = false,
		defaultValue = "false",
		description = "Automatically assign project manager to new projects",
		hidden = false, order = 13
	)
	private boolean autoAssignProjectManager = false;

	@Column (name = "require_time_tracking", nullable = false)
	@MetaData (
		displayName = "Require Time Tracking", required = true, readOnly = false,
		defaultValue = "true", description = "Require time tracking for all activities",
		hidden = false, order = 14
	)
	private boolean requireTimeTracking = true;

	@Column (name = "default_hourly_rate", nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Hourly rate must be non-negative")
	@MetaData (
		displayName = "Default Hourly Rate", required = false, readOnly = false,
		defaultValue = "50.0", description = "Default hourly rate for cost calculations",
		hidden = false, order = 15
	)
	private BigDecimal defaultHourlyRate = new BigDecimal("50.0");

	// UI and Display Settings
	@Column (name = "company_theme_color", nullable = true, length = 7)
	@Size (max = 7)
	@MetaData (
		displayName = "Theme Color", required = false, readOnly = false,
		defaultValue = "#1976d2", description = "Company primary theme color (hex)",
		hidden = false, order = 16, maxLength = 7
	)
	private String companyThemeColor = "#1976d2";

	@Column (name = "show_budget_info", nullable = false)
	@MetaData (
		displayName = "Show Budget Information", required = true, readOnly = false,
		defaultValue = "true",
		description = "Display budget information in project views", hidden = false,
		order = 17
	)
	private boolean showBudgetInfo = true;

	@Column (name = "enable_gantt_charts", nullable = false)
	@MetaData (
		displayName = "Enable Gantt Charts", required = true, readOnly = false,
		defaultValue = "true", description = "Enable Gantt chart functionality",
		hidden = false, order = 18
	)
	private boolean enableGanttCharts = true;

	// Security and Permissions
	@Column (
		name = "default_user_role", nullable = false,
		length = CEntityConstants.MAX_LENGTH_NAME
	)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@MetaData (
		displayName = "Default User Role", required = true, readOnly = false,
		defaultValue = "MEMBER", description = "Default role for new users",
		hidden = false, order = 19, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String defaultUserRole = "MEMBER";

	@Column (name = "require_approval_for_time_entries", nullable = false)
	@MetaData (
		displayName = "Require Time Entry Approval", required = true, readOnly = false,
		defaultValue = "false", description = "Require manager approval for time entries",
		hidden = false, order = 20
	)
	private boolean requireApprovalForTimeEntries = false;

	/**
	 * Default constructor required by JPA. Initializes entity with default values.
	 */
	public CCompanySettings() {
		super();
		initializeDefaults();
	}

	/**
	 * Constructor with company.
	 * @param company The company these settings belong to
	 */
	public CCompanySettings(final CCompany company) {
		super();
		this.company = company;
		initializeDefaults();
	}

	public CCompany getCompany() { return company; }
	// Getters and setters following the existing pattern

	public String getCompanyThemeColor() { return companyThemeColor; }

	public String getCompanyTimezone() { return companyTimezone; }

	public String getDefaultActivityPriority() { return defaultActivityPriority; }

	public String getDefaultActivityStatus() { return defaultActivityStatus; }

	public BigDecimal getDefaultHourlyRate() { return defaultHourlyRate; }

	public String getDefaultProjectStatus() { return defaultProjectStatus; }

	public String getDefaultUserRole() { return defaultUserRole; }

	public Integer getDueDateReminderDays() { return dueDateReminderDays; }

	public Integer getEndWorkHour() { return endWorkHour; }

	public Integer getStartWorkHour() { return startWorkHour; }

	public Integer getWorkingDaysPerWeek() { return workingDaysPerWeek; }

	public BigDecimal getWorkingHoursPerDay() { return workingHoursPerDay; }

	@Override
	protected void initializeDefaults() {

		if (companyTimezone == null) {
			companyTimezone = ZoneId.systemDefault().getId();
		}

		if (workingHoursPerDay == null) {
			workingHoursPerDay = new BigDecimal("8.0");
		}

		if (defaultHourlyRate == null) {
			defaultHourlyRate = new BigDecimal("50.0");
		}
	}

	public boolean isAutoAssignProjectManager() { return autoAssignProjectManager; }

	public boolean isEmailNotificationsEnabled() { return emailNotificationsEnabled; }

	public boolean isEnableGanttCharts() { return enableGanttCharts; }

	public boolean isOverdueNotificationEnabled() { return overdueNotificationEnabled; }

	public boolean isRequireApprovalForTimeEntries() {
		return requireApprovalForTimeEntries;
	}

	public boolean isRequireTimeTracking() { return requireTimeTracking; }

	public boolean isShowBudgetInfo() { return showBudgetInfo; }

	public void setAutoAssignProjectManager(final boolean autoAssignProjectManager) {
		this.autoAssignProjectManager = autoAssignProjectManager;
	}

	public void setCompany(final CCompany company) { this.company = company; }

	public void setCompanyThemeColor(final String companyThemeColor) {
		this.companyThemeColor = companyThemeColor;
	}

	public void setCompanyTimezone(final String companyTimezone) {
		this.companyTimezone = companyTimezone;
	}

	public void setDefaultActivityPriority(final String defaultActivityPriority) {
		this.defaultActivityPriority = defaultActivityPriority;
	}

	public void setDefaultActivityStatus(final String defaultActivityStatus) {
		this.defaultActivityStatus = defaultActivityStatus;
	}

	public void setDefaultHourlyRate(final BigDecimal defaultHourlyRate) {
		this.defaultHourlyRate = defaultHourlyRate;
	}

	public void setDefaultProjectStatus(final String defaultProjectStatus) {
		this.defaultProjectStatus = defaultProjectStatus;
	}

	public void setDefaultUserRole(final String defaultUserRole) {
		this.defaultUserRole = defaultUserRole;
	}

	public void setDueDateReminderDays(final Integer dueDateReminderDays) {
		this.dueDateReminderDays = dueDateReminderDays;
	}

	public void setEmailNotificationsEnabled(final boolean emailNotificationsEnabled) {
		this.emailNotificationsEnabled = emailNotificationsEnabled;
	}

	public void setEnableGanttCharts(final boolean enableGanttCharts) {
		this.enableGanttCharts = enableGanttCharts;
	}

	public void setEndWorkHour(final Integer endWorkHour) {
		this.endWorkHour = endWorkHour;
	}

	public void setOverdueNotificationEnabled(final boolean overdueNotificationEnabled) {
		this.overdueNotificationEnabled = overdueNotificationEnabled;
	}

	public void
		setRequireApprovalForTimeEntries(final boolean requireApprovalForTimeEntries) {
		this.requireApprovalForTimeEntries = requireApprovalForTimeEntries;
	}

	public void setRequireTimeTracking(final boolean requireTimeTracking) {
		this.requireTimeTracking = requireTimeTracking;
	}

	public void setShowBudgetInfo(final boolean showBudgetInfo) {
		this.showBudgetInfo = showBudgetInfo;
	}

	public void setStartWorkHour(final Integer startWorkHour) {
		this.startWorkHour = startWorkHour;
	}

	public void setWorkingDaysPerWeek(final Integer workingDaysPerWeek) {
		this.workingDaysPerWeek = workingDaysPerWeek;
	}

	public void setWorkingHoursPerDay(final BigDecimal workingHoursPerDay) {
		this.workingHoursPerDay = workingHoursPerDay;
	}

	@Override
	public String toString() {
		return "CCompanySettings{" + "company="
			+ (company != null ? company.getName() : "null") + ", defaultProjectStatus='"
			+ defaultProjectStatus + '\'' + ", defaultActivityStatus='"
			+ defaultActivityStatus + '\'' + ", defaultActivityPriority='"
			+ defaultActivityPriority + '\'' + ", companyTimezone='" + companyTimezone
			+ '\'' + ", workingHoursPerDay=" + workingHoursPerDay
			+ ", workingDaysPerWeek=" + workingDaysPerWeek
			+ ", emailNotificationsEnabled=" + emailNotificationsEnabled + '}';
	}
}