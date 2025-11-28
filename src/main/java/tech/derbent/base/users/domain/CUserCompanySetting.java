package tech.derbent.base.users.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.annotations.CSpringAuxillaries;
import tech.derbent.api.domains.CAbstractEntityRelationship;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.roles.domain.CUserCompanyRole;

/** Entity representing the relationship between a user and a company with ownership privileges. This entity manages company membership, roles, and
 * ownership levels for users. */
@Entity
@Table (name = "cusercompanysetting", uniqueConstraints = @UniqueConstraint (columnNames = {
		"user_id", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cusercompanysetting_id"))
public class CUserCompanySetting extends CAbstractEntityRelationship<CUserCompanySetting> {

	public static final String DEFAULT_COLOR = "#DC143C";
	public static final String DEFAULT_ICON = "vaadin:tasks";
	public static final String VIEW_NAME = "User Company Settings View";

	// Static helper methods for bidirectional relationship management
	public static void addUserToCompany(CCompany company, CUser user, CUserCompanySetting settings) {
		Check.notNull(company, "Company must not be null");
		Check.notNull(user, "User must not be null");
		Check.notNull(settings, "UserCompanySetting must not be null");
		// Set the relationships in the settings object
		settings.setCompany(company);
		settings.setUser(user);
		// Remove any existing relationship to avoid duplicates
		// Add to both sides of the bidirectional relationship
	}

	@ManyToOne
	@JoinColumn (name = "company_id", nullable = false)
	@OnDelete (action = OnDeleteAction.CASCADE)
	@AMetaData (
			displayName = "Company", required = true, readOnly = false, description = "Company in this relationship", hidden = false, 
			dataProviderBean = "context", dataProviderMethod = "getAvailableCompanyForUser"
	)
	private CCompany company;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "role_id", nullable = true)
	@AMetaData (
			displayName = "Role", required = false, readOnly = false, description = "User's role within the company", hidden = false, 
			dataProviderBean = "context", dataProviderMethod = "getAvailableCompanyRolesForUser", setBackgroundFromColor = true, useIcon = true
	)
	private CUserCompanyRole role;
	@ManyToOne
	@JoinColumn (name = "user_id", nullable = false)
	@AMetaData (
			displayName = "User", required = true, readOnly = false, description = "User in this company relationship", hidden = false, 
			dataProviderBean = "context", dataProviderMethod = "getAvailableUsersForCompany"
	)
	private CUser user;

	public CUserCompanySetting() {
		super(CUserCompanySetting.class);
	}

	public CUserCompanySetting(CUser user, CCompany company, CUserCompanyRole role, String ownershipLevel) {
		super(CUserCompanySetting.class);
		this.user = user;
		this.company = company;
		this.role = role;
		setOwnershipLevel(ownershipLevel);
	}

	/** Check if this user can manage company settings.
	 * @return true if user has MANAGE_COMPANY privilege or is owner */
	public boolean canManageCompany() {
		return isOwner() || hasPrivilege("MANAGE_COMPANY");
	}

	/** Check if this user can manage other users in the company.
	 * @return true if user has MANAGE_USERS privilege or is admin */
	public boolean canManageUsers() {
		return isCompanyAdmin() || hasPrivilege("MANAGE_USERS");
	}

	public CCompany getCompany() { return company; }

	public String getCompanyName() { return company != null ? company.getName() : "Unknown Company"; }

	public CUserCompanyRole getRole() { return role; }

	// Getters and Setters
	public CUser getUser() { return user; }

	public String getUserName() { return user != null ? user.getName() : "Unknown User"; }

	/** Check if this user has company admin privileges.
	 * @return true if user is company owner or admin */
	public boolean isCompanyAdmin() { return isOwner() || isAdmin(); }

	public void setCompany(CCompany company) { this.company = company; }

	public void setRole(CUserCompanyRole role) { this.role = role; }

	public void setUser(CUser user) { this.user = user; }

	@Override
	public String toString() {
		return String.format("UserCompanySettings[user=%s, company=%s, ownership=%s, role=%s, active=%s]",
				user != null ? CSpringAuxillaries.safeToString(user) : "null", company != null ? CSpringAuxillaries.safeToString(company) : "null",
				getOwnershipLevel(), role != null ? CSpringAuxillaries.safeToString(role) : "null", getActive());
	}
}
