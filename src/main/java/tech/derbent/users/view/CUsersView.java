package tech.derbent.users.view;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.grids.CGrid;
import tech.derbent.api.views.grids.CGridViewBaseNamed;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySettings;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserInitializerService;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

@Route ("cusersview")
@PageTitle ("Users")
@Menu (order = 3.2, icon = "class:tech.derbent.users.view.CUsersView", title = "Settings.Users")
@PermitAll // When security is enabled, allow all authenticated users
public class CUsersView extends CGridViewBaseNamed<CUser> {

	public static final String DEFAULT_COLOR = "#006988";
	public static final String DEFAULT_ICON = "vaadin:archive";
	public static final String ENTITY_ROUTE_TEMPLATE_EDIT = "cusersview/%s/edit";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Users View";
	private final CCompanyService companyService;
	// private PasswordField passwordField;
	private final String ENTITY_ID_FIELD = "user_id";
	private final CProjectService projectService;
	private CPanelUserProjectSettings projectSettingsGrid;
	private final CUserProjectSettingsService userProjectSettingsService;
	private final CUserTypeService userTypeService;

	@Autowired
	public CUsersView(final CUserService entityService, final CProjectService projectService, final CUserTypeService userTypeService,
			final CCompanyService companyService, final CSessionService sessionService, final CUserProjectSettingsService userProjectSettingsService,
			final CDetailSectionService screenService) {
		super(CUser.class, entityService, sessionService, screenService);
		this.userTypeService = userTypeService;
		this.companyService = companyService;
		this.projectService = projectService;
		this.userProjectSettingsService = userProjectSettingsService;
	}

	@Override
	protected Div createDetailsTabLeftContent() {
		// Create custom tab content for users view
		final Div detailsTabLabel = new Div();
		detailsTabLabel.setText("User Details");
		detailsTabLabel.setClassName("details-tab-label");
		return detailsTabLabel;
	}

