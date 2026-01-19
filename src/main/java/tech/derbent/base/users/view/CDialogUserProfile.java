package tech.derbent.base.users.view;

import java.io.IOException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.server.streams.InMemoryUploadCallback;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadMetadata;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CImageUtils;
import tech.derbent.base.users.domain.CUser;

/** User profile dialog for editing user profile information. Extends CDBEditDialog to provide consistent dialog behavior. Allows users to: - Edit
 * display name - Change password - Upload/delete profile picture */
public class CDialogUserProfile extends CDialogDBEdit<CUser> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogUserProfile.class);
	private static final long MAX_FILE_SIZE = CImageUtils.MAX_IMAGE_SIZE;
	private static final long serialVersionUID = 1L;
	private final Binder<CUser> binder = new Binder<>(CUser.class);
	private PasswordField confirmPasswordField;
	private PasswordField currentPasswordField;
	private CButton deleteProfilePictureButton;
	private TextField lastnameField;
	private TextField nameField;
	private PasswordField newPasswordField;
	private final PasswordEncoder passwordEncoder;
	private Image profilePicturePreview;
	private Upload profilePictureUpload;
	private byte[] temporaryImageData;

	/** Constructor for CUserProfileDialog.
	 * @param user            The user to edit
	 * @param onSave          Callback when user is saved
	 * @param passwordEncoder Password encoder for password validation and encoding
	 * @throws Exception */
	public CDialogUserProfile(final CUser user, final Consumer<CUser> onSave, final PasswordEncoder passwordEncoder) throws Exception {
		super(user, onSave, false); // isNew = false for profile editing
		LOGGER.info("CUserProfileDialog constructor called for user: {}", user != null ? user.getLogin() : "null");
		if (passwordEncoder == null) {
			throw new IllegalArgumentException("PasswordEncoder cannot be null");
		}
		this.passwordEncoder = passwordEncoder;
		setupDialog();
		populateForm();
	}

	/** Creates the password change section. */
	private void createPasswordChangeSection() {
		LOGGER.debug("Creating password change section");
		final Span sectionTitle = new Span("Change Password");
		sectionTitle.getStyle().set("font-weight", "bold");
		sectionTitle.getStyle().set("font-size", "1.1em");
		sectionTitle.getStyle().set("margin-top", "20px");
		currentPasswordField = new PasswordField("Current Password");
		currentPasswordField.setHelperText("Leave empty to keep current password");
		newPasswordField = new PasswordField("New Password");
		confirmPasswordField = new PasswordField("Confirm New Password");
		final FormLayout passwordSection = new FormLayout();
		passwordSection.add(currentPasswordField);
		passwordSection.add(newPasswordField, confirmPasswordField);
		passwordSection.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("300px", 2));
		mainLayout.add(sectionTitle);
		mainLayout.add(passwordSection);
	}

	/** Creates the profile information section with name fields. */
	private void createProfileInfoSection() {
		final Span sectionTitle = new Span("Profile Information");
		sectionTitle.getStyle().set("font-weight", "bold");
		sectionTitle.getStyle().set("font-size", "1.1em");
		nameField = new TextField("First Name");
		nameField.setRequired(true);
		nameField.setMaxLength(CUser.MAX_LENGTH_NAME);
		lastnameField = new TextField("Last Name");
		lastnameField.setRequired(true);
		lastnameField.setMaxLength(CUser.MAX_LENGTH_NAME);
		// Bind fields to data
		try {
			binder.forField(nameField).withValidator(new StringLengthValidator("Name is required", 1, CUser.MAX_LENGTH_NAME)).bind(CUser::getName,
					CUser::setName);
		} catch (final Exception e) {
			LOGGER.error("Failed to bind name field: {} - using simple binding fallback", e.getMessage());
			try {
				binder.bind(nameField, "name");
			} catch (final Exception fallbackException) {
				LOGGER.error("Simple binding fallback also failed for name field: {}", fallbackException.getMessage());
			}
		}
		try {
			binder.forField(lastnameField).withValidator(new StringLengthValidator("Last name is required", 1, CUser.MAX_LENGTH_NAME))
					.bind(CUser::getLastname, CUser::setLastname);
		} catch (final Exception e) {
			LOGGER.error("Failed to bind lastname field: {} - using simple binding fallback", e.getMessage());
			try {
				binder.bind(lastnameField, "lastname");
			} catch (final Exception fallbackException) {
				LOGGER.error("Simple binding fallback also failed for lastname field: {}", fallbackException.getMessage());
			}
		}
		final FormLayout profileSection = new FormLayout();
		profileSection.add(nameField, lastnameField);
		profileSection.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
		mainLayout.add(sectionTitle);
		mainLayout.add(profileSection);
	}

	/** Creates the profile picture upload section. */
	@SuppressWarnings ("unused")
	private void createProfilePictureSection() {
		final Span sectionTitle = new Span("Profile Picture");
		sectionTitle.getStyle().set("font-weight", "bold");
		sectionTitle.getStyle().set("font-size", "1.1em");
		sectionTitle.getStyle().set("margin-top", "20px");
		// Profile picture preview
		profilePicturePreview = new Image();
		profilePicturePreview.setId("profile-picture-preview");
		profilePicturePreview.setWidth("100px");
		profilePicturePreview.setHeight("100px");
		profilePicturePreview.getStyle().set("border-radius", "50%");
		profilePicturePreview.getStyle().set("object-fit", "cover");
		profilePicturePreview.getStyle().set("border", "2px solid var(--lumo-contrast-20pct)");
		// File upload component using modern API
		final InMemoryUploadCallback uploadCallback = (metadata, data) -> {
			LOGGER.info("Profile picture upload received: {} ({} bytes)", metadata.fileName(), data.length);
			try {
				handleProfilePictureUpload(metadata, data);
			} catch (final IOException e) {
				LOGGER.error("Error handling profile picture upload", e);
				CNotificationService.showWarning("Failed to upload profile picture: " + e.getMessage());
			}
		};
		final InMemoryUploadHandler uploadHandler = new InMemoryUploadHandler(uploadCallback);
		profilePictureUpload = new Upload();
		profilePictureUpload.setId("profile-picture-upload");
		profilePictureUpload.setUploadHandler(uploadHandler);
		profilePictureUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
		profilePictureUpload.setMaxFileSize((int) MAX_FILE_SIZE);
		profilePictureUpload.setDropLabel(new Span("Drop image here or click to upload"));
		profilePictureUpload.setUploadButton(CButton.createTertiary("Choose File", null, null));
		profilePictureUpload.addAllFinishedListener(event -> {
			LOGGER.info("Profile picture upload completed");
		});
		profilePictureUpload.addFileRejectedListener(event -> {
			LOGGER.error("Profile picture upload rejected: {}", event.getErrorMessage());
			CNotificationService.showWarning("Failed to upload profile picture: " + event.getErrorMessage());
		});
		// Delete button
		deleteProfilePictureButton = CButton.createError("Remove Picture", null, event -> deleteProfilePicture());
		deleteProfilePictureButton.setId("profile-picture-delete-button");
		final HorizontalLayout pictureControls = new HorizontalLayout();
		pictureControls.add(profilePictureUpload, deleteProfilePictureButton);
		pictureControls.setAlignItems(HorizontalLayout.Alignment.END);
		final VerticalLayout pictureSection = new VerticalLayout();
		pictureSection.add(profilePicturePreview, pictureControls);
		pictureSection.setHorizontalComponentAlignment(VerticalLayout.Alignment.CENTER, profilePicturePreview);
		pictureSection.setPadding(false);
		pictureSection.setSpacing(true);
		// Now update profile picture preview after all components are created
		updateProfilePicturePreview();
		mainLayout.add(sectionTitle);
		mainLayout.add(pictureSection);
	}

	/** Deletes the current profile picture. */
	private void deleteProfilePicture() {
		// Clear temporary data
		temporaryImageData = null;
		// Update preview to default
		setDefaultProfilePicture();
		CNotificationService.showSuccess("Profile picture removed");
	}

	@Override
	public String getDialogTitleString() { return "Edit Profile"; }

	@Override
	protected Icon getFormIcon() { return VaadinIcon.USER.create(); }

	@Override
	public String getFormTitleString() { return "User Profile"; }

	@Override
	public String getSuccessUpdateMessage() { return "Profile updated successfully"; }

	/** Handles profile picture changes on save. */
	private void handleProfilePictureChange() {
		if (temporaryImageData != null) {
			// New picture was uploaded
			getEntity().setProfilePictureData(temporaryImageData);
			LOGGER.info("Profile picture data updated for user: {}", getEntity().getLogin());
		} else if (!deleteProfilePictureButton.isEnabled() && getEntity().getProfilePictureData() != null) {
			// Picture was deleted
			getEntity().setProfilePictureData(null);
			LOGGER.info("Profile picture removed for user: {}", getEntity().getLogin());
		}
	}

	/** Handles profile picture upload using modern Vaadin Upload API. */
	private void handleProfilePictureUpload(final UploadMetadata metadata, final byte[] data) throws IOException {
		LOGGER.info("Handling profile picture upload: {} ({} bytes)", metadata.fileName(), data.length);
		final String fileName = metadata.fileName();
		if (fileName == null || fileName.trim().isEmpty()) {
			throw new IOException("Invalid file name");
		}
		// Validate image data
		CImageUtils.validateImageData(data, fileName);
		// Resize image to standard profile picture size
		final byte[] resizedImageData = CImageUtils.resizeToProfilePicture(data);
		// Store temporary image data
		temporaryImageData = resizedImageData;
		// Update preview
		final String dataUrl = CImageUtils.createDataUrl(resizedImageData);
		profilePicturePreview.setSrc(dataUrl);
		deleteProfilePictureButton.setEnabled(true);
		CNotificationService.showSuccess("Profile picture uploaded and resized successfully");
	}

	@Override
	protected void populateForm() {
		LOGGER.info("Populating form for user: {}", getEntity() != null ? getEntity().getLogin() : "null");
		if (getEntity() == null) {
			LOGGER.warn("Cannot populate form - data is null");
			return;
		}
		// Bind data to form
		binder.setBean(getEntity());
		// Update profile picture preview
		updateProfilePicturePreview();
		// Clear password fields
		currentPasswordField.clear();
		newPasswordField.clear();
		confirmPasswordField.clear();
		LOGGER.debug("Form populated successfully");
	}

	@Override
	protected void save() {
		LOGGER.info("Saving user profile for user: {}", getEntity() != null ? getEntity().getLogin() : "null");
		try {
			validateForm();
			// Write form data to bean
			binder.writeBean(getEntity());
			// Handle password change if needed
			final String newPassword = newPasswordField.getValue();
			if (!newPassword.isEmpty()) {
				final String encodedPassword = passwordEncoder.encode(newPassword);
				getEntity().setPassword(encodedPassword);
				LOGGER.info("Password updated for user: {}", getEntity().getLogin());
			}
			// Handle profile picture change
			handleProfilePictureChange();
			// Call parent save method
			super.save();
		} catch (final Exception e) {
			LOGGER.error("Error saving user profile", e);
			CNotificationService.showWarning("Failed to save profile: " + e.getMessage());
		}
	}

	/** Sets the default profile picture (user icon). */
	private void setDefaultProfilePicture() {
		// Use the default profile picture from CImageUtils
		profilePicturePreview.setSrc(CImageUtils.getDefaultProfilePictureDataUrl());
		deleteProfilePictureButton.setEnabled(false);
		LOGGER.debug("Set default profile picture");
	}

	@Override
	protected void setupContent() throws Exception {
		LOGGER.debug("Setting up user profile dialog content");
		super.setupContent();
		// Create form sections
		createProfileInfoSection();
		createPasswordChangeSection();
		createProfilePictureSection();
		// Set dialog width to accommodate all sections
		setWidth("600px");
	}

	/** Updates the profile picture preview based on current user data. */
	private void updateProfilePicturePreview() {
		LOGGER.debug("Updating profile picture preview for user: {}", getEntity() != null ? getEntity().getLogin() : "null");
		if (getEntity() != null && getEntity().getProfilePictureData() != null && getEntity().getProfilePictureData().length > 0) {
			final String dataUrl = CImageUtils.createDataUrl(getEntity().getProfilePictureData());
			if (dataUrl != null) {
				profilePicturePreview.setSrc(dataUrl);
				deleteProfilePictureButton.setEnabled(true);
				LOGGER.debug("Set profile picture preview from database data");
			} else {
				setDefaultProfilePicture();
			}
		} else {
			setDefaultProfilePicture();
		}
	}

	@Override
	protected void validateForm() {
		LOGGER.debug("Validating user profile form");
		if (getEntity() == null) {
			throw new IllegalStateException("User data cannot be null");
		}
		// Validate profile information
		if (!binder.isValid()) {
			throw new IllegalArgumentException("Please fill all required fields correctly");
		}
		// Validate password change if fields are not empty
		final String currentPassword = currentPasswordField.getValue();
		final String newPassword = newPasswordField.getValue();
		final String confirmPassword = confirmPasswordField.getValue();
		if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
			validatePasswordChange(currentPassword, newPassword, confirmPassword);
		}
		LOGGER.debug("Form validation completed successfully");
	}

	/** Validates password change fields. */
	private void validatePasswordChange(final String currentPassword, final String newPassword, final String confirmPassword) {
		LOGGER.debug("Validating password change");
		if (currentPassword.isEmpty()) {
			throw new IllegalArgumentException("Current password is required to change password");
		}
		if (newPassword.isEmpty()) {
			throw new IllegalArgumentException("New password cannot be empty");
		}
		if (newPassword.length() < 6) {
			throw new IllegalArgumentException("New password must be at least 6 characters long");
		}
		if (!newPassword.equals(confirmPassword)) {
			throw new IllegalArgumentException("New password and confirmation do not match");
		}
		// Verify current password
		if (!passwordEncoder.matches(currentPassword, getEntity().getPassword())) {
			throw new IllegalArgumentException("Current password is incorrect");
		}
		LOGGER.debug("Password validation completed successfully");
	}
}
