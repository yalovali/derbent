package tech.derbent.users.view;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.InMemoryUploadCallback;
import com.vaadin.flow.server.streams.UploadMetadata;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;

import tech.derbent.abstracts.views.CButton;
import tech.derbent.abstracts.views.CDBEditDialog;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.users.domain.CUser;

/**
 * User profile dialog for editing user profile information. Extends CDBEditDialog to
 * provide consistent dialog behavior. Allows users to: - Edit display name - Change
 * password - Upload/delete profile picture
 */
public class CUserProfileDialog extends CDBEditDialog<CUser> {

	private static final long serialVersionUID = 1L;

	private static final String UPLOAD_DIR = "profile-pictures";

	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

	private final PasswordEncoder passwordEncoder;

	private TextField nameField;

	private TextField lastnameField;

	private PasswordField currentPasswordField;

	private PasswordField newPasswordField;

	private PasswordField confirmPasswordField;

	private Upload profilePictureUpload;

	private Image profilePicturePreview;

	private CButton deleteProfilePictureButton;

	private String temporaryImagePath;

	private byte[] temporaryImageData;

	private String temporaryImageFileName;

	private final Binder<CUser> binder = new Binder<>(CUser.class);

	/**
	 * Constructor for CUserProfileDialog.
	 * @param user            The user to edit
	 * @param onSave          Callback when user is saved
	 * @param passwordEncoder Password encoder for password validation and encoding
	 */
	public CUserProfileDialog(final CUser user, final Consumer<CUser> onSave,
		final PasswordEncoder passwordEncoder) {
		super(user, onSave, false); // isNew = false for profile editing
		LOGGER.info("CUserProfileDialog constructor called for user: {}",
			user != null ? user.getLogin() : "null");

		if (passwordEncoder == null) {
			throw new IllegalArgumentException("PasswordEncoder cannot be null");
		}
		this.passwordEncoder = passwordEncoder;
		setupDialog();
		populateForm();
	}

