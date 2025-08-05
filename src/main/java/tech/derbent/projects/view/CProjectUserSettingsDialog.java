package tech.derbent.projects.view;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.data.domain.PageRequest;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;

import tech.derbent.abstracts.views.CDBEditDialog;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserService;

/**
 * Dialog for adding users to a project (reverse direction). Inherits generic dialog logic
 * from CDBEditDialog.
 */
public class CProjectUserSettingsDialog extends CDBEditDialog<CUserProjectSettings> {

	private static final long serialVersionUID = 1L;

	private final CProjectService projectService;
	private final CUserService userService;
	private final CProject project;

	// Form components
	private ComboBox<CUser> userComboBox;
	private TextField rolesField;
	private TextField permissionsField;

	public CProjectUserSettingsDialog(final CProjectService projectService,
		final CUserService userService, // Added userService parameter
		final CUserProjectSettings settings, final CProject project,
		final Consumer<CUserProjectSettings> onSave) {
		// Call parent constructor with provided settings or new instance if null
		super(settings != null ? settings : new CUserProjectSettings(), onSave,
			settings == null);
		this.projectService = projectService;
		this.userService = userService; // Store injected userService
		this.project = project;
		setupDialog();
		populateForm();
	}

	/** Returns available users for selection. */
	private List<CUser> getAvailableUsers() {
		try {
			// Use the injected userService instance
			final PageRequest pageable = PageRequest.of(0, 1000);
			return userService.list(pageable);
		} catch (final Exception e) {
			LOGGER.warn("Error retrieving users", e);
			return java.util.Collections.emptyList();
		}
	}

	@Override
	protected Icon getFormIcon() { return VaadinIcon.USERS.create(); }

	@Override
	protected String getFormTitle() {
		return isNew ? "Add User to Project" : "Edit User Project Assignment";
	}

	@Override
	public String getHeaderTitle() {
		return isNew ? "Add User to Project" : "Edit User Assignment";
	}

	@Override
	protected String getSuccessCreateMessage() {
		return "User added to project successfully";
	}

	@Override
	protected String getSuccessUpdateMessage() {
		return "User project assignment updated successfully";
	}

	/** Populates form fields from data. */
	@Override
	protected void populateForm() {
		LOGGER.debug("Populating form for {}", getClass().getSimpleName());

		if ((projectService == null) || (project == null)) {
			throw new IllegalStateException(
				"ProjectService and Project must be initialized before populating form");
		}
		// User selection
		userComboBox = new ComboBox<>("User");
		userComboBox.setAllowCustomValue(false);
		userComboBox.setItemLabelGenerator(user -> user.getName() + " "
			+ (user.getLastname() != null ? user.getLastname() : "") + " ("
			+ user.getLogin() + ")");
		userComboBox.setItems(getAvailableUsers());
		userComboBox.setRequired(true);
		userComboBox.setEnabled(isNew); // Only allow changing user when creating new
										// assignment
		// Roles field
		rolesField = new TextField("Roles");
		rolesField
			.setPlaceholder("Enter roles separated by commas (e.g., DEVELOPER, MANAGER)");
		rolesField.setHelperText("Comma-separated list of roles for this project");
		rolesField.setRequired(true);
		// Permissions field
		permissionsField = new TextField("Permissions");
		permissionsField.setPlaceholder(
			"Enter permissions separated by commas (e.g., READ, WRITE, DELETE)");
		permissionsField
			.setHelperText("Comma-separated list of permissions for this project");
		permissionsField.setRequired(true);
		formLayout.add(userComboBox, rolesField, permissionsField);

		if (!isNew) {

			if (data.getUser() != null) {
				userComboBox.setValue(data.getUser());
			}

			if (data.getRole() != null) {
				rolesField.setValue(data.getRole());
			}

			if (data.getPermission() != null) {
				permissionsField.setValue(data.getPermission());
			}
		}
	}

	/** Validates form fields. Throws exception if invalid. */
	@Override
	protected void validateForm() {

		if (userComboBox.getValue() == null) {
			throw new IllegalArgumentException("Please select a user");
		}
		// Validate role field
		final String role = rolesField.getValue();

		if ((role == null) || role.trim().isEmpty()) {
			throw new IllegalArgumentException("Role is required and cannot be empty");
		}
		// Validate permission field
		final String permission = permissionsField.getValue();

		if ((permission == null) || permission.trim().isEmpty()) {
			throw new IllegalArgumentException(
				"Permission is required and cannot be empty");
		}
		// Set project and user
		data.setProject(project);
		final CUser selectedUser = userComboBox.getValue();

		if (selectedUser != null) {
			data.setUser(selectedUser);
		}
		data.setRole(role.trim());
		data.setPermission(permission.trim());
	}
}