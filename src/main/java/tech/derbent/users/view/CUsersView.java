package tech.derbent.users.view;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.services.CDetailsBuilder;
import tech.derbent.abstracts.utils.CPanelDetails;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CAbstractNamedEntityPage;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;
import tech.derbent.users.service.CUserViewService;

@Route ("cusersview/:user_id?/:action?(edit)")
@PageTitle ("User Master Detail")
@Menu (
	order = 3.2, icon = "class:tech.derbent.users.view.CUsersView",
	title = "Settings.Users"
)
@PermitAll // When security is enabled, allow all authenticated users
public class CUsersView extends CAbstractNamedEntityPage<CUser>
	implements CInterfaceIconSet {

	private static final long serialVersionUID = 1L;

	public static final String ENTITY_ROUTE_TEMPLATE_EDIT = "cusersview/%s/edit";

	public static String getIconColorCode() {
		return CUser.getIconColorCode(); // Use the static method from CUser
	}

	public static String getIconFilename() { return CUser.getIconFilename(); }

	private PasswordField passwordField;

	private final String ENTITY_ID_FIELD = "user_id";

	private CPanelUserProjectSettings projectSettingsGrid;

	private final CUserTypeService userTypeService;

	private final CCompanyService companyService;

	private final CProjectService projectService;

	private final CScreenService screenService;

	private final CUserProjectSettingsService userProjectSettingsService;

	// private final TextField name; • Annotate the CUsersView constructor with @Autowired
	// to let Spring inject dependencies.
	@Autowired
	public CUsersView(final CScreenService screenService,
		final CUserService entityService, final CProjectService projectService,
		final CUserTypeService userTypeService, final CCompanyService companyService,
		final CSessionService sessionService,
		final CUserProjectSettingsService userProjectSettingsService) {
		super(CUser.class, entityService, sessionService);
		addClassNames("users-view");
		this.userTypeService = userTypeService;
		this.companyService = companyService;
		this.projectService = projectService;
		this.screenService = screenService;
		this.userProjectSettingsService = userProjectSettingsService;
		// projectSettingsGrid = new CPanelUserProjectSettings(projectService);
	}

	@Override
	protected void createDetailsLayout() throws Exception {
		/**********************/
		final CDetailsBuilder detailsBuilder = new CDetailsBuilder();
		final CScreen screen = screenService.findByNameAndProject(
			sessionService.getActiveProject().orElse(null),
			CUserViewService.USER_VIEW_NAME);
		detailsBuilder.buildDetails(screen, getBinder(), getBaseDetailsLayout());
		/**********************/
		projectSettingsGrid = new CPanelUserProjectSettings(getCurrentEntity(),
			getBinder(), (CUserService) entityService, userTypeService, companyService,
			projectService, userProjectSettingsService);
		addAccordionPanel(projectSettingsGrid);
		/**********************/
		// Add the new screen demo panel below existing panels addAccordionPanel(panel);
		final CPanelDetails panel =
			detailsBuilder.getSectionPanel(CUserViewService.BASE_PANEL_NAME);
		Check.notNull(panel, "Panel for CUserViewService.BASE_PANEL_NAME cannot be null");
		// Add password field for editing
		passwordField = new PasswordField("Password");
		passwordField.setPlaceholder("Enter new password (leave empty to keep current)");
		passwordField.setWidthFull();
		passwordField.setHelperText("Password will be encrypted when saved");
		panel.addToContent(passwordField);
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
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	/**
	 * Handles password update through the description panel.
	 */
	private void handlePasswordUpdate() {

		// Handle password update if a new password was entered
		if ((passwordField != null) && !passwordField.isEmpty()) {
			final String newPassword = passwordField.getValue();

			if ((getCurrentEntity().getLogin() != null)
				&& !getCurrentEntity().getLogin().isEmpty()) {
				((CUserService) entityService)
					.updatePassword(getCurrentEntity().getLogin(), newPassword);
				LOGGER.info("Password updated for user: {}",
					getCurrentEntity().getLogin());
			}
		}
	}

	@Override
	protected boolean onBeforeSaveEvent() {
		LOGGER.info("onBeforeSaveEvent called for CUsersView");

		if (super.onBeforeSaveEvent() == false) {
			return false; // If the base class validation fails, do not proceed
		}
		handlePasswordUpdate();
		return true; // Default implementation allows save
	}

	/**
	 * Simplified form population with clear separation of concerns. Updates both the main
	 * form and project settings panel when user selection changes. Handles lazy loading
	 * and data synchronization properly.
	 */
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
			final Supplier<List<CUserProjectSettings>> supplier =
				() -> user.getProjectSettings();
			final Runnable runnable = () -> {
				final CUser refreshedProject = entityService.getById(user.getId()).get();
				populateForm(refreshedProject);
			};
			//
			projectSettingsGrid.setAccessors(supplier, runnable);
		}
		else {
			projectSettingsGrid.setCurrentUser(value);
			final Supplier<List<CUserProjectSettings>> supplier =
				() -> Collections.emptyList();
			final Runnable runnable = () -> {
				// Do nothing
			};
			projectSettingsGrid.setAccessors(supplier, runnable);
		}
	}

	@Override
	protected void setupToolbar() {
		// TODO Auto-generated method stub
	}
}
