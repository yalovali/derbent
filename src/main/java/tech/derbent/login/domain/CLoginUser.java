package tech.derbent.login.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.projects.domain.CProject;

/**
 * CLoginUser authentication-specific fields. This class represents a user that
 * can authenticate in the system. Authentication Flow: 1. User enters
 * username/password in CLoginView 2. Spring Security calls CLoginUserService to
 * load user by username 3. CLoginUserService queries database using
 * CLoginUserRepository 4. Password is verified against stored encoded password
 * 5. User roles determine access permissions
 */
@Entity
@Table(name = "cloginuser") // Separate table for login users with authentication data
public class CLoginUser extends CEntityDB {

	/**
	 * Password field for authentication. Stored as encoded hash (never plain text).
	 * Uses BCrypt encoding for security.
	 */
	@Column(name = "password", nullable = false, length = 255)
	@Size(max = 255)
	@MetaData(displayName = "Password", required = true, readOnly = false, description = "User password (stored as hash)", hidden = true)
	private String password;
	/**
	 * User roles for authorization (e.g., "USER", "ADMIN"). Comma-separated string
	 * of roles for simplicity. Used by Spring Security for access control.
	 */
	@Column(name = "roles", nullable = false, length = 255)
	@Size(max = 255)
	@MetaData(displayName = "Roles", required = true, readOnly = false, defaultValue = "USER", description = "User roles (comma-separated)", hidden = false)
	private String roles = "USER";
	/**
	 * Flag to enable/disable user account. Disabled users cannot authenticate.
	 */
	@Column(name = "enabled", nullable = false)
	@MetaData(displayName = "Enabled", required = true, readOnly = false, defaultValue = "true", description = "Whether user account is enabled", hidden = false)
	private boolean enabled = true;
	@MetaData(displayName = "Login", required = true, readOnly = false, defaultValue = "-", description = "Login name for the system", hidden = false)
	@Column(name = "login", nullable = true, length = MAX_LENGTH_NAME, unique = true)
	@Size(max = MAX_LENGTH_NAME)
	private String login;
	@MetaData(displayName = "Email", required = true, readOnly = false, defaultValue = "", description = "", hidden = false)
	@Column(name = "email", nullable = true, length = MAX_LENGTH_NAME, unique = false)
	@Size(max = MAX_LENGTH_NAME)
	private String email;
	@Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = false)
	@Size(max = MAX_LENGTH_NAME)
	@MetaData(displayName = "User Name", required = true, readOnly = false, defaultValue = "-", description = "User's first name", hidden = false)
	private String name;
	@Column(name = "lastname", nullable = true, length = MAX_LENGTH_NAME, unique = false)
	@MetaData(displayName = "Last Name", required = true, readOnly = false, defaultValue = "-", description = "User's lastname", hidden = false)
	@Size(max = MAX_LENGTH_NAME)
	private String lastname;
	// inside your class
	@ManyToMany
	@JoinTable(name = "cloginuser_project", joinColumns = @JoinColumn(name = "login_user_id"), inverseJoinColumns = @JoinColumn(name = "project_id"))
	private Set<CProject> projects = new HashSet<>();
	@MetaData(displayName = "Phone", required = true, readOnly = false, defaultValue = "-", description = "Phone number", hidden = false)
	@Column(name = "phone", nullable = true, length = MAX_LENGTH_NAME, unique = false)
	@Size(max = MAX_LENGTH_NAME)
	private String phone;
	@Column(name = "created_date", nullable = true)
	private LocalDateTime created_date;
	@Column(name = "updated_date", nullable = true)
	private LocalDateTime updated_date;

	/**
	 * Default constructor required by JPA.
	 */
	public CLoginUser() {
		super();
	}

	/**
	 * Constructor for creating new login user with basic info. Password should be
	 * encoded before setting.
	 * @param username the login username
	 * @param password the encoded password
	 * @param name     the user's first name
	 * @param email    the user's email
	 */
	public CLoginUser(final String username, final String password, final String name, final String email) {
		super();
		setLogin(username); // Use inherited login field as username
		this.password = password;
		setName(name);
		setEmail(email);
	}
	// Getters and Setters

	public String getEmail() { return email; }

	public String getLastname() { return lastname; }

	public String getLogin() { return login; }

	public String getName() { return name; }

	/**
	 * @return the encoded password
	 */
	public String getPassword() { return password; }

	public String getPhone() { return phone; }

	public Set<CProject> getProjects() { return projects; }

	/**
	 * @return comma-separated string of user roles
	 */
	public String getRoles() { return roles; }

	/**
	 * Convenience method to get username for authentication. Uses the inherited
	 * login field from CUser.
	 * @return the username (login field)
	 */
	public String getUsername() { return getLogin(); }

	/**
	 * @return true if user account is enabled
	 */
	public boolean isEnabled() { return enabled; }

	public void setEmail(final String email) { this.email = email; }

	/**
	 * Sets whether user account is enabled.
	 * @param enabled true to enable account, false to disable
	 */
	public void setEnabled(final boolean enabled) { this.enabled = enabled; }

	public void setLastname(final String lastname) { this.lastname = lastname; }

	public void setLogin(final String login) { this.login = login; }

	public void setName(final String name) { this.name = name; }

	/**
	 * Sets the password. Should be encoded before calling this method.
	 * @param password the encoded password
	 */
	public void setPassword(final String password) { this.password = password; }

	public void setPhone(final String phone) { this.phone = phone; }

	public void setProjects(final Set<CProject> projects) { this.projects = projects; }

	/**
	 * Sets user roles as comma-separated string.
	 * @param roles the roles (e.g., "USER,ADMIN")
	 */
	public void setRoles(final String roles) { this.roles = roles; }

	/**
	 * Convenience method to set username. Sets the inherited login field from
	 * CUser.
	 * @param username the username to set
	 */
	public void setUsername(final String username) {
		setLogin(username);
	}
}