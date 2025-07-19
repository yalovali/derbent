package tech.derbent.users.view;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

public class CUserProjectSettingsDialog extends Dialog {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private final CProjectService projectService;
	private final CUserProjectSettings settings;
	private final CUser user;
	private final Consumer<CUserProjectSettings> onSave;
	private final boolean isNew;

	// Form components
	private ComboBox<CProject> projectComboBox;
	private TextField rolesField;
	private TextField permissionsField;

	public CUserProjectSettingsDialog(
		final CProjectService projectService,
		final CUserProjectSettings settings,
		final CUser user,
		final Consumer<CUserProjectSettings> onSave
	) {
		this.projectService = projectService;
		this.settings = settings != null ? settings : new CUserProjectSettings();
		this.user = user;
		this.onSave = onSave;
		this.isNew = settings == null;

		setupDialog();
		setupForm();
		setupButtons();
		populateForm();
	}

	private void setupDialog() {
		setHeaderTitle(isNew ? "Add Project Assignment" : "Edit Project Assignment");
		setModal(true);
		setCloseOnEsc(true);
		setCloseOnOutsideClick(false);
		setWidth("500px");
	}

	private void setupForm() {
		final VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.setSpacing(true);

		final H3 title = new H3(isNew ? "Assign User to Project" : "Edit Project Assignment");
		layout.add(title);

		final FormLayout formLayout = new FormLayout();

		// Project selection
		projectComboBox = new ComboBox<>("Project");
		projectComboBox.setItemLabelGenerator(CProject::getName);
		projectComboBox.setItems(getAvailableProjects());
		projectComboBox.setRequired(true);
		projectComboBox.setEnabled(isNew); // Only allow changing project for new assignments

		// Roles field
		rolesField = new TextField("Roles");
		rolesField.setPlaceholder("Enter roles separated by commas (e.g., DEVELOPER, MANAGER)");
		rolesField.setHelperText("Comma-separated list of roles for this project");

		// Permissions field
		permissionsField = new TextField("Permissions");
		permissionsField.setPlaceholder("Enter permissions separated by commas (e.g., READ, WRITE, DELETE)");
		permissionsField.setHelperText("Comma-separated list of permissions for this project");

		formLayout.add(projectComboBox, rolesField, permissionsField);
		layout.add(formLayout);

		add(layout);
	}

	private List<CProject> getAvailableProjects() {
		// Get all projects
		List<CProject> allProjects = projectService.findAll();
		
		// If editing existing, we should include the current project
		if (!isNew && settings.getProjectId() != null) {
			projectService.get(settings.getProjectId()).ifPresent(project -> {
				if (!allProjects.contains(project)) {
					allProjects.add(project);
				}
			});
		}
		
		return allProjects;
	}

	private void setupButtons() {
		final Button saveButton = new Button("Save", e -> save());
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		final Button cancelButton = new Button("Cancel", e -> close());

		final HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
		buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);

		getFooter().add(buttonLayout);
	}

	private void populateForm() {
		if (!isNew) {
			// Populate existing settings
			if (settings.getProjectId() != null) {
				projectService.get(settings.getProjectId()).ifPresent(projectComboBox::setValue);
			}

			if (settings.getRoles() != null) {
				rolesField.setValue(String.join(", ", settings.getRoles()));
			}

			if (settings.getPermissions() != null) {
				permissionsField.setValue(String.join(", ", settings.getPermissions()));
			}
		}
	}

	private void save() {
		try {
			validateForm();

			// Set user
			settings.setUser(user);

			// Set project
			CProject selectedProject = projectComboBox.getValue();
			if (selectedProject != null) {
				settings.setProjectId(selectedProject.getId());
			}

			// Set roles
			String rolesText = rolesField.getValue();
			if (rolesText != null && !rolesText.trim().isEmpty()) {
				Set<String> roles = new HashSet<>();
				for (String role : rolesText.split(",")) {
					String trimmedRole = role.trim().toUpperCase();
					if (!trimmedRole.isEmpty()) {
						roles.add(trimmedRole);
					}
				}
				settings.setRoles(roles);
			} else {
				settings.setRoles(new HashSet<>());
			}

			// Set permissions
			String permissionsText = permissionsField.getValue();
			if (permissionsText != null && !permissionsText.trim().isEmpty()) {
				Set<String> permissions = new HashSet<>();
				for (String permission : permissionsText.split(",")) {
					String trimmedPermission = permission.trim().toUpperCase();
					if (!trimmedPermission.isEmpty()) {
						permissions.add(trimmedPermission);
					}
				}
				settings.setPermissions(permissions);
			} else {
				settings.setPermissions(new HashSet<>());
			}

			// Notify parent and close
			if (onSave != null) {
				onSave.accept(settings);
			}

			close();
			Notification.show(isNew ? "Project assignment created successfully" : "Project assignment updated successfully");

		} catch (Exception e) {
			LOGGER.error("Error saving project settings", e);
			Notification.show("Error saving project assignment: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
		}
	}

	private void validateForm() {
		if (projectComboBox.getValue() == null) {
			throw new IllegalArgumentException("Please select a project");
		}
	}
}