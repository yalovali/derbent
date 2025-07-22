package tech.derbent.users.view;

import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.textfield.TextField;

import tech.derbent.abstracts.views.CDBEditDialog;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

/**
 * Dialog for assigning a user to a project. Inherits generic dialog logic from
 * CDialog.
 */
public class CUserProjectSettingsDialog extends CDBEditDialog<CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	private final CProjectService projectService;
	private final CUser user;
	// Form components
	private ComboBox<CProject> projectComboBox;
	private TextField rolesField;
	private TextField permissionsField;

	public CUserProjectSettingsDialog(final CProjectService projectService, final CUserProjectSettings settings, final CUser user, final Consumer<CUserProjectSettings> onSave) {
		// Call parent constructor with provided settings or new instance if null Use
		// new CUserProjectSettings() if settings is null to ensure non-null data This
		// allows the dialog to handle both new assignments and edits without requiring
		// a separate constructor for new assignments.
		super(settings != null ? settings : new CUserProjectSettings(), onSave, settings == null);
		this.projectService = projectService;
		this.user = user;
		setupDialog();// call setupDialog() to initialize the dialog
		populateForm(); // Call after fields are initialized
	}

	/** Returns available projects for selection. */
	private List<CProject> getAvailableProjects() {
		final List<CProject> allProjects = projectService.findAll();
		if (!isNew && (data.getProjectId() != null)) {
			projectService.get(data.getProjectId()).ifPresent(project -> {
				if (!allProjects.contains(project)) {
					allProjects.add(project);
				}
			});
		}
		return allProjects;
	}

	@Override
	protected Icon getFormIcon() { // TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getFormTitle() { return isNew ? "Assign User to Project" : "Edit Project Assignment"; }

	@Override
	public String getHeaderTitle() { return isNew ? "Add Project Assignment" : "Edit Project Assignment"; }

	@Override
	protected String getSuccessCreateMessage() { return "Project assignment created successfully"; }

	@Override
	protected String getSuccessUpdateMessage() { return "Project assignment updated successfully"; }

	/** Populates form fields from data. */
	@Override
	protected void populateForm() {
		LOGGER.debug("Populating form for {}", getClass().getSimpleName());
		if ((projectService == null) || (user == null)) {
			throw new IllegalStateException("ProjectService and User must be initialized before populating form");
		}
		// Project selection
		projectComboBox = new ComboBox<>("Project");
		projectComboBox.setItemLabelGenerator(CProject::getName);
		projectComboBox.setItems(getAvailableProjects());
		projectComboBox.setRequired(true);
		projectComboBox.setEnabled(isNew);
		// Roles field
		rolesField = new TextField("Roles");
		rolesField.setPlaceholder("Enter roles separated by commas (e.g., DEVELOPER, MANAGER)");
		rolesField.setHelperText("Comma-separated list of roles for this project");
		// Permissions field
		permissionsField = new TextField("Permissions");
		permissionsField.setPlaceholder("Enter permissions separated by commas (e.g., READ, WRITE, DELETE)");
		permissionsField.setHelperText("Comma-separated list of permissions for this project");
		formLayout.add(projectComboBox, rolesField, permissionsField);
		if (!isNew) {
			if (data.getProjectId() != null) {
				projectService.get(data.getProjectId()).ifPresent(projectComboBox::setValue);
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
		if (projectComboBox.getValue() == null) {
			throw new IllegalArgumentException("Please select a project");
		}
		// Set user and project
		data.setUser(user);
		final CProject selectedProject = projectComboBox.getValue();
		if (selectedProject != null) {
			data.setProjectId(selectedProject.getId());
		}
		data.setRole(rolesField.getValue());
		data.setPermission(permissionsField.getValue());
	}
}
