package tech.derbent.users.view;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CVerticalLayout;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

/** Reusable component for editing user project settings. This component provides form fields for project assignment, role selection, and permission
 * settings. It can be bound to a CEnhancedBinder and integrated with various forms and dialogs throughout the application. Features: - Project
 * selection ComboBox with color-aware display - Role selection ComboBox with proper data provider - Permission text field for custom permissions -
 * Full binding support with CEnhancedBinder - Proper validation and error handling - Notification support through CNotificationService */
public class CUserProjectSettingsComponent extends CVerticalLayout {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectSettingsComponent.class);
	private static final long serialVersionUID = 1L;
	// Form fields
	private ComboBox<CProject> projectComboBox;
	private ComboBox<CUserProjectRole> roleComboBox;
	private TextField permissionField;
	// Services
	@Autowired
	private CNotificationService notificationService;
	private final CProjectService projectService;
	private final CUserProjectRoleService roleService;
	// Binder for data binding
	private CEnhancedBinder<CUserProjectSettings> binder;
	// Current data
	private CUserProjectSettings currentSettings;
	private CUser currentUser;
	// Callbacks
	private Consumer<CUserProjectSettings> onSave;
	private Runnable onCancel;

	/** Creates a new user project settings component.
	 * @param projectService the project service for loading available projects
	 * @param roleService    the role service for loading available roles */
	public CUserProjectSettingsComponent(CProjectService projectService, CUserProjectRoleService roleService) {
		super(false, true, false); // no padding, with spacing, no margin
		Check.notNull(projectService, "Project service cannot be null");
		Check.notNull(roleService, "Role service cannot be null");
		this.projectService = projectService;
		this.roleService = roleService;
		initializeComponent();
		createFields();
		setupBinder();
		LOGGER.debug("Created CUserProjectSettingsComponent with project and role services");
	}

	/** Binds this component to the provided binder for external form integration.
	 * @param externalBinder the external binder to use */
	public void bindToExternalBinder(CEnhancedBinder<CUserProjectSettings> externalBinder) {
		Check.notNull(externalBinder, "External binder cannot be null");
		this.binder = externalBinder;
		bindFields();
		LOGGER.debug("Component bound to external binder");
	}

	/** Populates the component with user project settings data.
	 * @param user     the user to configure settings for
	 * @param settings the existing settings (null for new settings) */
	public void populateData(CUser user, CUserProjectSettings settings) {
		Check.notNull(user, "User cannot be null");
		this.currentUser = user;
		this.currentSettings = settings;
		try {
			// Load available projects for the user
			loadProjectData();
			// Load available roles
			loadRoleData();
			// Populate fields if we have existing settings
			if (settings != null && binder != null) {
				binder.readBean(settings);
				LOGGER.debug("Populated component with existing settings for user: {}", user.getName());
			} else {
				// For new settings, set the user
				if (settings == null) {
					currentSettings = new CUserProjectSettings();
					currentSettings.setUser(user);
				}
				LOGGER.debug("Component prepared for new settings for user: {}", user.getName());
			}
		} catch (Exception e) {
			LOGGER.error("Error populating data for user {}: {}", user != null ? user.getName() : "null", e.getMessage(), e);
			if (notificationService != null) {
				notificationService.showError("Failed to load project settings data: " + e.getMessage());
			}
		}
	}

	/** Gets the current settings from the form fields.
	 * @return the current settings with form data, or null if validation fails */
	public CUserProjectSettings getCurrentSettings() {
		if (binder != null && currentSettings != null) {
			try {
				binder.writeBean(currentSettings);
				return currentSettings;
			} catch (Exception e) {
				LOGGER.error("Error getting current settings: {}", e.getMessage(), e);
				if (notificationService != null) {
					notificationService.showError("Validation failed: " + e.getMessage());
				}
				return null;
			}
		}
		return currentSettings;
	}

	/** Sets callbacks for save and cancel operations.
	 * @param onSave   callback when save is requested
	 * @param onCancel callback when cancel is requested */
	public void setCallbacks(Consumer<CUserProjectSettings> onSave, Runnable onCancel) {
		this.onSave = onSave;
		this.onCancel = onCancel;
	}

	/** Validates the current form data.
	 * @return true if validation passes, false otherwise */
	public boolean validate() {
		if (binder != null) {
			return binder.validate().isOk();
		}
		return true;
	}

	/** Clears all form fields. */
	public void clear() {
		if (projectComboBox != null)
			projectComboBox.clear();
		if (roleComboBox != null)
			roleComboBox.clear();
		if (permissionField != null)
			permissionField.clear();
		currentSettings = null;
		LOGGER.debug("Component fields cleared");
	}

	/** Gets the project ComboBox component for external access.
	 * @return the project ComboBox */
	public ComboBox<CProject> getProjectComboBox() { return projectComboBox; }

	/** Gets the role ComboBox component for external access.
	 * @return the role ComboBox */
	public ComboBox<CUserProjectRole> getRoleComboBox() { return roleComboBox; }

	/** Gets the permission field component for external access.
	 * @return the permission field */
	public TextField getPermissionField() { return permissionField; }

	private void initializeComponent() {
		setClassName("user-project-settings-component");
		setWidthFull();
		setSpacing(true);
	}

	private void createFields() {
		// Project selection ComboBox
		projectComboBox = new ComboBox<>("Project");
		projectComboBox.setItemLabelGenerator(project -> CColorUtils.getDisplayTextFromEntity(project));
		projectComboBox.setWidthFull();
		projectComboBox.setRequired(true);
		projectComboBox.setPlaceholder("Select a project");
		CAuxillaries.setId(projectComboBox);
		// Role selection ComboBox
		roleComboBox = new ComboBox<>("Role");
		roleComboBox.setItemLabelGenerator(role -> CColorUtils.getDisplayTextFromEntity(role));
		roleComboBox.setWidthFull();
		roleComboBox.setPlaceholder("Select a role");
		CAuxillaries.setId(roleComboBox);
		// Permission text field
		permissionField = new TextField("Permissions");
		permissionField.setWidthFull();
		permissionField.setPlaceholder("Enter custom permissions (optional)");
		permissionField.setHelperText("Comma-separated permissions like: READ,WRITE,DELETE");
		CAuxillaries.setId(permissionField);
		// Add fields to layout
		add(projectComboBox, roleComboBox, permissionField);
		LOGGER.debug("Created form fields for user project settings component");
	}

	private void setupBinder() {
		// Create internal binder if none provided externally
		if (binder == null) {
			binder = new CEnhancedBinder<>(CUserProjectSettings.class);
		}
		bindFields();
	}

	private void bindFields() {
		if (binder != null) {
			try {
				// Bind project field
				binder.forField(projectComboBox).asRequired("Project is required").bind(CUserProjectSettings::getProject,
						CUserProjectSettings::setProject);
				// Bind role field (optional)
				binder.bind(roleComboBox, CUserProjectSettings::getRole, CUserProjectSettings::setRole);
				// Bind permission field (optional)
				binder.bind(permissionField, CUserProjectSettings::getPermission, CUserProjectSettings::setPermission);
				LOGGER.debug("Successfully bound all fields to CUserProjectSettings");
			} catch (Exception e) {
				LOGGER.error("Error binding fields: {}", e.getMessage(), e);
				if (notificationService != null) {
					notificationService.showError("Failed to setup form binding: " + e.getMessage());
				}
			}
		}
	}

	private void loadProjectData() {
		try {
			List<CProject> availableProjects;
			if (currentUser != null) {
				// Load projects available for the current user
				availableProjects = projectService.getAvailableProjectsForUser(currentUser.getId());
			} else {
				// Load all projects if no specific user context
				availableProjects = projectService.findAll();
			}
			projectComboBox.setItems(availableProjects);
			LOGGER.debug("Loaded {} available projects", availableProjects.size());
		} catch (Exception e) {
			LOGGER.error("Error loading project data: {}", e.getMessage(), e);
			if (notificationService != null) {
				notificationService.showError("Failed to load projects: " + e.getMessage());
			}
		}
	}

	private void loadRoleData() {
		try {
			List<CUserProjectRole> availableRoles = roleService.findAll();
			roleComboBox.setItems(availableRoles);
			LOGGER.debug("Loaded {} available roles", availableRoles.size());
		} catch (Exception e) {
			LOGGER.error("Error loading role data: {}", e.getMessage(), e);
			if (notificationService != null) {
				notificationService.showError("Failed to load roles: " + e.getMessage());
			}
		}
	}
}
