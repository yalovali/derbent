package tech.derbent.api.roles.domain;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.companies.domain.CCompany;

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
	// Page access permissions
	@ElementCollection (fetch = FetchType.EAGER)
	@CollectionTable (name = "cusercompanyrole_read_pages", joinColumns = @JoinColumn (name = "role_id"))
	@Column (name = "page_name", length = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Read Access Pages", required = false, readOnly = false, description = "Set of pages where user has read access",
			hidden = false, order = 20
	)
	private Set<String> readAccessPages = new HashSet<>();
	@ElementCollection (fetch = FetchType.EAGER)
	@CollectionTable (name = "cusercompanyrole_write_pages", joinColumns = @JoinColumn (name = "role_id"))
	@Column (name = "page_name", length = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Write Access Pages", required = false, readOnly = false, description = "Set of pages where user has write access",
			hidden = false, order = 21
	)
	private Set<String> writeAccessPages = new HashSet<>();

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

	// Page access getters and setters
	public Set<String> getReadAccessPages() {
		if (readAccessPages == null) {
			readAccessPages = new HashSet<>();
		}
		return readAccessPages;
	}

	public void setReadAccessPages(Set<String> readAccessPages) {
		this.readAccessPages = readAccessPages != null ? readAccessPages : new HashSet<>();
	}

	public Set<String> getWriteAccessPages() {
		if (writeAccessPages == null) {
			writeAccessPages = new HashSet<>();
		}
		return writeAccessPages;
	}

	public void setWriteAccessPages(Set<String> writeAccessPages) {
		this.writeAccessPages = writeAccessPages != null ? writeAccessPages : new HashSet<>();
	}

	// Page access utility methods
	public boolean hasReadAccess(String pageName) {
		return getReadAccessPages().contains(pageName) || hasWriteAccess(pageName);
	}

	public boolean hasWriteAccess(String pageName) {
		return getWriteAccessPages().contains(pageName);
	}

	public void addReadAccess(String pageName) {
		getReadAccessPages().add(pageName);
	}

	public void addWriteAccess(String pageName) {
		getWriteAccessPages().add(pageName);
		// Write access implies read access
		addReadAccess(pageName);
	}

	public void removeReadAccess(String pageName) {
		getReadAccessPages().remove(pageName);
		// If removing read access, also remove write access
		removeWriteAccess(pageName);
	}

	public void removeWriteAccess(String pageName) {
		getWriteAccessPages().remove(pageName);
	}

	@Override
	public String toString() {
		return String.format("%s{id=%d, name='%s', isAdmin=%s, isUser=%s, isGuest=%s, readPages=%d, writePages=%d}", getClass().getSimpleName(),
				getId(), getName(), isAdmin, isUser, isGuest, getReadAccessPages().size(), getWriteAccessPages().size());
	}

	@Override
	public void initializeAllFields() {
		// TODO Auto-generated method stub
	}
}
