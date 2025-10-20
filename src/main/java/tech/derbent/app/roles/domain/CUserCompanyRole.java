package tech.derbent.app.roles.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.app.companies.domain.CCompany;

/** CUserCompanyRole - Defines a user's role within a specific company context. Replaces enumeration-based role system with flexible, database-driven
 * role management. Includes boolean attributes for role types and page access permissions. */
@Entity
@Table (name = "cusercompanyrole", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cusercompanyrole_id"))
public class CUserCompanyRole extends CNonProjectType<CUserCompanyRole> {

	public static final String DEFAULT_COLOR = "#163f1d";
	public static final String DEFAULT_ICON = "vaadin:building";
	public static final int MAX_LENGTH_NAME = 255;
	public static final String VIEW_NAME = "User Company Roles View";
	// Boolean attributes for company role types
	@Column (name = "is_admin", nullable = false)
	@AMetaData (
			displayName = "Is Admin", required = true, readOnly = false, defaultValue = "false",
			description = "Whether this role has admin privileges in the company", hidden = false, order = 10
	)
	private Boolean isAdmin = Boolean.FALSE;
	@Column (name = "is_user", nullable = false)
	@AMetaData (
			displayName = "Is User", required = true, readOnly = false, defaultValue = "true",
			description = "Whether this role has standard user privileges", hidden = false, order = 11
	)
	private Boolean isUser = Boolean.TRUE;
	@Column (name = "is_guest", nullable = false)
	@AMetaData (
			displayName = "Is Guest", required = true, readOnly = false, defaultValue = "false",
			description = "Whether this role has guest-level privileges", hidden = false, order = 12
	)
	private Boolean isGuest = Boolean.FALSE;

	// Constructors
	public CUserCompanyRole() {
		super();
	}

	public CUserCompanyRole(String name, CCompany company) {
		super(CUserCompanyRole.class, name, company);
	}

	// Boolean attribute getters and setters
	public Boolean getIsAdmin() { return isAdmin; }

	public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin != null ? isAdmin : Boolean.FALSE; }

	public Boolean getIsUser() { return isUser; }

	public void setIsUser(Boolean isUser) { this.isUser = isUser != null ? isUser : Boolean.TRUE; }

	public Boolean getIsGuest() { return isGuest; }

	public void setIsGuest(Boolean isGuest) { this.isGuest = isGuest != null ? isGuest : Boolean.FALSE; }

	// Convenience boolean methods
	public boolean isAdmin() { return Boolean.TRUE.equals(isAdmin); }

	public boolean isUser() { return Boolean.TRUE.equals(isUser); }

	public boolean isGuest() { return Boolean.TRUE.equals(isGuest); }

	@Override
	public String toString() {
		return String.format("%s{id=%d, name='%s', isAdmin=%s, isUser=%s, isGuest=%s}", getClass().getSimpleName(), getId(), getName(), isAdmin,
				isUser, isGuest);
	}

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships from parent class (CNonProjectType)
		if (getCompany() != null) {
			getCompany().getName(); // Trigger company loading
		}
	}
}
