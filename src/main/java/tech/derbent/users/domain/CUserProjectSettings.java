package tech.derbent.users.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "cuserprojectsettings", uniqueConstraints = @UniqueConstraint (columnNames = {
		"user_id", "project_id"
})) // table name for the entity
@AttributeOverride (name = "id", column = @Column (name = "cuserprojectsettings_id"))
public class CUserProjectSettings extends CEntityDB<CUserProjectSettings> {

	public static void addUserToProject(final CProject project, final CUser user, final CUserProjectSettings settings) {
		if ((project == null) || (user == null)) {
			throw new IllegalArgumentException("Project and User cannot be null");
		}
		if (settings == null) {
			throw new IllegalArgumentException("UserProjectSettings cannot be null");
		}
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
		if ((project == null) || (user == null)) {
			throw new IllegalArgumentException("Project and User cannot be null");
		}
		// Remove from project's user settings
		project.getUserSettings().removeIf(settings -> settings.getUser().equals(user));
		// Remove from user's project settings
		if (user.getProjectSettings() != null) {
			user.getProjectSettings().removeIf(settings -> settings.getProject().equals(project));
		}
	}

	@ManyToOne
	@JoinColumn (name = "user_id", nullable = false)
	private CUser user;
	@ManyToOne
	@JoinColumn (name = "project_id", nullable = false)
	private CProject project;
	@Column (name = "role")
	private String role;
	@Column
	private String permission;

	public CUserProjectSettings() {
		super(CUserProjectSettings.class);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return null;
	}

	public String getPermission() { return permission; }

	public CProject getProject() { return project; }

	public String getProjectName() { return project != null ? project.getName() : "Unknown Project"; }

	public String getRole() { return role; }

	public CUser getUser() { return user; }

	@Override
	public Class<?> getViewClass() { // TODO Auto-generated method stub
		return null;
	}

	public void setPermission(final String permission) { this.permission = permission; }

	public void setProject(final CProject project) { this.project = project; }

	public void setRole(final String role) { this.role = role; }

	public void setUser(final CUser user) { this.user = user; }

	@Override
	public String toString() {
		return String.format("UserProjectSettings[user=%s, project=%s, role=%s, permission=%s]", user != null ? user.getLogin() : "null",
				project != null ? project.getName() : "null", role, permission);
	}
}
