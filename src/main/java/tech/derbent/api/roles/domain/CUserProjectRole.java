package tech.derbent.api.roles.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;

/** CUserProjectRole - Defines a user's role within a company context. Replaces enumeration-based role system with flexible, database-driven
 * role management. Includes boolean attributes for role types and page access permissions. */
@Entity
@Table (name = "cuserprojectrole", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cuserprojectrole_id"))
public class CUserProjectRole extends CRole<CUserProjectRole> {

	public static final String DEFAULT_COLOR = "#8E8E8E"; // CDE Dark Gray - project roles
	public static final String DEFAULT_ICON = "vaadin:book";
	public static final int MAX_LENGTH_NAME = 255;
	public static final String ENTITY_TITLE_PLURAL = "User Project Roles";
	public static final String ENTITY_TITLE_SINGULAR = "User Project Role";
	public static final String VIEW_NAME = "User Project Roles View";
	
	// Boolean attributes for project role types
	@Column (name = "is_admin", nullable = false)
	@AMetaData (
			displayName = "Is Admin", required = true, readOnly = false, defaultValue = "false",
			description = "Whether this role has admin privileges in the project", hidden = false
	)
	private Boolean isAdmin = Boolean.FALSE;
	@Column (name = "is_guest", nullable = false)
	@AMetaData (
			displayName = "Is Guest", required = true, readOnly = false, defaultValue = "false",
			description = "Whether this role has guest-level privileges", hidden = false
	)
	private Boolean isGuest = Boolean.FALSE;
	@Column (name = "is_user", nullable = false)
	@AMetaData (
			displayName = "Is User", required = true, readOnly = false, defaultValue = "true",
			description = "Whether this role has standard user privileges", hidden = false
	)
	private Boolean isUser = Boolean.TRUE;

	// Constructors
	public CUserProjectRole() {
		super();
		initializeDefaults();
	}

	public CUserProjectRole(String name, CCompany company) {
		super(CUserProjectRole.class, name, company);
		initializeDefaults();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		isAdmin = Boolean.FALSE;
		isGuest = Boolean.FALSE;
		isUser = Boolean.TRUE;
	}

	// Boolean attribute getters and setters
	public Boolean getIsAdmin() { return isAdmin; }

	public Boolean getIsGuest() { return isGuest; }

	public Boolean getIsUser() { return isUser; }

	// Convenience boolean methods
	public boolean isAdmin() { return Boolean.TRUE.equals(isAdmin); }

	public boolean isGuest() { return Boolean.TRUE.equals(isGuest); }

	public boolean isUser() { return Boolean.TRUE.equals(isUser); }

	public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin != null ? isAdmin : Boolean.FALSE; }

	public void setIsGuest(Boolean isGuest) { this.isGuest = isGuest != null ? isGuest : Boolean.FALSE; }

	public void setIsUser(Boolean isUser) { this.isUser = isUser != null ? isUser : Boolean.TRUE; }

	@Override
	public String toString() {
		return String.format("%s{id=%d, name='%s', isAdmin=%s, isUser=%s, isGuest=%s}", getClass().getSimpleName(), getId(), getName(), isAdmin,
				isUser, isGuest);
	}
}
