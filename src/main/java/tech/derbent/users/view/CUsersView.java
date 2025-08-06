package tech.derbent.users.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CAbstractNamedEntityPage;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;
import tech.derbent.users.service.CUserProjectSettingsService;

@Route ("users/:user_id?/:action?(edit)")
@PageTitle ("User Master Detail")
@Menu (
	order = 3.2, icon = "class:tech.derbent.users.view.CUsersView",
	title = "Settings.Users"
)
@PermitAll // When security is enabled, allow all authenticated users
public class CUsersView extends CAbstractNamedEntityPage<CUser>
	implements CInterfaceIconSet {

	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CUser.getIconColorCode(); // Use the static method from CUser
	}

	public static String getIconFilename() { return CUser.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "user_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "users/%s/edit";

	private CPanelUserProjectSettings projectSettingsGrid;

	private final CUserTypeService userTypeService;

	private final CCompanyService companyService;

	CPanelUserDescription descriptionPanel;

	private final CProjectService projectService;
	private final CUserProjectSettingsService userProjectSettingsService;

	// private final TextField name; • Annotate the CUsersView constructor with @Autowired
	// to let Spring inject dependencies.
	@Autowired
	public CUsersView(final CUserService entityService,
		final CProjectService projectService, final CUserTypeService userTypeService,
		final CCompanyService companyService, final CSessionService sessionService,
		final CUserProjectSettingsService userProjectSettingsService) {
		super(CUser.class, entityService, sessionService);
		addClassNames("users-view");
		this.userTypeService = userTypeService;
		this.companyService = companyService;
		this.projectService = projectService;
		this.userProjectSettingsService = userProjectSettingsService;
		// projectSettingsGrid = new CPanelUserProjectSettings(projectService);
		LOGGER.info("CUsersView initialized with user type and company services");
	}

	@Override
	protected void createDetailsLayout() {
		CAccordionDBEntity<CUser> panel;
		descriptionPanel = new CPanelUserDescription(getCurrentEntity(), getBinder(),
			(CUserService) entityService, userTypeService, companyService);
		addAccordionPanel(descriptionPanel);
		panel = new CPanelUserContactInfo(getCurrentEntity(), getBinder(),
			(CUserService) entityService, userTypeService, companyService);
		addAccordionPanel(panel);
		panel = new CPanelUserCompanyAssociation(getCurrentEntity(), getBinder(),
			(CUserService) entityService, userTypeService, companyService);
		addAccordionPanel(panel);
		// panel = new CPanelUserBasicInfo(getCurrentEntity(), getBinder(), (CUserService)
		// entityService); addAccordionPanel(panel);
		projectSettingsGrid = new CPanelUserProjectSettings(getCurrentEntity(),
			getBinder(), (CUserService) entityService, userTypeService, companyService,
			projectService, userProjectSettingsService);
		addAccordionPanel(projectSettingsGrid);
		panel = new CPanelUserSystemAccess(getCurrentEntity(), getBinder(),
			(CUserService) entityService, userTypeService, companyService);
		addAccordionPanel(panel);
		/**************/
		// descriptionPanel = new CPanelUserDescription(getCurrentEntity(),
		// getBinder(),(CUserService) entityService, userTypeService, companyService);
		// getBaseDetailsLayout().add(descriptionPanel);
		// getBaseDetailsLayout().add(projectSettingsGrid);
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
	protected void createGridForEntity() {
		LOGGER.info("Creating grid for users with appropriate field widths");
		// Add profile picture column first
		grid.addImageColumn(CUser::getProfilePictureData, "Picture");
		// Add columns using CGrid methods with field-type-appropriate widths
		grid.addShortTextColumn(CUser::getName, "Name", "name");
		grid.addShortTextColumn(CUser::getLastname, "Last Name", "lastname");
		grid.addShortTextColumn(CUser::getLogin, "Login", "login");
		grid.addLongTextColumn(CUser::getEmail, "Email", "email");
		// Status column uses lambda expression - not directly sortable at DB level
		grid.addBooleanColumn(CUser::isEnabled, "Status", "Enabled", "Disabled");
		// User type requires join - not directly sortable at DB level
		grid.addReferenceColumn(
			item -> item.getUserType() != null ? item.getUserType().getName() : "",
			"User Type");
		// Company requires join - not directly sortable at DB level
		grid.addReferenceColumn(
			item -> item.getCompany() != null ? item.getCompany().getName() : "",
			"Company");
		grid.addShortTextColumn(CUser::getRoles, "Roles", "roles");
		// Data provider is already set up in the base class
		// CAbstractMDPage.createGridLayout() No need to call grid.setItems() again as
		// it's already configured to handle sorting properly
	}

	/**
	 * Simplified save button creation with focused error handling.
	 * 
	 * Handles the complete save workflow:
	 * 1. Form validation and data binding
	 * 2. Password update processing
	 * 3. Entity persistence
	 * 4. UI updates and navigation
	 */
	@Override
	protected CButton createSaveButton(final String buttonText) {
		LOGGER.info("Creating custom save button for CUsersView");
		
		return CButton.createPrimary(buttonText, e -> {
			try {
				performSaveOperation();
			} catch (final ValidationException validationException) {
				handleValidationError();
			} catch (final Exception exception) {
				handleUnexpectedError(exception);
			}
		});
	}

	/**
	 * Performs the complete save operation workflow.
	 */
	private void performSaveOperation() throws ValidationException {
		ensureEntityExists();
		bindFormData();
		handlePasswordUpdate();
		saveEntityAndUpdateUI();
	}

	/**
	 * Ensures a current entity exists, creating one if necessary.
	 */
	private void ensureEntityExists() {
		if (getCurrentEntity() == null) {
			setCurrentEntity(entityService.createEntity());
		}
	}

	/**
	 * Binds form data to the current entity.
	 */
	private void bindFormData() throws ValidationException {
		getBinder().writeBean(getCurrentEntity());
	}

	/**
	 * Handles password update through the description panel.
	 */
	private void handlePasswordUpdate() {
		descriptionPanel.saveEventHandler();
	}

	/**
	 * Saves the entity and updates the UI accordingly.
	 */
	private void saveEntityAndUpdateUI() {
		final CUser savedEntity = entityService.save(getCurrentEntity());
		LOGGER.info("User saved successfully with ID: {}", savedEntity.getId());
		
		setCurrentEntity(savedEntity);
		updateUIAfterSave();
	}

	/**
	 * Updates UI components after successful save.
	 */
	private void updateUIAfterSave() {
		clearForm();
		refreshGrid();
		safeShowNotification("User data saved successfully");
		safeNavigateToClass();
	}

	/**
	 * Handles validation errors with user-friendly messaging.
	 */
	private void handleValidationError() {
		new CWarningDialog(
			"Failed to save the data. Please check that all required fields are filled and values are valid.")
			.open();
	}

	/**
	 * Handles unexpected errors with proper logging.
	 */
	private void handleUnexpectedError(final Exception exception) {
		LOGGER.error("Unexpected error during save operation", exception);
		new CWarningDialog("An unexpected error occurred while saving. Please try again.").open();
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	/**
	 * Simplified form population with clear separation of concerns.
	 * 
	 * Updates both the main form and project settings panel when user selection changes.
	 * Handles lazy loading and data synchronization properly.
	 */
	@Override
	protected void populateForm(final CUser value) {
		super.populateForm(value);
		LOGGER.info("Populating form with user data: {}", value != null ? value.getLogin() : "null");
		
		updateProjectSettingsPanel(value);
	}

	/**
	 * Updates the project settings panel based on current user selection.
	 * 
	 * @param user The selected user, or null to clear the panel
	 */
	private void updateProjectSettingsPanel(final CUser user) {
		if (projectSettingsGrid == null) {
			LOGGER.debug("Project settings grid not yet initialized, skipping populate");
			return;
		}

		if (user != null) {
			configureProjectSettingsForUser(user);
		} else {
			clearProjectSettings();
		}
	}

	/**
	 * Configures project settings panel for the specified user.
	 * 
	 * @param user The user to configure project settings for
	 */
	private void configureProjectSettingsForUser(final CUser user) {
		final CUser userWithSettings = loadUserWithProjects(user.getId());
		projectSettingsGrid.setCurrentUser(userWithSettings);
		
		setupProjectSettingsAccessors(userWithSettings);
	}

	/**
	 * Loads user data with project settings to avoid lazy initialization issues.
	 * 
	 * @param userId The ID of the user to load
	 * @return User entity with project settings loaded
	 */
	private CUser loadUserWithProjects(final Long userId) {
		return ((CUserService) entityService).getUserWithProjects(userId);
	}

	/**
	 * Sets up accessor methods for project settings panel data management.
	 * 
	 * @param userWithSettings User entity with project settings loaded
	 */
	private void setupProjectSettingsAccessors(final CUser userWithSettings) {
		projectSettingsGrid.setProjectSettingsAccessors(
			() -> {
				// Ensure the collection is never null
				List<CUserProjectSettings> settings = userWithSettings.getProjectSettings();
				if (settings == null) {
					settings = new ArrayList<>();
					userWithSettings.setProjectSettings(settings);
				}
				return settings;
			},
			(settings) -> {
				// Instead of replacing the entire collection, update the existing one
				// This preserves JPA's collection tracking and prevents orphan removal issues
				List<CUserProjectSettings> currentSettings = userWithSettings.getProjectSettings();
				if (currentSettings == null) {
					// If null, initialize with new ArrayList and set it
					currentSettings = new ArrayList<>(settings);
					userWithSettings.setProjectSettings(currentSettings);
				} else {
					// Update existing collection in-place to preserve JPA tracking
					currentSettings.clear();
					currentSettings.addAll(settings);
				}
				entityService.save(userWithSettings);
			},
			() -> refreshUserAfterProjectUpdate(userWithSettings.getId())
		);
	}

	/**
	 * Refreshes user data after project settings update.
	 * 
	 * @param userId The ID of the user to refresh
	 */
	private void refreshUserAfterProjectUpdate(final Long userId) {
		try {
			final CUser refreshedUser = loadUserWithProjects(userId);
			populateForm(refreshedUser);
		} catch (final Exception e) {
			LOGGER.error("Error refreshing user after project settings update", e);
		}
	}

	/**
	 * Clears project settings panel when no user is selected.
	 */
	private void clearProjectSettings() {
		projectSettingsGrid.setCurrentUser(null);
		projectSettingsGrid.setProjectSettingsAccessors(
			Collections::emptyList, 
			(settings) -> {}, 
			() -> {}
		);
	}

	@Override
	protected void setupToolbar() {
		// TODO Auto-generated method stub
	}
}
