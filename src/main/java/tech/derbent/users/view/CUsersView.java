package tech.derbent.users.view;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

@Route ("users/:user_id?/:action?(edit)")
@PageTitle ("User Master Detail")
@Menu (order = 3.2, icon = "vaadin:users", title = "Settings.Users")
@PermitAll // When security is enabled, allow all authenticated users
public class CUsersView extends CAbstractMDPage<CUser> {

	private static final long serialVersionUID = 1L;

	private final String ENTITY_ID_FIELD = "user_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "users/%s/edit";

	private CPanelUserProjectSettings projectSettingsGrid;

	private final CUserTypeService userTypeService;

	private final CCompanyService companyService;

	CPanelUserDescription descriptionPanel;

	private final CProjectService projectService;

	// private final TextField name; • Annotate the CUsersView constructor with @Autowired
	// to let Spring inject dependencies.
	@Autowired
	public CUsersView(final CUserService entityService,
		final CProjectService projectService, final CUserTypeService userTypeService,
		final CCompanyService companyService) {
		super(CUser.class, entityService);
		addClassNames("users-view");
		this.userTypeService = userTypeService;
		this.companyService = companyService;
		this.projectService = projectService;
		// projectSettingsGrid = new CPanelUserProjectSettings(projectService);
		LOGGER.info("CUsersView initialized with user type and company services");
	}

	@Override
	protected void createDetailsLayout() {
		CAccordionDescription<CUser> panel;
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
			projectService);
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
		// Add columns using CGrid methods with field-type-appropriate widths
		grid.addShortTextColumn(CUser::getName, "Name", "name");
		grid.addShortTextColumn(CUser::getLastname, "Last Name", "lastname");
		grid.addShortTextColumn(CUser::getLogin, "Login", "login");
		grid.addLongTextColumn(CUser::getEmail, "Email", "email");
		// Status column uses lambda expression - not directly sortable at DB level
		grid.addBooleanColumn(CUser::isEnabled, "Status", "Enabled", "Disabled");
		// User type requires join - not directly sortable at DB level
		grid.addReferenceColumn(
			user -> user.getUserType() != null ? user.getUserType().getName() : "",
			"User Type");
		// Company requires join - not directly sortable at DB level
		grid.addReferenceColumn(
			user -> user.getCompany() != null ? user.getCompany().getName() : "",
			"Company");
		grid.addShortTextColumn(CUser::getRoles, "Roles", "roles");
		// Data provider is already set up in the base class
		// CAbstractMDPage.createGridLayout() No need to call grid.setItems() again as
		// it's already configured to handle sorting properly
	}

	@Override
	protected CButton createSaveButton(final String buttonText) {
		LOGGER.info("Creating custom save button for CUsersView");
		final tech.derbent.abstracts.views.CButton save =
			tech.derbent.abstracts.views.CButton.createPrimary(buttonText, e -> {

				try {

					if (getCurrentEntity() == null) {
						// why dont you use populateForm(
						setCurrentEntity(newEntity());
					}
					getBinder().writeBean(getCurrentEntity());
					// Handle password update if a new password was entered
					descriptionPanel.saveEventHandler();
					entityService.save(getCurrentEntity());
					clearForm();
					refreshGrid();
					Notification.show("Data updated");
					// Navigate back to the current view (list mode)
					UI.getCurrent().navigate(getClass());
				} catch (final ValidationException validationException) {
					new CWarningDialog(
						"Failed to save the data. Please check that all required fields are filled and values are valid.")
						.open();
				} catch (final Exception exception) {
					LOGGER.error("Unexpected error during save operation", exception);
					new CWarningDialog(
						"An unexpected error occurred while saving. Please try again.")
						.open();
				}
			});
		return save;
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected CUser newEntity() {
		return new CUser();
	}

	@Override
	protected void populateForm(final CUser value) {
		super.populateForm(value);
		LOGGER.info("Populating form with user data: {}",
			value != null ? value.getLogin() : "null");
		// Clear the description panel descriptionPanel.populateForm(value);

		// Update the project settings grid when a user is selected
		if (value != null) {
			// Load user with project settings to avoid lazy initialization issues
			final CUser userWithSettings =
				((CUserService) entityService).getUserWithProjects(value.getId());
			projectSettingsGrid.setCurrentUser(userWithSettings);
			projectSettingsGrid.setProjectSettingsAccessors(
				() -> userWithSettings.getProjectSettings() != null
					? userWithSettings.getProjectSettings()
					: java.util.Collections.emptyList(),
				(settings) -> {
					userWithSettings.setProjectSettings(settings);
					// Save the user when project settings are updated
					entityService.save(userWithSettings);
				}, () -> {

					// Refresh the current entity after save
					try {
						final CUser refreshedUser = ((CUserService) entityService)
							.getUserWithProjects(userWithSettings.getId());
						populateForm(refreshedUser);
					} catch (final Exception e) {
						LOGGER.error(
							"Error refreshing user after project settings update", e);
					}
				});
		}
		else {
			projectSettingsGrid.setCurrentUser(null);
			projectSettingsGrid.setProjectSettingsAccessors(
				() -> java.util.Collections.emptyList(), (settings) -> {}, () -> {});
		}
	}

	@Override
	protected void setupToolbar() {
		// TODO Auto-generated method stub
	}
}