	@Override
	public void createGridForEntity(final CGrid<CUser> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumnEntityNamed(CUser::getUserType, "Type");
		grid.addColumnEntityNamed(CUser::getCompany, "Company");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addDateTimeColumn(CEntityNamed::getCreatedDate, "Created", null);
		// Add profile picture column first
		grid.addImageColumn(CUser::getProfilePictureData, "Picture");
		grid.addShortTextColumn(CUser::getLastname, "Last Name", "lastname");
		grid.addShortTextColumn(CUser::getLogin, "Login", "login");
		grid.addLongTextColumn(CUser::getEmail, "Email", "email");
		grid.addBooleanColumn(CUser::isEnabled, "Status", "Enabled", "Disabled");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	/** Handles password update through the description panel. */
	private void handlePasswordUpdate() {
		final PasswordField passwordField = (PasswordField) detailsBuilder.getComponentByName(CUserInitializerService.BASE_PANEL_NAME, "password");
		Check.notNull(passwordField, "Password field cannot be null");
		// Handle password update if a new password was entered
		if ((passwordField != null) && !passwordField.isEmpty()) {
			final String newPassword = passwordField.getValue();
			if ((getCurrentEntity().getLogin() != null) && !getCurrentEntity().getLogin().isEmpty()) {
				((CUserService) entityService).updatePassword(getCurrentEntity().getLogin(), newPassword);
				LOGGER.info("Password updated for user: {}", getCurrentEntity().getLogin());
			}
		}
	}

	@Override
	protected boolean onBeforeSaveEvent() {
		LOGGER.info("onBeforeSaveEvent called for entity: {} in ", getCurrentEntity(), this.getClass().getSimpleName());
		if (super.onBeforeSaveEvent() == false) {
			return false; // If the base class validation fails, do not proceed
		}
		handlePasswordUpdate();
		return true; // Default implementation allows save
	}

	/** Simplified form population with clear separation of concerns. Updates both the main form and project settings panel when user selection
	 * changes. Handles lazy loading and data synchronization properly. */
	@Override
	protected void populateForm(final CUser value) {
		if (value == null) {
			LOGGER.warn("User with ID {} not found, cannot populate form");
		}
		super.populateForm(value);
		if (value != null) {
			final CUser user = entityService.getById(value.getId()).orElse(null);
			// Load project with user settings to avoid lazy initialization issues
			projectSettingsGrid.setCurrentUser(user);
			// supply the project settings from eager loaded project
			final Supplier<List<CUserProjectSettings>> supplier = () -> user.getProjectSettings();
			final Runnable runnable = () -> {
				final CUser refreshedProject = entityService.getById(user.getId()).get();
				populateForm(refreshedProject);
			};
			//
			projectSettingsGrid.setAccessors(supplier, runnable);
		} else {
			projectSettingsGrid.setCurrentUser(value);
			final Supplier<List<CUserProjectSettings>> supplier = () -> Collections.emptyList();
			final Runnable runnable = () -> {
				// Do nothing
			};
			projectSettingsGrid.setAccessors(supplier, runnable);
		}
	}

	@Override
	protected void updateDetailsComponent() throws Exception {
		buildScreen(CUsersView.VIEW_NAME);
		projectSettingsGrid = new CPanelUserProjectSettings(this, getCurrentEntity(), getBinder(), (CUserService) entityService, userTypeService,
				companyService, projectService, userProjectSettingsService);
		addAccordionPanel(projectSettingsGrid);
	}

	/** Content owner method to provide available projects for the current user context. This method demonstrates context-aware data provision.
	 * @return list of available projects */
	public List<CProject> getAvailableProjects() {
		LOGGER.debug("Getting available projects for user context");
		final CUser currentUser = getCurrentEntity();
		if (currentUser == null) {
			LOGGER.debug("No current user, returning all projects");
			return projectService.findAll();
		}
		// Get all projects - could be filtered based on user's company, role, etc.
		final List<CProject> allProjects = projectService.findAll();
		LOGGER.debug("Found {} available projects for user: {}", allProjects.size(),
				currentUser.getName() != null ? currentUser.getName() : "Unknown");
		// For demonstration, return all projects
		// In a real scenario, you might filter based on user's company, permissions, etc.
		return allProjects;
	}

	/** Creates a custom component for managing user project settings. This method is referenced in the @AMetaData annotation on the projectSettings
	 * field.
	 * @return a component for managing project settings */
	public Component createUserProjectSettingsComponent() {
		try {
			LOGGER.debug("Creating custom project settings component for user: {}",
					getCurrentEntity() != null ? getCurrentEntity().getName() : "null");
			// Create a simple panel that shows existing project settings with basic CRUD operations
			final CPanelUserProjectSettings panel = new CPanelUserProjectSettings(this, getCurrentEntity(), getBinder(), (CUserService) entityService,
					userTypeService, companyService, projectService, userProjectSettingsService);
			return panel;
		} catch (Exception e) {
			LOGGER.error("Failed to create user project settings component: {}", e.getMessage(), e);
			// Fallback to a simple div with error message
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading project settings: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	/** Creates a custom component for managing user company settings. This method is referenced in the @AMetaData annotation on the companySettings
	 * field.
	 * @return a component for managing company settings */
	public Component createUserCompanySettingsComponent() {
		try {
			LOGGER.debug("Creating custom company settings component for user: {}",
					getCurrentEntity() != null ? getCurrentEntity().getName() : "null");
			// Create a simple display of company settings
			// For now, create a basic component - can be enhanced later
			final Div companyDiv = new Div();
			companyDiv.addClassName("user-company-settings");
			final CUser currentUser = getCurrentEntity();
			if (currentUser != null && currentUser.getCompanySettings() != null) {
				companyDiv.add(new Div("Company Settings (" + currentUser.getCompanySettings().size() + " companies)"));
				// Add basic information about each company
				for (CUserCompanySettings companySetting : currentUser.getCompanySettings()) {
					final Div companyItem = new Div();
					companyItem.addClassName("company-item");
					companyItem.setText(String.format("Company: %s, Role: %s, Department: %s", companySetting.getCompanyName(),
							companySetting.getRole() != null ? companySetting.getRole() : "N/A",
							companySetting.getDepartment() != null ? companySetting.getDepartment() : "N/A"));
					companyDiv.add(companyItem);
				}
			} else {
				companyDiv.add(new Div("No company settings available"));
			}
			return companyDiv;
		} catch (Exception e) {
			LOGGER.error("Failed to create user company settings component: {}", e.getMessage(), e);
			// Fallback to a simple div with error message
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading company settings: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}
}
