package tech.derbent.base.users.domain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.avatar.Avatar;
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
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.interfaces.IFieldInfoGenerator;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.CImageUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;

@Entity
@Table (name = "cuser", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"login", "company_id"
})) // Using quoted identifier to ensure exact case matching in
@AttributeOverride (name = "id", column = @Column (name = "user_id"))
public class CUser extends CEntityOfCompany<CUser> implements ISearchable, IFieldInfoGenerator, IHasIcon, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#6CAFB0"; // CDE Light Green - individual people
	public static final String DEFAULT_ICON = "vaadin:user";
	public static final String ENTITY_TITLE_PLURAL = "Users";
	public static final String ENTITY_TITLE_SINGULAR = "User";
	/** Icon size for user icons in pixels */
	public static final int ICON_SIZE = 16;
	private static final Logger LOGGER = LoggerFactory.getLogger(CUser.class);
	public static final int MAX_LENGTH_NAME = 255;
	public static final String VIEW_NAME = "Users View";

	/** Creates an icon from image data using proper SVG wrapping. This method embeds images in SVG which is then directly rendered in the DOM.
	 * @param imageData Binary image data (PNG/JPEG)
	 * @return Icon component with properly rendered image
	 * @throws IllegalArgumentException if image data is null or empty */
	private static Icon createIconFromImageData(final byte[] imageData) {
		Check.notNull(imageData, "Image data cannot be null");
		Check.isTrue(imageData.length > 0, "Image data cannot be empty");
		// Encode image data as base64 data URL
		final String base64Image = Base64.getEncoder().encodeToString(imageData);
		final String mimeType = detectMimeType(imageData);
		final String dataUrl = "data:" + mimeType + ";base64," + base64Image;
		// Create an SVG that contains the image
		final String svgContent = String.format(
				"<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 %d %d\">"
						+ "<image href=\"%s\" width=\"%d\" height=\"%d\" " + "style=\"border-radius: 2px;\"/></svg>",
				ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, dataUrl, ICON_SIZE, ICON_SIZE);
		return createSvgIcon(svgContent);
	}

	private static String createSvgDataUrl(final String svgContent) {
		Check.notBlank(svgContent, "SVG content cannot be null or blank");
		final String encodedSvg = URLEncoder.encode(svgContent, StandardCharsets.UTF_8);
		return "data:image/svg+xml;charset=utf-8," + encodedSvg;
	}

	/** Creates a custom Icon component that can render SVG content.
	 * @param svgContent The SVG markup to render
	 * @return Icon component that will properly render the SVG */
	private static Icon createSvgIcon(final String svgContent) {
		Check.notBlank(svgContent, "SVG content cannot be null or blank");
		final String svgDataUrl = createSvgDataUrl(svgContent);
		final Icon icon = new Icon();
		icon.getElement().setAttribute("icon", svgDataUrl);
		icon.setSize(ICON_SIZE + "px");
		return CColorUtils.styleIcon(icon);
	}

	/** Detects MIME type from image data signature.
	 * @param imageData Binary image data
	 * @return MIME type string (e.g., "image/png", "image/jpeg") */
	private static String detectMimeType(final byte[] imageData) {
		if (imageData.length < 4) {
			return "image/png"; // Default fallback
		}
		// Check for PNG signature (89 50 4E 47)
		if ((imageData[0] & 0xFF) == 0x89 && (imageData[1] & 0xFF) == 0x50 && (imageData[2] & 0xFF) == 0x4E && (imageData[3] & 0xFF) == 0x47) {
			return "image/png";
		}
		// Check for JPEG signature (FF D8)
		if ((imageData[0] & 0xFF) == 0xFF && (imageData[1] & 0xFF) == 0xD8) {
			return "image/jpeg";
		}
		return "image/png"; // Default fallback
	}

	@OneToMany (fetch = FetchType.LAZY)
	@OrderColumn (name = "item_index")
	@AMetaData (
			displayName = "Activities", required = false, readOnly = false, description = "" + "List of activities created by this user",
			hidden = true, useDualListSelector = true, dataProviderBean = "CActivityService", dataProviderMethod = "listByUser"
	)
	private List<CActivity> activities;
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "user_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "User documents (CV, certifications, etc.)",
			hidden = false, dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
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
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "user_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this user", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
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
										/** Default constructor for JPA. */
	protected CUser() {}

	public CUser(final String name, final CCompany company) {
		super(CUser.class, name, company);
		initializeDefaults();
	}

	public CUser(final String username, final String password, final String name, final String email, final CCompany company,
			final CUserCompanyRole companyRole) {
		super(CUser.class, name, company);
		initializeDefaults();
		login = username;
		this.email = email;
		setPassword(password);
		setCompany(company, companyRole);
	}

	/** Add a project setting to this user and maintain bidirectional relationship.
	 * @param projectSettings1 the project settings to add */
	public void addProjectSettings(final CUserProjectSettings projectSettings1) {
		if (projectSettings1 == null) {
			return;
		}
		if (projectSettings == null) {
			projectSettings = new ArrayList<>();
		}
		if (projectSettings.contains(projectSettings1)) {
			return;
		}
		projectSettings.add(projectSettings1);
		projectSettings1.setUser(this);
	}

	/** Copies entity fields to target entity. Override to add CUser-specific fields.
	 * @param target  The target entity
	 * @param options Clone options to control copying behavior */
	
	@Override
	public boolean equals(final Object o) {
		return super.equals(o);
	}

	public List<CActivity> getActivities() { return activities; }

	// IHasAttachments interface methods
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	public Boolean getAttributeDisplaySectionsAsTabs() { return attributeDisplaySectionsAsTabs == null ? false : attributeDisplaySectionsAsTabs; }

	/** Creates an Avatar component for this user with proper initials and color. This is the PROPER way to display user avatars in Vaadin. Avatar
	 * component has built-in support for initials, colors, and profile pictures.
	 * @return Avatar component configured for this user */
	public Avatar getAvatar() {
		final Avatar avatar = new Avatar();
		// Set user's name for the avatar (use toString() for display name)
		final String displayName = toString();
		avatar.setName(displayName);
		// Set initials
		final String initials = getInitials();
		avatar.setAbbreviation(initials);
		// Set color based on name hash for consistency
		final int colorIndex = Math.abs(displayName.hashCode() % 7); // Vaadin supports 7 color variants
		avatar.setColorIndex(colorIndex);
		// Set profile picture if available
		if (profilePictureThumbnail != null && profilePictureThumbnail.length > 0) {
			try {
				final String base64Image = Base64.getEncoder().encodeToString(profilePictureThumbnail);
				final String mimeType = detectMimeType(profilePictureThumbnail);
				final String dataUrl = "data:" + mimeType + ";base64," + base64Image;
				avatar.setImage(dataUrl);
			} catch (final Exception e) {
				LOGGER.error("Failed to set avatar image", e);
				// Avatar will fall back to showing initials
			}
		}
		return avatar;
	}

	@Override
	public Class<?> getClassName() { return CUser.class; }

	@Override
	public String getColor() { return color != null ? color : DEFAULT_COLOR; }

	// IHasComments interface methods
	@Override
	public Set<CComment> getComments() { return comments; }

	public CUserCompanyRole getCompanyRole() { return companyRole; }

	public String getEmail() { return email; }

	@Override
	public Icon getIcon() {
		// Use thumbnail if available for efficient rendering
		if (profilePictureThumbnail != null && profilePictureThumbnail.length > 0) {
			return createIconFromImageData(profilePictureThumbnail);
		}
		// Generate SVG avatar with initials when no profile picture is available
		// This is more efficient and produces better quality than PNG avatars
		try {
			final String initials = getInitials();
			final String svgContent = CImageUtils.generateAvatarSvg(initials, ICON_SIZE);
			return createSvgIcon(svgContent);
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
		if (getName() != null && !getName().trim().isEmpty()) {
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
		if (lastname != null && !lastname.trim().isEmpty() && initials.length() < 2) {
			initials += lastname.substring(0, 1).toUpperCase();
		}
		// Fall back to username if no name is available
		if (initials.isEmpty() && login != null && !login.trim().isEmpty()) {
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

	private final void initializeDefaults() {
		attributeDisplaySectionsAsTabs = true;
		color = DEFAULT_COLOR;
		lastname = "";
		email = "";
		phone = "1234567";
		attributeDisplaySectionsAsTabs = true;
		password = ""; // Empty - user must set password
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
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
		if (searchValue == null || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		// Check string fields
		if (fieldNames.remove("login") && getLogin() != null && getLogin().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("email") && getEmail() != null && getEmail().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("lastname") && getLastname() != null && getLastname().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("phone") && getPhone() != null && getPhone().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("color") && getColor() != null && getColor().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		// Check entity fields
		if (fieldNames.remove("companyRole") && getCompanyRole() != null && getCompanyRole().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		// Check boolean field
		if (fieldNames.remove("attributeDisplaySectionsAsTabs") && getAttributeDisplaySectionsAsTabs() != null
				&& getAttributeDisplaySectionsAsTabs().toString().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		return false;
	}

	/** Remove a project setting from this user and maintain bidirectional relationship.
	 * @param projectSettings1 the project settings to remove */
	public void removeProjectSettings(final CUserProjectSettings projectSettings1) {
		Check.notNull(projectSettings1, "Project settings cannot be null");
		Check.notNull(projectSettings, "User's project settings collection cannot be null");
		if (projectSettings.remove(projectSettings1)) {
			projectSettings1.setUser(null);
		}
	}

	public void setActivities(final List<CActivity> activities) { this.activities = activities; }

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setAttributeDisplaySectionsAsTabs(final Boolean attributeDisplaySectionsAsTabs) {
		this.attributeDisplaySectionsAsTabs = attributeDisplaySectionsAsTabs;
	}

	@Override
	public void setColor(final String color) { this.color = color; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

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
				// LOGGER.debug("Generated thumbnail for user profile picture: {} bytes -> {} bytes",
				// profilePictureData.length,profilePictureThumbnail.length);
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
