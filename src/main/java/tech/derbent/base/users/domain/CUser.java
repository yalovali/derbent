package tech.derbent.base.users.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.Icon;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.interfaces.IFieldInfoGenerator;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.CImageUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.roles.domain.CUserCompanyRole;

@Entity
@Table (name = "cuser", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"login", "company_id"
})) // Using quoted identifier to ensure exact case matching in
@AttributeOverride (name = "id", column = @Column (name = "user_id"))
public class CUser extends CEntityOfCompany<CUser> implements ISearchable, IFieldInfoGenerator, IHasIcon {

	public static final String DEFAULT_COLOR = "#6CAFB0"; // CDE Light Green - individual people
	public static final String DEFAULT_ICON = "vaadin:user";
	public static final String ENTITY_TITLE_PLURAL = "Users";
	public static final String ENTITY_TITLE_SINGULAR = "User";
	/** Icon size for user icons in pixels */
	public static final int ICON_SIZE = 16;
	private static final Logger LOGGER = LoggerFactory.getLogger(CUser.class);
	public static final int MAX_LENGTH_NAME = 255;
	public static final String VIEW_NAME = "Users View";
	@OneToMany (fetch = FetchType.LAZY)
	@OrderColumn (name = "item_index")
	@AMetaData (
			displayName = "Activities", required = false, readOnly = false, description = "" + "List of activities created by this user",
			hidden = true, useDualListSelector = true, dataProviderBean = "CActivityService", dataProviderMethod = "listByUser"
	)
	private List<CActivity> activities;
	@Column (name = "attribute_display_sections_as_tabs", nullable = true)
	@AMetaData (
			displayName = "Display Sections As Tabs", required = false, readOnly = false, defaultValue = "true",
			description = "Whether to display user interface sections as tabs", hidden = false
	)
	private Boolean attributeDisplaySectionsAsTabs;
	@Column (name = "color", nullable = true, length = 7)
	@AMetaData (
			displayName = "Color", required = false, readOnly = false, defaultValue = DEFAULT_COLOR,
			description = "User's color for display purposes", hidden = true
	)
	private String color;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "company_role_id", nullable = true)
	@AMetaData (
			displayName = "Company Role", required = false, readOnly = false, description = "User's role within the company", hidden = false,
			setBackgroundFromColor = true, useIcon = true, dataProviderBean = "CUserCompanyRoleService"
	)
	private CUserCompanyRole companyRole;
	@AMetaData (
			displayName = "Email", required = true, readOnly = false, defaultValue = "", description = "User's email address", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Column (name = "email", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
	@NotBlank (message = ValidationMessages.EMAIL_REQUIRED)
	@Email (message = ValidationMessages.EMAIL_INVALID)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME, message = ValidationMessages.EMAIL_MAX_LENGTH)
	private String email;
	@Column (name = "lastname", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
	@AMetaData (
			displayName = "Last Name", required = true, readOnly = false, defaultValue = "", description = "User's last name", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME, message = ValidationMessages.FIELD_MAX_LENGTH)
	private String lastname;
	@AMetaData (
			displayName = "Login", required = true, readOnly = false, defaultValue = "", description = "Login name for the system", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Column (name = "login", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
	@NotBlank (message = ValidationMessages.FIELD_REQUIRED)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME, message = ValidationMessages.FIELD_MAX_LENGTH)
	private String login;
	@Column (name = "password", nullable = true, length = 255)
	@Size (max = 255, message = ValidationMessages.FIELD_MAX_LENGTH)
	@AMetaData (
			displayName = "Password", required = false, readOnly = false, passwordField = true, description = "User password (stored as hash)",
			hidden = false, passwordRevealButton = false
	)
	private String password; // Encoded password
	@AMetaData (
			displayName = "Phone", required = false, readOnly = false, defaultValue = "", description = "Phone number", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	@Column (name = "phone", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME, message = ValidationMessages.FIELD_MAX_LENGTH)
	private String phone;
	@AMetaData (
			displayName = "Profile Picture", required = false, readOnly = false, defaultValue = "",
			description = "User's profile picture stored as binary data", hidden = false, imageData = true
	)
	@Column (name = "profile_picture_data", nullable = true, length = 10000, columnDefinition = "bytea")
	private byte[] profilePictureData;
	/** Thumbnail version of profile picture for efficient icon rendering (16x16 pixels). Generated automatically when profile picture is set. */
	@Column (name = "profile_picture_thumbnail", nullable = true, length = 5000, columnDefinition = "bytea")
	private byte[] profilePictureThumbnail;
	@OneToMany (mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@AMetaData (
			displayName = "Project Settings", required = false, readOnly = true, description = "User's project memberships and roles", hidden = false,
			createComponentMethod = "createUserProjectSettingsComponent"
	)
	private List<CUserProjectSettings> projectSettings = new ArrayList<>();

	/** Default constructor for JPA. */
	public CUser() {
		super();
	}

	public CUser(final String name, final CCompany company) {
		super(CUser.class, name, company);
	}

	public CUser(final String username, final String password, final String name, final String email, final CCompany company,
			final CUserCompanyRole companyRole) {
		super(CUser.class, name, company);
		login = username;
		this.email = email;
		setPassword(password);
		setCompany(company, companyRole);
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

	/** Creates a custom icon component from image data (PNG format). Uses a clean implementation that creates a vaadin-icon element with an embedded
	 * image. This approach ensures the icon displays correctly in all contexts (grids, labels, menus). CRITICAL FIX: Sets the "icon-class" attribute
	 * to hide the shadow DOM's SVG element, allowing the custom image to be visible. This leverages Vaadin's built-in CSS rule:
	 * :host(:is([icon-class], [font-icon-content])) svg { display: none; }
	 * @param imageData PNG image data (should be 16x16 pixels for optimal display)
	 * @return Icon component containing the image */
	private Icon createIconFromImageData(final byte[] imageData) {
		Check.notNull(imageData, "Image data cannot be null");
		Check.isTrue(imageData.length > 0, "Image data cannot be empty");
		// Create an Icon with a clean DOM structure
		final Icon icon = new Icon();
		// CRITICAL: Set icon-class attribute to hide the shadow DOM's default SVG
		// This allows our custom image to be visible instead of being hidden behind the SVG
		icon.getElement().setAttribute("icon-class", "user-icon-image");
		// Encode image data as base64 data URL
		final String base64Image = Base64.getEncoder().encodeToString(imageData);
		final String dataUrl = "data:image/png;base64," + base64Image;
		// Create img element with proper attributes
		final com.vaadin.flow.dom.Element img = new com.vaadin.flow.dom.Element("img");
		img.setAttribute("src", dataUrl);
		img.setAttribute("alt", "User icon");
		img.setAttribute("loading", "eager"); // Ensure immediate loading
		// Apply critical inline styles for proper rendering
		// These styles ensure the image displays correctly across all browsers
		img.getStyle().set("width", ICON_SIZE + "px").set("height", ICON_SIZE + "px").set("display", "block") // Remove inline spacing
				.set("object-fit", "cover") // Ensure image fills the space
				.set("border-radius", "2px") // Slightly rounded corners
				.set("vertical-align", "middle"); // Align with text
		// Clear any default Icon styles that might interfere
		icon.getElement().getStyle().set("width", ICON_SIZE + "px").set("height", ICON_SIZE + "px").set("display", "inline-flex") // Use flex for
																																	// proper
																																	// alignment
				.set("align-items", "center").set("justify-content", "center").set("overflow", "hidden"); // Clip any overflow
		// Append the image to the icon element
		icon.getElement().appendChild(img);
		// Apply additional styling from CColorUtils for consistency with other icons
		// Note: This adds margin-right and flex-shrink properties
		return CColorUtils.styleIcon(icon);
	}

	@Override
	public boolean equals(final Object o) {
		return super.equals(o);
	}

	public List<CActivity> getActivities() { return activities; }

	public Boolean getAttributeDisplaySectionsAsTabs() { return attributeDisplaySectionsAsTabs == null ? false : attributeDisplaySectionsAsTabs; }

	@Override
	public Class<?> getClassName() { // TODO Auto-generated method stub
		return CUser.class;
	}

	@Override
	public String getColor() { return color != null ? color : DEFAULT_COLOR; }

	public CUserCompanyRole getCompanyRole() { return companyRole; }

	public String getEmail() { return email; }

	@Override
	public Icon getIcon() {
		// COMPREHENSIVE APPROACH: Create a properly styled icon element that wraps an image
		// This ensures reliable rendering of profile pictures and generated avatars across all contexts
		// Use thumbnail if available for efficient rendering
		if (profilePictureThumbnail != null && profilePictureThumbnail.length > 0) {
			return createIconFromImageData(profilePictureThumbnail);
		}
		// Generate avatar with initials when no profile picture is available
		try {
			final String initials = getInitials();
			final byte[] avatarImage = CImageUtils.generateAvatarWithInitials(initials, ICON_SIZE);
			return createIconFromImageData(avatarImage);
		} catch (final Exception e) {
			LOGGER.error("Failed to generate avatar with initials, falling back to default icon", e);
			return CColorUtils.styleIcon(new Icon(DEFAULT_ICON));
		}
	}

	@Override
	public String getIconString() {
		// Return icon string identifier based on profile picture availability
		if (profilePictureThumbnail != null && profilePictureThumbnail.length > 0) {
			return "vaadin:user-card"; // Icon indicating user with profile picture
		}
		return DEFAULT_ICON; // Default user icon
	}

	/** Gets the user's initials for avatar generation. Generates initials from the user's first name and last name. Falls back to login if no name is
	 * available.
	 * @return Initials string (typically 1-2 characters, e.g., "JD" for John Doe) */
	public String getInitials() {
		String initials = "";
		// Get initials from first name
		if ((getName() != null) && !getName().trim().isEmpty()) {
			final String[] nameParts = getName().trim().split("\\s+");
			for (final String part : nameParts) {
				if (!part.isEmpty()) {
					initials += part.substring(0, 1).toUpperCase();
					if (initials.length() >= 2) {
						break; // Limit to 2 initials
					}
				}
			}
		}
		// Add last name initial if we have less than 2 initials
		if ((lastname != null) && !lastname.trim().isEmpty() && (initials.length() < 2)) {
			initials += lastname.substring(0, 1).toUpperCase();
		}
		// Fall back to username if no name is available
		if (initials.isEmpty() && (login != null) && !login.trim().isEmpty()) {
			initials = login.substring(0, Math.min(2, login.length())).toUpperCase();
		}
		// Final fallback
		if (initials.isEmpty()) {
			initials = "U";
		}
		return initials;
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

	public byte[] getProfilePictureThumbnail() { return profilePictureThumbnail; }

	// Getter and setter with safe initialization to prevent lazy loading issues
	public List<CUserProjectSettings> getProjectSettings() { return projectSettings; }

	public String getUsername() {
		return getLogin(); // Convenience method to get username for authentication
	}

	@Override
	public boolean matches(final String searchText) {
		if (searchText == null || searchText.trim().isEmpty()) {
			return true; // Empty search matches all
		}
		final String lowerSearchText = searchText.toLowerCase().trim();
		// Search in name field (first name)
		if (getName() != null && getName().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in lastname field
		if (lastname != null && lastname.toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in login field
		if (login != null && login.toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in email field
		if (email != null && email.toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in description field
		if (getDescription() != null && getDescription().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in ID as string
		if (getId() != null && getId().toString().contains(lowerSearchText)) {
			return true;
		}
		return false;
	}

	/** Checks if this entity matches the given search value in the specified fields. This implementation extends CEntityOfCompany to also search in
	 * user-specific fields like login, email, lastname, phone, and entity references.
	 * @param searchValue the value to search for (case-insensitive)
	 * @param fieldNames  the list of field names to search in. If null or empty, searches only in "name" field. Supported field names: "id",
	 *                    "active", "name", "description", "company", "login", "email", "lastname", "phone", "companyRole",
	 *                    "attributeDisplaySectionsAsTabs"
	 * @return true if the entity matches the search criteria in any of the specified fields */
	@Override
	public boolean matchesFilter(final String searchValue, final java.util.Collection<String> fieldNames) {
		if ((searchValue == null) || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		// Check string fields
		if (fieldNames.remove("login") && (getLogin() != null) && getLogin().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("email") && (getEmail() != null) && getEmail().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("lastname") && (getLastname() != null) && getLastname().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("phone") && (getPhone() != null) && getPhone().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("color") && (getColor() != null) && getColor().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		// Check entity fields
		if (fieldNames.remove("companyRole") && (getCompanyRole() != null)
				&& getCompanyRole().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		// Check boolean field
		if (fieldNames.remove("attributeDisplaySectionsAsTabs") && (getAttributeDisplaySectionsAsTabs() != null)
				&& getAttributeDisplaySectionsAsTabs().toString().toLowerCase().contains(lowerSearchValue)) {
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

	public void setActivities(final List<CActivity> activities) { this.activities = activities; }

	public void setAttributeDisplaySectionsAsTabs(final Boolean attributeDisplaySectionsAsTabs) {
		this.attributeDisplaySectionsAsTabs = attributeDisplaySectionsAsTabs;
	}

	@Override
	public void setColor(final String color) { this.color = color; }

	public void setCompany(final CCompany company, final CUserCompanyRole companyRole) {
		setCompany(company);
		this.companyRole = companyRole;
	}

	public void setEmail(final String email) { this.email = email; }

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

	public void setProfilePictureData(final byte[] profilePictureData) {
		this.profilePictureData = profilePictureData;
		// Automatically generate thumbnail when setting profile picture
		if (profilePictureData != null && profilePictureData.length > 0) {
			try {
				profilePictureThumbnail = CImageUtils.resizeImage(profilePictureData, ICON_SIZE, ICON_SIZE);
				LOGGER.debug("Generated thumbnail for user profile picture: {} bytes -> {} bytes", profilePictureData.length,
						profilePictureThumbnail.length);
			} catch (final Exception e) {
				LOGGER.error("Failed to generate thumbnail for user profile picture", e);
				// If thumbnail generation fails, clear it so we fall back to default icon
				profilePictureThumbnail = null;
			}
		} else {
			// Clear thumbnail when profile picture is cleared
			profilePictureThumbnail = null;
		}
	}

	public void setProfilePictureThumbnail(final byte[] profilePictureThumbnail) { this.profilePictureThumbnail = profilePictureThumbnail; }

	public void setProjectSettings(final List<CUserProjectSettings> projectSettings) {
		this.projectSettings = projectSettings != null ? projectSettings : new ArrayList<>();
	}

	@Override
	public String toString() {
		// Return user-friendly representation for UI display
		if (getName() != null && !getName().trim().isEmpty()) {
			if (lastname != null && !lastname.trim().isEmpty()) {
				return getName() + " " + lastname;
			}
			return getName();
		}
		if (login != null && !login.trim().isEmpty()) {
			return login;
		}
		return "User #" + getId();
	}
}
