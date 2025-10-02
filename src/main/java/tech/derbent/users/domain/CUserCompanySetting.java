package tech.derbent.users.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.annotations.ARelationshipMetadata;
import tech.derbent.api.domains.CAbstractEntityRelationship;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;

/** Entity representing the relationship between a user and a company with ownership privileges. This entity manages company membership, roles, and
 * ownership levels for users. */
@Entity
@Table (name = "cusercompanysetting", uniqueConstraints = @UniqueConstraint (columnNames = {
		"user_id", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cusercompanysetting_id"))
@ARelationshipMetadata (
		parentEntityClass = CCompany.class, childEntityClass = CUser.class, relationshipEntityClass = CUserCompanySetting.class,
		displayName = "User Company Membership", description = "Manages user membership and ownership in companies", supportsOwnership = true,
		defaultOwnership = "MEMBER", ownershipLevels = {
				"OWNER", "ADMIN", "MEMBER", "VIEWER"
		}, parentCollectionField = "users", childCollectionField = "companySetting"
)
public class CUserCompanySetting extends CAbstractEntityRelationship<CUserCompanySetting> {

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
		removeUserFromCompany(company, user);
		// Add to both sides of the bidirectional relationship
		if (company.getUsers() != null) {
			company.getUsers().add(user);
		}
	}

	public static void removeUserFromCompany(CCompany company, CUser user) {
		Check.notNull(company, "Company must not be null");
		Check.notNull(user, "User must not be null");
		// Remove from company's users list
		if (company.getUsers() != null) {
			company.getUsers().removeIf(u -> u.equals(user));
		}
	}

	@ManyToOne
	@JoinColumn (name = "company_id", nullable = false)
	@AMetaData (
			displayName = "Company", required = true, readOnly = false, description = "Company in this relationship", hidden = false, order = 2,
			dataProviderOwner = "content", dataProviderMethod = "getAvailableCompaniesForUser"
	)
	private CCompany company;
	@Column (name = "role", nullable = true, length = 100)
	@AMetaData (
			displayName = "Role", required = false, readOnly = false, defaultValue = "", description = "User's role within the company",
			hidden = false, order = 3, maxLength = 100
	)
	private String role;
	@ManyToOne
	@JoinColumn (name = "user_id", nullable = false)
	@AMetaData (
			displayName = "User", required = true, readOnly = false, description = "User in this company relationship", hidden = false, order = 1,
			dataProviderOwner = "content", dataProviderMethod = "getAvailableUsersForCompany"
	)
	private CUser user;

	public CUserCompanySetting() {
		super(CUserCompanySetting.class);
	}

	public CUserCompanySetting(CUser user, CCompany company) {
		super(CUserCompanySetting.class);
		this.user = user;
		this.company = company;
	}

	public CUserCompanySetting(CUser user, CCompany company, String ownershipLevel) {
		this(user, company);
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

	public String getRole() { return role; }

	// Getters and Setters
	public CUser getUser() { return user; }

	public String getUserName() { return user != null ? user.getName() : "Unknown User"; }

	/** Check if this user has company admin privileges.
	 * @return true if user is company owner or admin */
	public boolean isCompanyAdmin() { return isOwner() || isAdmin(); }

	public void setCompany(CCompany company) { this.company = company; }

	public void setRole(String role) { this.role = role; }

	public void setUser(CUser user) { this.user = user; }

	@Override
	public String toString() {
		return String.format("UserCompanySettings[user=%s, company=%s, ownership=%s, role=%s, active=%s]", user != null ? user.getLogin() : "null",
				company != null ? company.getName() : "null", getOwnershipLevel(), role, isActive());
	}

	@Override
	public void initializeAllFields() {
		// TODO Auto-generated method stub
	}
}
