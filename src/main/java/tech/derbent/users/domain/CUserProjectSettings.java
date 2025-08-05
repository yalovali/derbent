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
@Table (
	name = "cuserprojectsettings", uniqueConstraints = @UniqueConstraint (columnNames = {
		"user_id", "project_id" }
	)
) // table name for the entity
@AttributeOverride (name = "id", column = @Column (name = "cuserprojectsettings_id"))
public class CUserProjectSettings extends CEntityDB<CUserProjectSettings> {

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

	public String getPermission() { return permission; }

	public CProject getProject() { return project; }

	public String getRole() { return role; }

	public CUser getUser() { return user; }

	public void setPermission(final String permission) { this.permission = permission; }

	public void setProject(final CProject project) { this.project = project; }

	public void setRole(final String role) { this.role = role; }

	public void setUser(final CUser user) { this.user = user; }

	@Override
	public String toString() {
		return String.format(
			"UserProjectSettings[user=%s, project=%s, role=%s, permission=%s]",
			user != null ? user.getLogin() : "null",
			project != null ? project.getName() : "null", role, permission);
	}
}