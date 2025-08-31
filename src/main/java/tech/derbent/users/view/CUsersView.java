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
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseNamed;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;
import tech.derbent.users.service.CUserViewService;

@Route ("cusersview")
@PageTitle ("Users")
@Menu (order = 3.2, icon = "class:tech.derbent.users.view.CUsersView", title = "Settings.Users")
@PermitAll // When security is enabled, allow all authenticated users
public class CUsersView extends CGridViewBaseNamed<CUser> {
	private static final long serialVersionUID = 1L;
	public static final String ENTITY_ROUTE_TEMPLATE_EDIT = "cusersview/%s/edit";

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return CUser.getIconColorCode(); // Use the static method from CUser
	}

	public static String getIconFilename() { return CUser.getIconFilename(); }

	// private PasswordField passwordField;
	private final String ENTITY_ID_FIELD = "user_id";
	private CPanelUserProjectSettings projectSettingsGrid;
	private final CUserTypeService userTypeService;
	private final CCompanyService companyService;
	private final CProjectService projectService;
	private final CUserProjectSettingsService userProjectSettingsService;

	@Autowired
	public CUsersView(final CUserService entityService, final CProjectService projectService, final CUserTypeService userTypeService,
			final CCompanyService companyService, final CSessionService sessionService, final CUserProjectSettingsService userProjectSettingsService,
			final CScreenService screenService) {
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
		grid.addShortTextColumn(CUser::getRoles, "Roles", "roles");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	/** Handles password update through the description panel. */
	private void handlePasswordUpdate() {
		final PasswordField passwordField = (PasswordField) detailsBuilder.getComponentByName(CUserViewService.BASE_PANEL_NAME, "password");
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
		/**********************/
		// final CScreen screen = screenService.findByNameAndProject(sessionService.getActiveProject().orElse(null), CUserViewService.BASE_VIEW_NAME);
		// detailsBuilder.buildDetails(screen, getBinder(), getBaseDetailsLayout());
		buildScreen(CUserViewService.BASE_VIEW_NAME);
		/**********************/
		projectSettingsGrid = new CPanelUserProjectSettings(getCurrentEntity(), getBinder(), (CUserService) entityService, userTypeService,
				companyService, projectService, userProjectSettingsService);
		addAccordionPanel(projectSettingsGrid);
	}
}
