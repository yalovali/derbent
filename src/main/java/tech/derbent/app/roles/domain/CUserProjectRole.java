package tech.derbent.app.roles.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;

/** CUserProjectRole - Defines a user's role within a specific project context. Replaces enumeration-based role system with flexible, database-driven
 * role management. Includes boolean attributes for role types and page access permissions. */
@Entity
@Table (name = "cuserprojectrole", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cuserprojectrole_id"))
public class CUserProjectRole extends CRole<CUserProjectRole> {

	public static final String DEFAULT_COLOR = "#8E8E8E"; // CDE Dark Gray - project roles
	public static final String DEFAULT_ICON = "vaadin:book";
	public static final int MAX_LENGTH_NAME = 255;
	public static final String ENTITY_TITLE_PLURAL = "User Project Roles";
	public static final String ENTITY_TITLE_SINGULAR = "User Project Role";
	public static final String VIEW_NAME = "User Project Roles View";
	
	// Project field - roles are scoped to both company and project
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "project_id", nullable = false)
	@OnDelete (action = OnDeleteAction.CASCADE)
	@AMetaData (displayName = "Project", required = true, readOnly = true, description = "Project of this role", hidden = false)
	private CProject project;
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
	}

	public CUserProjectRole(String name, CProject project) {
		super(CUserProjectRole.class, name, project != null ? project.getCompany() : null);
		this.project = project;
	}
	
	// Project getter and setter
	public CProject getProject() { return project; }
	
	public void setProject(final CProject project) {
		Check.notNull(project, "Project cannot be null for project-scoped roles");
		if (getCompany() != null && project.getCompany() != null) {
			Check.isTrue(getCompany().getId().equals(project.getCompany().getId()), 
				"Project must belong to the same company as the role");
		}
		this.project = project;
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
