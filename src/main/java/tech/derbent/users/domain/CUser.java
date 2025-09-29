package tech.derbent.users.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.interfaces.IFieldInfoGenerator;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;

@Entity
@Table (name = "cuser") // Using quoted identifier to ensure exact case matching in
// PostgreSQL
@AttributeOverride (name = "id", column = @Column (name = "user_id"))
public class CUser extends CEntityNamed<CUser> implements ISearchable, IFieldInfoGenerator {

	public static final String DEFAULT_COLOR = "#00546d";
	public static final String DEFAULT_ICON = "vaadin:book";
	public static final int MAX_LENGTH_NAME = 255;
	public static final String VIEW_NAME = "Users View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "company_id", nullable = true)
	@AMetaData (displayName = "Company", required = false, readOnly = false, description = "Company the user belongs to", hidden = false, order = 10)
	private CCompany company;
	@AMetaData (
			displayName = "Email", required = true, readOnly = false, defaultValue = "", description = "User's email address", hidden = false,
			order = 4, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Column (name = "email", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	private String email;
	@AMetaData (
			displayName = "Enabled", required = true, readOnly = false, defaultValue = "true", description = "Is user account enabled?",
			hidden = false, order = 8
	)
	@Column (name = "enabled", nullable = false)
	private Boolean enabled = Boolean.TRUE; // User account status, default is enabled
	@Column (name = "lastname", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
	@AMetaData (
			displayName = "Last Name", required = true, readOnly = false, defaultValue = "", description = "User's last name", hidden = false,
			order = 2, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	private String lastname;
	@AMetaData (
			displayName = "Login", required = true, readOnly = false, defaultValue = "", description = "Login name for the system", hidden = false,
			order = 3, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Column (name = "login", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME, unique = true)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	private String login;
	@Column (name = "password", nullable = true, length = 255)
	@Size (max = 255)
	@AMetaData (
			displayName = "Password", required = false, readOnly = false, passwordField = true, description = "User password (stored as hash)",
			hidden = false, order = 99, passwordRevealButton = false
	)
	private String password; // Encoded password
	@AMetaData (
			displayName = "Phone", required = false, readOnly = false, defaultValue = "", description = "Phone number", hidden = false, order = 5,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Column (name = "phone", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	private String phone;
	@AMetaData (
			displayName = "Profile Picture", required = false, readOnly = false, defaultValue = "",
			description = "User's profile picture stored as binary data", hidden = false, order = 11, imageData = true
	)
	@Column (name = "profile_picture_data", nullable = true, length = 10000, columnDefinition = "bytea")
	private byte[] profilePictureData;
	// load it eagerly because there a few projects that use this field
	// Single company settings - one user can have access to one company only
	@OneToOne (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "single_company_settings_id", nullable = true)
	@AMetaData (
			displayName = "Company Setting", required = false, readOnly = false, description = "User's company membership and role", hidden = false,
			order = 15, createComponentMethod = "createSingleCompanyUserSettingComponent"
	)
	private CUserCompanySetting companySetting;
	@OneToMany (mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@AMetaData (
			displayName = "Project Settings", required = false, readOnly = true, description = "User's project memberships and roles", hidden = false,
			order = 20, createComponentMethod = "createUserProjectSettingsComponent"
	)
	private List<CUserProjectSettings> projectSettings = new ArrayList<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "cusertype_id", nullable = true)
	@AMetaData (displayName = "User Type", required = false, readOnly = false, description = "Type category of the user", hidden = false, order = 9)
	private CUserType userType;

	/** Default constructor for JPA. */
	public CUser() {
		super();
		// Initialize with default values for JPA
		enabled = true;
	}

	public CUser(final String name) {
		super(CUser.class, name);
	}

	public CUser(final String username, final String password, final String name, final String email) {
		super(CUser.class, name);
		login = username;
		this.email = email;
		setPassword(password);
	}

	public CUser(final String username, final String password, final String name, final String email, final String roles) {
		super(CUser.class, name);
		login = username;
		super.setName(name);
		this.email = email;
		setPassword(password);
	}

	/** Add a project setting to this user and maintain bidirectional relationship.
	 * @param projectSettings the project settings to add */
	public void addProjectSettings(final CUserProjectSettings projectSettings) {
		if (projectSettings == null) {
			return;
		}
		if (this.projectSettings == null) {
			this.projectSettings = new ArrayList<>();
		}
		if (!this.projectSettings.contains(projectSettings)) {
			this.projectSettings.add(projectSettings);
			projectSettings.setUser(this);
		}
	}

	@Override
	public boolean equals(final Object o) {
		return super.equals(o);
	}

	@Override
	public Class<?> getClassName() { // TODO Auto-generated method stub
		return CUser.class;
	}

	public CCompany getCompany() { return company; }

	public CUserCompanySetting getCompanySettings() { return companySetting; }

	public String getEmail() { return email; }

	public Boolean getEnabled() {
		return enabled; // Return the enabled status
	}

	public String getLastname() { return lastname; }

	public String getLogin() { return login; }

	@Override
	public String getName() { return super.getName(); }

	public String getPassword() {
		return password; // Return the encoded password
	}

	public String getPhone() { return phone; }

	public byte[] getProfilePictureData() { return profilePictureData; }

	// Getter and setter with safe initialization to prevent lazy loading issues
	public List<CUserProjectSettings> getProjectSettings() { return projectSettings; }

	public String getUsername() {
		return getLogin(); // Convenience method to get username for authentication
	}

	public CUserType getUserType() { return userType; }

	public Boolean isEnabled() {
		return enabled; // Return the enabled status
	}

	@Override
	public boolean matches(final String searchText) {
		if ((searchText == null) || searchText.trim().isEmpty()) {
			return true; // Empty search matches all
		}
		final String lowerSearchText = searchText.toLowerCase().trim();
		// Search in name field (first name)
		if ((getName() != null) && getName().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in lastname field
		if ((lastname != null) && lastname.toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in login field
		if ((login != null) && login.toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in email field
		if ((email != null) && email.toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in description field
		if ((getDescription() != null) && getDescription().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in ID as string
		if ((getId() != null) && getId().toString().contains(lowerSearchText)) {
			return true;
		}
		return false;
	}

	/** Remove a project setting from this user and maintain bidirectional relationship.
	 * @param projectSettings the project settings to remove */
	public void removeProjectSettings(final CUserProjectSettings projectSettings) {
		Check.notNull(projectSettings, "Project settings cannot be null");
		Check.notNull(this.projectSettings, "User's project settings collection cannot be null");
		if (this.projectSettings.remove(projectSettings)) {
			projectSettings.setUser(null);
		}
	}

	public void setCompany(final CCompany company) { this.company = company; }

	public void setCompanySettings(final CUserCompanySetting companySetting) {
		this.companySetting = companySetting;
		// Maintain bidirectional relationship
		if (companySetting != null) {
			companySetting.setUser(this);
		}
	}

	public void setEmail(final String email) { this.email = email; }

	public void setEnabled(final Boolean enabled) {
		this.enabled = enabled; // Set the enabled status
	}

	public void setLastname(final String lastname) { this.lastname = lastname; }

	public void setLogin(final String login) { this.login = login; }

	@Override
	public void setName(final String name) {
		super.setName(name);
	}

	public void setPassword(final String password) {
		// Password should be encoded before setting
		this.password = password; // Assuming password is already encoded
	}

	public void setPhone(final String phone) { this.phone = phone; }

	public void setProfilePictureData(final byte[] profilePictureData) { this.profilePictureData = profilePictureData; }

	public void setProjectSettings(final List<CUserProjectSettings> projectSettings) {
		this.projectSettings = projectSettings != null ? projectSettings : new ArrayList<>();
	}

	public void setUserType(final CUserType userType) { this.userType = userType; }

	/** Returns a comprehensive string representation of the user including all key fields. Note: This method is used for debugging and logging
	 * purposes. For ComboBox display in the UI, the CEntityFormBuilder now uses getName() method automatically to show only the user's name instead
	 * of all fields. This resolves the combobox display issue where users were listed with complete text with all fields.
	 * @return detailed string representation of the user */
	@Override
	public String toString() {
		// Return user-friendly representation for UI display
		if ((getName() != null) && !getName().trim().isEmpty()) {
			if ((lastname != null) && !lastname.trim().isEmpty()) {
				return getName() + " " + lastname;
			}
			return getName();
		}
		if ((login != null) && !login.trim().isEmpty()) {
			return login;
		}
		return "User #" + getId();
	}
}
