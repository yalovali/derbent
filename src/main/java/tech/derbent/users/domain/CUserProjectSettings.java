package tech.derbent.users.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.utils.Check;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "cuserprojectsettings", uniqueConstraints = @UniqueConstraint (columnNames = {
		"user_id", "project_id"
})) // table name for the entity
@AttributeOverride (name = "id", column = @Column (name = "cuserprojectsettings_id"))
public class CUserProjectSettings extends CEntityDB<CUserProjectSettings> {

	public static final String VIEW_NAME = "User Settings View";

	public static void addUserToProject(final CProject project, final CUser user, final CUserProjectSettings settings) {
		Check.notNull(project, "Project must not be null");
		Check.notNull(user, "User must not be null");
		Check.notNull(settings, "UserProjectSettings must not be null");
		// Set the relationships in the settings object
		settings.setProject(project);
		settings.setUser(user);
		// Remove any existing relationship to avoid duplicates
		removeUserFromProject(project, user);
		// Add to both sides of the bidirectional relationship
		project.getUserSettings().add(settings);
		// Initialize user's project settings list if null
		if (user.getProjectSettings() == null) {
			user.setProjectSettings(new java.util.ArrayList<>());
		}
		user.getProjectSettings().add(settings);
	}

	// remove a user from project
	public static void removeUserFromProject(final CProject project, final CUser user) {
		Check.notNull(project, "Project must not be null");
		Check.notNull(user, "User must not be null");
		// Remove from project's user settings
		project.getUserSettings().removeIf(settings -> settings.getUser().equals(user));
		// Remove from user's project settings
		if (user.getProjectSettings() != null) {
			user.getProjectSettings().removeIf(settings -> settings.getProject().equals(project));
		}
	}

	@Column
	@AMetaData (
			displayName = "Permissions", required = false, readOnly = false, description = "User's project permission", hidden = false, order = 13
	)
	private String permission;
	@ManyToOne
	@JoinColumn (name = "project_id", nullable = false)
	@AMetaData (
			displayName = "Project", required = false, readOnly = false, description = "User's project", hidden = false, order = 5,
			setBackgroundFromColor = true, useIcon = true, dataProviderOwner = "content", dataProviderMethod = "getAvailableProjects"
	)
	private CProject project;
	@ManyToOne
	@JoinColumn (name = "role_id", nullable = true)
	@AMetaData (
			displayName = "Project Role", required = false, readOnly = false, description = "User's role in this project", hidden = false, order = 5,
			dataProviderClass = tech.derbent.api.roles.service.CUserProjectRoleService.class, setBackgroundFromColor = true, useIcon = true
	)
	private CUserProjectRole role;
	@ManyToOne
	@JoinColumn (name = "user_id", nullable = false)
	@AMetaData (
			displayName = "User", required = false, readOnly = false, description = "Project user", hidden = false, order = 3,
			setBackgroundFromColor = true, useIcon = true, dataProviderBean = "CUserService", dataProviderMethod = "findAll"
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
		return String.format("UserProjectSettings[user=%s, project=%s, role=%s, permission=%s]", user != null ? user.getLogin() : "null",
				project != null ? project.getName() : "null", role, permission);
	}
}
