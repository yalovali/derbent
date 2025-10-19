package tech.derbent.companies.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.domains.CEntityNamed;

/** CCompany - Domain entity representing companies within the organization. Layer: Domain (MVC) Inherits from CEntityDB to provide database
 * functionality. */
@Entity
@Table (name = "ccompany")
@AttributeOverride (name = "id", column = @Column (name = "company_id"))
public class CCompany extends CEntityNamed<CCompany> {

	public static final String DEFAULT_COLOR = "#6f42c1";
	public static final String DEFAULT_ICON = "vaadin:office";
	public static final String VIEW_NAME = "Company View";
	@Column (name = "address", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "Address", required = false, readOnly = false, defaultValue = "", description = "Company address", hidden = false,
			order = 3, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String address;
	@Column (name = "company_logo_url", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "Company Logo URL", required = false, readOnly = false, defaultValue = "", description = "URL or path to company logo",
			hidden = false, order = 11, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String companyLogoUrl;
	// Company Configuration Settings
	@Column (name = "company_theme", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Company Theme", required = false, readOnly = false, defaultValue = "lumo-light",
			description = "Default theme for company users", hidden = false, order = 10, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String companyTheme = "lumo-light";
	@Column (name = "company_timezone", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Company Timezone", required = false, readOnly = false, defaultValue = "Europe/Istanbul",
			description = "Default timezone for company operations", hidden = false, order = 15, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String companyTimezone = "Europe/Istanbul";
	@Column (name = "default_language", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Default Language", required = false, readOnly = false, defaultValue = "tr",
			description = "Default language code for company users", hidden = false, order = 16, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String defaultLanguage = "tr";
	@Column (name = "email", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Email", required = false, readOnly = false, defaultValue = "", description = "Company email address", hidden = false,
			order = 5, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String email;
	@Column (name = "enable_notifications", nullable = false)
	@AMetaData (
			displayName = "Enable Notifications", required = true, readOnly = false, defaultValue = "false",
			description = "Enable email and system notifications for company", hidden = false, order = 17
	)
	private Boolean enableNotifications;
	@Column (name = "notification_email", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Notification Email", required = false, readOnly = false, defaultValue = "",
			description = "Primary email for company notifications", hidden = false, order = 18, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String notificationEmail;
	@Column (name = "phone", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Phone", required = false, readOnly = false, defaultValue = "", description = "Company phone number", hidden = false,
			order = 4, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String phone;
	@Column (name = "primary_color", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Primary Color", required = false, readOnly = false, defaultValue = "#1976d2", colorField = true,
			description = "Primary brand color for the company", hidden = false, order = 12, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String primaryColor = "#1976d2";
	@Column (name = "tax_number", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Tax Number", required = false, readOnly = false, defaultValue = "", description = "Company tax identification number",
			hidden = false, order = 7, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String taxNumber;
	@Column (name = "website", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Website", required = false, readOnly = false, defaultValue = "", description = "Company website URL", hidden = false,
			order = 6, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String website;
	@Column (name = "working_hours_end", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Working Hours End", required = false, readOnly = false, defaultValue = "17:00",
			description = "Company working hours end time", hidden = false, order = 14, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String workingHoursEnd = "17:00";
	@Column (name = "working_hours_start", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Working Hours Start", required = false, readOnly = false, defaultValue = "09:00",
			description = "Company working hours start time", hidden = false, order = 13, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String workingHoursStart = "09:00";

	/** Default constructor for JPA. */
	public CCompany() {
		super();
		initializeDefaults();
	}

	public CCompany(final String name) {
		super(CCompany.class, name); // Use the CEntityNamed constructor
	}

	public String getAddress() { return address; }

	public String getCompanyLogoUrl() { return companyLogoUrl; }

	public String getCompanyTheme() { return companyTheme; }

	public String getCompanyTimezone() { return companyTimezone; }

	public String getDefaultLanguage() { return defaultLanguage; }

	public String getEmail() { return email; }

	public Boolean getEnableNotifications() { return enableNotifications; }

	public String getNotificationEmail() { return notificationEmail; }

	public String getPhone() { return phone; }

	public String getPrimaryColor() { return primaryColor; }

	public String getTaxNumber() { return taxNumber; }

	public String getWebsite() { return website; }

	public String getWorkingHoursEnd() { return workingHoursEnd; }

	public String getWorkingHoursStart() { return workingHoursStart; }

	@Override
	public void initializeAllFields() {
		// No lazy-loaded entity relationships to initialize
	}

	/** Initialize default configuration values. */
	@Override
	protected void initializeDefaults() {
		if (companyTheme == null) {
			companyTheme = "lumo-light";
		}
		if (primaryColor == null) {
			primaryColor = "#1976d2";
		}
		if (workingHoursStart == null) {
			workingHoursStart = "09:00";
		}
		if (workingHoursEnd == null) {
			workingHoursEnd = "17:00";
		}
		if (companyTimezone == null) {
			companyTimezone = "Europe/Istanbul";
		}
		if (defaultLanguage == null) {
			defaultLanguage = "tr";
		}
		if (enableNotifications == null) {
			enableNotifications = Boolean.TRUE;
		}
	}

	public Boolean isEnableNotifications() { return enableNotifications; }

	public void setAddress(final String address) { this.address = address; }

	public void setCompanyLogoUrl(final String companyLogoUrl) { this.companyLogoUrl = companyLogoUrl; }

	public void setCompanyTheme(final String companyTheme) { this.companyTheme = companyTheme; }

	public void setCompanyTimezone(final String companyTimezone) { this.companyTimezone = companyTimezone; }

	public void setDefaultLanguage(final String defaultLanguage) { this.defaultLanguage = defaultLanguage; }

	public void setEmail(final String email) { this.email = email; }

	public void setEnableNotifications(final Boolean enableNotifications) { this.enableNotifications = enableNotifications; }

	public void setNotificationEmail(final String notificationEmail) { this.notificationEmail = notificationEmail; }

	public void setPhone(final String phone) { this.phone = phone; }

	public void setPrimaryColor(final String primaryColor) { this.primaryColor = primaryColor; }

	public void setTaxNumber(final String taxNumber) { this.taxNumber = taxNumber; }

	public void setWebsite(final String website) { this.website = website; }

	public void setWorkingHoursEnd(final String workingHoursEnd) { this.workingHoursEnd = workingHoursEnd; }

	public void setWorkingHoursStart(final String workingHoursStart) { this.workingHoursStart = workingHoursStart; }

	@Override
	public String toString() {
		return "CCompany{" + "name='" + getName() + '\'' + ", description='" + getDescription() + '\'' + ", address='" + address + '\'' + ", phone='"
				+ phone + '\'' + ", email='" + email + '\'' + ", website='" + website + '\'' + ", taxNumber='" + taxNumber + '\'' + ", active="
				+ getActive() + ", companyTheme='" + companyTheme + '\'' + ", primaryColor='" + primaryColor + '\'' + ", workingHoursStart='"
				+ workingHoursStart + '\'' + ", workingHoursEnd='" + workingHoursEnd + '\'' + ", companyTimezone='" + companyTimezone + '\''
				+ ", defaultLanguage='" + defaultLanguage + '\'' + ", enableNotifications=" + enableNotifications + '}';
	}
}
