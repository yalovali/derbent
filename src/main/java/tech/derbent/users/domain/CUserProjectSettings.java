package tech.derbent.users.domain;

import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import tech.derbent.abstracts.domains.CEntityDB;

@Entity
public class CUserProjectSettings extends CEntityDB {

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private CUser user;
	@Column(name = "project_id", nullable = false)
	private Long projectId;
	@ElementCollection
	@CollectionTable(name = "cuser_project_roles", joinColumns = @JoinColumn(name = "settings_id"))
	@Column(name = "role")
	private Set<String> roles;
	@ElementCollection
	@CollectionTable(name = "cuser_project_permissions", joinColumns = @JoinColumn(name = "settings_id"))
	@Column(name = "permission")
	private Set<String> permissions;

	public CUserProjectSettings() {}

	public Set<String> getPermissions() { return permissions; }

	public Long getProjectId() { return projectId; }

	public Set<String> getRoles() { return roles; }

	public CUser getUser() { return user; }

	public void setPermissions(final Set<String> permissions) { this.permissions = permissions; }

	public void setProjectId(final Long projectId) { this.projectId = projectId; }

	public void setRoles(final Set<String> roles) { this.roles = roles; }

	public void setUser(final CUser user) { this.user = user; }
}