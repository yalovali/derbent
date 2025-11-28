package tech.derbent.base.users.domain;

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
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.roles.domain.CUserProjectRole;

@Entity
@Table (name = "cuserprojectsettings", uniqueConstraints = @UniqueConstraint (columnNames = {
		"user_id", "project_id"
})) // table name for the entity
@AttributeOverride (name = "id", column = @Column (name = "cuserprojectsettings_id"))
public class CUserProjectSettings extends CEntityDB<CUserProjectSettings> {

	public static final String DEFAULT_COLOR = "#DC143C";
	public static final String DEFAULT_ICON = "vaadin:tasks";
	public static final String VIEW_NAME = "User Settings View";
	@Column
	@AMetaData (displayName = "Permissions", required = false, readOnly = false, description = "User's project permission", hidden = false)
	private String permission;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "project_id", nullable = false)
	@AMetaData (
			displayName = "Project", required = false, readOnly = false, description = "User's project", hidden = false,
			setBackgroundFromColor = true, useIcon = true, dataProviderBean = "context", dataProviderMethod = "getAvailableProjects"
	)
	private CProject project;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "role_id", nullable = true)
	@AMetaData (
			displayName = "Project Role", required = false, readOnly = false, description = "User's role in this project", hidden = false,
			dataProviderBean = "CUserProjectRoleService", setBackgroundFromColor = true, useIcon = true
	)
	private CUserProjectRole role;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "user_id", nullable = false)
	@AMetaData (
			displayName = "User", required = false, readOnly = false, description = "Project user", hidden = false, setBackgroundFromColor = true,
			useIcon = true, dataProviderBean = "context", dataProviderMethod = "getAvailableUsers"
	)
	private CUser user;

	public CUserProjectSettings() {
		super(CUserProjectSettings.class);
	}

	public String getPermission() { return permission; }

	public CProject getProject() { return project; }

	public String getProjectName() { return project != null ? project.getName() : "Unknown Project"; }

	public CUserProjectRole getRole() { return role; }

	public CUser getUser() { return user; }

	public void setPermission(final String permission) { this.permission = permission; }

	public void setProject(final CProject project) { this.project = project; }

	public void setRole(final CUserProjectRole role) { this.role = role; }

	public void setUser(final CUser user) { this.user = user; }

	@Override
	public String toString() {
		return String.format("UserProjectSettings[user id=%s, project id=%s, role=%s, permission=%s]",
				user != null ? CSpringAuxillaries.safeGetId(user) : null, project != null ? CSpringAuxillaries.safeGetId(project) : null,
				CSpringAuxillaries.safeToString(role), permission);
	}
}