	/**
	 * Creates the password change section.
	 */
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
		passwordSection.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
			new FormLayout.ResponsiveStep("300px", 2));
		mainLayout.add(sectionTitle);
		mainLayout.add(passwordSection);
	}

	/**
	 * Creates the profile information section with name fields.
	 */
	private void createProfileInfoSection() {
		LOGGER.debug("Creating profile info section");
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
		binder.forField(nameField)
			.withValidator(
				new StringLengthValidator("Name is required", 1, CUser.MAX_LENGTH_NAME))
			.bind(CUser::getName, CUser::setName);
		binder.forField(lastnameField).withValidator(
			new StringLengthValidator("Last name is required", 1, CUser.MAX_LENGTH_NAME))
			.bind(CUser::getLastname, CUser::setLastname);
		final FormLayout profileSection = new FormLayout();
		profileSection.add(nameField, lastnameField);
		profileSection.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
		mainLayout.add(sectionTitle);
		mainLayout.add(profileSection);
	}

	/**
	 * Creates the profile picture upload section.
	 */
	private void createProfilePictureSection() {
		LOGGER.debug("Creating profile picture section");
		final Span sectionTitle = new Span("Profile Picture");
		sectionTitle.getStyle().set("font-weight", "bold");
		sectionTitle.getStyle().set("font-size", "1.1em");
		sectionTitle.getStyle().set("margin-top", "20px");
		// Profile picture preview
		profilePicturePreview = new Image();
		profilePicturePreview.setWidth("100px");
		profilePicturePreview.setHeight("100px");
		profilePicturePreview.getStyle().set("border-radius", "50%");
		profilePicturePreview.getStyle().set("object-fit", "cover");
		profilePicturePreview.getStyle().set("border",
			"2px solid var(--lumo-contrast-20pct)");
		// Set default or current profile picture (after button is created)
		// updateProfilePicturePreview(); // Move this after button creation
		// File upload component using modern API
		final InMemoryUploadCallback uploadCallback = (metadata, data) -> {
			LOGGER.info("Profile picture upload received: {} ({} bytes)", 
				metadata.fileName(), data.length);
			try {
				handleProfilePictureUpload(metadata, data);
			} catch (final IOException e) {
				LOGGER.error("Error handling profile picture upload", e);
				new CWarningDialog("Failed to upload profile picture: " + e.getMessage())
					.open();
			}
		};
		final InMemoryUploadHandler uploadHandler = new InMemoryUploadHandler(uploadCallback);
		profilePictureUpload = new Upload();
		profilePictureUpload.setUploadHandler(uploadHandler);
		profilePictureUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
		profilePictureUpload.setMaxFileSize((int) MAX_FILE_SIZE);
		profilePictureUpload.setDropLabel(new Span("Drop image here or click to upload"));
		profilePictureUpload.setUploadButton(CButton.createTertiary("Choose File"));
		profilePictureUpload.addAllFinishedListener(event -> {
			LOGGER.info("Profile picture upload completed");
		});
		profilePictureUpload.addFileRejectedListener(event -> {
			LOGGER.error("Profile picture upload rejected: {}", event.getErrorMessage());
			new CWarningDialog(
				"Failed to upload profile picture: " + event.getErrorMessage())
				.open();
		});
		// Delete button
		deleteProfilePictureButton =
			CButton.createError("Remove Picture", e -> deleteProfilePicture());
		final HorizontalLayout pictureControls = new HorizontalLayout();
		pictureControls.add(profilePictureUpload, deleteProfilePictureButton);
		pictureControls.setAlignItems(HorizontalLayout.Alignment.END);
		final VerticalLayout pictureSection = new VerticalLayout();
		pictureSection.add(profilePicturePreview, pictureControls);
		pictureSection.setHorizontalComponentAlignment(VerticalLayout.Alignment.CENTER,
			profilePicturePreview);
		pictureSection.setPadding(false);
		pictureSection.setSpacing(true);
		// Now update profile picture preview after all components are created
		updateProfilePicturePreview();
		mainLayout.add(sectionTitle);
		mainLayout.add(pictureSection);
	}

	/**
	 * Deletes the old profile picture file.
	 */
	private void deleteOldProfilePicture() {
		LOGGER.debug("Deleting old profile picture");

		if ((data != null) && (data.getProfilePicturePath() != null)) {
			final Path oldPicturePath = Paths.get(data.getProfilePicturePath());

			if (Files.exists(oldPicturePath)) {

				try {
					Files.delete(oldPicturePath);
					LOGGER.info("Deleted old profile picture: {}", oldPicturePath);
				} catch (final IOException e) {
					LOGGER.warn("Failed to delete old profile picture: {}",
						oldPicturePath, e);
				}
			}
		}
	}

	/**
	 * Deletes the current profile picture.
	 */
	private void deleteProfilePicture() {
		LOGGER.info("Deleting profile picture for user: {}",
			data != null ? data.getLogin() : "null");
		// Clear temporary path and data
		temporaryImagePath = null;
		temporaryImageData = null;
		temporaryImageFileName = null;
		// Update preview to default
		setDefaultProfilePicture();
		Notification.show("Profile picture removed", 3000,
			Notification.Position.TOP_CENTER);
	}

	/**
	 * Gets file extension from filename.
	 */
	private String getFileExtension(final String fileName) {

		if ((fileName == null) || fileName.trim().isEmpty()) {
			return "jpg"; // default
		}
		final int lastDotIndex = fileName.lastIndexOf('.');

		if ((lastDotIndex > 0) && (lastDotIndex < (fileName.length() - 1))) {
			return fileName.substring(lastDotIndex + 1).toLowerCase();
		}
		return "jpg"; // default
	}

	@Override
	protected Icon getFormIcon() { return VaadinIcon.USER.create(); }

	@Override
	protected String getFormTitle() { return "User Profile"; }

	@Override
	public String getHeaderTitle() { return "Edit Profile"; }

	@Override
	protected String getSuccessUpdateMessage() { return "Profile updated successfully"; }

	/**
	 * Handles profile picture changes on save.
	 */
	private void handleProfilePictureChange() throws IOException {
		LOGGER.debug("Handling profile picture change");

		if (temporaryImagePath != null) {
			// New picture was uploaded
			deleteOldProfilePicture();
			data.setProfilePicturePath(temporaryImagePath);
			LOGGER.info("Profile picture path updated to: {}", temporaryImagePath);
		}
		else if ((deleteProfilePictureButton.isEnabled() == false)
			&& (data.getProfilePicturePath() != null)
			&& !data.getProfilePicturePath().isEmpty()) {
			// Picture was deleted
			deleteOldProfilePicture();
			data.setProfilePicturePath(null);
			LOGGER.info("Profile picture removed for user: {}", data.getLogin());
		}
	}

	/**
	 * Handles profile picture upload using modern Vaadin Upload API.
	 */
	private void handleProfilePictureUpload(final UploadMetadata metadata,
		final byte[] data) throws IOException {
		LOGGER.info("Handling profile picture upload: {} ({} bytes)", 
			metadata.fileName(), data.length);

		if ((metadata == null) || (data == null) || (data.length == 0)) {
			throw new IOException("Invalid upload data");
		}
		
		final String fileName = metadata.fileName();
		if ((fileName == null) || fileName.trim().isEmpty()) {
			throw new IOException("Invalid file name");
		}
		
		// Create upload directory if it doesn't exist
		final Path uploadDir = Paths.get(UPLOAD_DIR);

		if (!Files.exists(uploadDir)) {
			Files.createDirectories(uploadDir);
			LOGGER.debug("Created upload directory: {}", uploadDir);
		}
		// Generate unique filename
		final String fileExtension = getFileExtension(fileName);
		final String uniqueFileName =
			"profile_" + System.currentTimeMillis() + "." + fileExtension;
		final Path targetPath = uploadDir.resolve(uniqueFileName);

		// Save uploaded file from byte array
		Files.write(targetPath, data);
		LOGGER.info("Profile picture saved to: {}", targetPath);
		
		// Update preview and store temporary path and data
		temporaryImagePath = targetPath.toString();
		temporaryImageData = data;
		temporaryImageFileName = fileName;
		profilePicturePreview.setSrc("file://" + temporaryImagePath);
		deleteProfilePictureButton.setEnabled(true);
		Notification.show("Profile picture uploaded successfully", 3000,
			Notification.Position.TOP_CENTER);
	}

	@Override
	protected void populateForm() {
		LOGGER.info("Populating form for user: {}",
			data != null ? data.getLogin() : "null");

		if (data == null) {
			LOGGER.warn("Cannot populate form - data is null");
			return;
		}
		// Bind data to form
		binder.setBean(data);
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
		LOGGER.info("Saving user profile for user: {}",
			data != null ? data.getLogin() : "null");

		try {
			validateForm();
			// Write form data to bean
			binder.writeBean(data);
			// Handle password change if needed
			final String newPassword = newPasswordField.getValue();

			if (!newPassword.isEmpty()) {
				final String encodedPassword = passwordEncoder.encode(newPassword);
				data.setPassword(encodedPassword);
				LOGGER.info("Password updated for user: {}", data.getLogin());
			}
			// Handle profile picture change
			handleProfilePictureChange();
			// Call parent save method
			super.save();
		} catch (final Exception e) {
			LOGGER.error("Error saving user profile", e);
			new CWarningDialog("Failed to save profile: " + e.getMessage()).open();
		}
	}

	/**
	 * Sets the default profile picture (user icon).
	 */
	private void setDefaultProfilePicture() {
		// Use a data URL for a default user icon
		profilePicturePreview.setSrc(
			"data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiB2aWV3Qm94PSIwIDAgMTAwIDEwMCI+PGNpcmNsZSBjeD0iNTAiIGN5PSI1MCIgcj0iNTAiIGZpbGw9IiNmNWY1ZjUiLz48Y2lyY2xlIGN4PSI1MCIgY3k9IjM1IiByPSIxNSIgZmlsbD0iIzk5OTk5OSIvPjxwYXRoIGQ9Im0yNSA3NWMwLTE0IDExLTI1IDI1LTI1czI1IDExIDI1IDI1IiBmaWxsPSIjOTk5OTk5Ii8+PC9zdmc+");
		deleteProfilePictureButton.setEnabled(false);
		LOGGER.debug("Set default profile picture");
	}

	@Override
	protected void setupContent() {
		LOGGER.debug("Setting up user profile dialog content");
		super.setupContent();
		// Create form sections
		createProfileInfoSection();
		createPasswordChangeSection();
		createProfilePictureSection();
		// Set dialog width to accommodate all sections
		setWidth("600px");
	}

	/**
	 * Updates the profile picture preview based on current user data.
	 */
	private void updateProfilePicturePreview() {
		LOGGER.debug("Updating profile picture preview for user: {}",
			data != null ? data.getLogin() : "null");

		if ((data != null) && (data.getProfilePicturePath() != null)
			&& !data.getProfilePicturePath().isEmpty()) {
			final String imagePath = data.getProfilePicturePath();

			if (Files.exists(Paths.get(imagePath))) {
				profilePicturePreview.setSrc("file://" + imagePath);
				deleteProfilePictureButton.setEnabled(true);
				LOGGER.debug("Set profile picture preview to: {}", imagePath);
			}
			else {
				setDefaultProfilePicture();
			}
		}
		else {
			setDefaultProfilePicture();
		}
	}

	@Override
	protected void validateForm() {
		LOGGER.debug("Validating user profile form");

		if (data == null) {
			throw new IllegalStateException("User data cannot be null");
		}

		// Validate profile information
		if (!binder.isValid()) {
			throw new IllegalArgumentException(
				"Please fill all required fields correctly");
		}
		// Validate password change if fields are not empty
		final String currentPassword = currentPasswordField.getValue();
		final String newPassword = newPasswordField.getValue();
		final String confirmPassword = confirmPasswordField.getValue();

		if (!currentPassword.isEmpty() || !newPassword.isEmpty()
			|| !confirmPassword.isEmpty()) {
			validatePasswordChange(currentPassword, newPassword, confirmPassword);
		}
		LOGGER.debug("Form validation completed successfully");
	}

	/**
	 * Validates password change fields.
	 */
	private void validatePasswordChange(final String currentPassword,
		final String newPassword, final String confirmPassword) {
		LOGGER.debug("Validating password change");

		if (currentPassword.isEmpty()) {
			throw new IllegalArgumentException(
				"Current password is required to change password");
		}

		if (newPassword.isEmpty()) {
			throw new IllegalArgumentException("New password cannot be empty");
		}

		if (newPassword.length() < 6) {
			throw new IllegalArgumentException(
				"New password must be at least 6 characters long");
		}

		if (!newPassword.equals(confirmPassword)) {
			throw new IllegalArgumentException(
				"New password and confirmation do not match");
		}

		// Verify current password
		if (!passwordEncoder.matches(currentPassword, data.getPassword())) {
			throw new IllegalArgumentException("Current password is incorrect");
		}
		LOGGER.debug("Password validation completed successfully");
	}
}