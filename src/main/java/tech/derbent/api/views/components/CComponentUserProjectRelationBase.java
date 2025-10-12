package tech.derbent.api.views.components;

import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.grid.Grid;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.session.service.ISessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserTypeService;

/** Generic base class for User-Project relationship components. This class provides common functionality for both User->Project and Project->User
 * relationship components, reducing code duplication while maintaining flexibility for specific implementations.
 * @param <MasterClass>     The main entity type (CUser for user-centric, CProject for project-centric)
 * @param <RelationalClass> The relationship entity type (always CUserProjectSettings) */
public abstract class CComponentUserProjectRelationBase<MasterClass extends CEntityNamed<MasterClass>,
		RelationalClass extends CEntityDB<RelationalClass>> extends CComponentRelationPanelBase<MasterClass, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	protected final CCompanyService companyService;
	protected final CProjectService projectService;
	protected final CUserProjectSettingsService userProjectSettingsService;
	protected final CUserTypeService userTypeService;

	public CComponentUserProjectRelationBase(final String title, final Class<MasterClass> entityClass,
			final CAbstractService<MasterClass> entityService, ISessionService sessionService, final ApplicationContext applicationContext) {
		super(title, entityClass, CUserProjectSettings.class, entityService, applicationContext.getBean(CUserProjectSettingsService.class),
				sessionService, applicationContext);
		this.userProjectSettingsService = applicationContext.getBean(CUserProjectSettingsService.class);
		this.projectService = applicationContext.getBean(CProjectService.class);
		this.userTypeService = applicationContext.getBean(CUserTypeService.class);
		this.companyService = applicationContext.getBean(CCompanyService.class);
	}

	@Override
	protected void deleteRelation(CUserProjectSettings selected) throws Exception {
		userProjectSettingsService.deleteByUserProject(selected.getUser(), selected.getProject());
	}

	@Override
	protected String getDeleteConfirmationMessage(final CUserProjectSettings selected) {
		Check.notNull(selected, "Selected settings cannot be null");
		Check.notNull(selected.getProject(), "Project cannot be null");
		final String projectName = selected.getProject().getName();
		return String.format("Are you sure you want to delete the project setting for '%s'? This action cannot be undone.", projectName);
	}

	@Override
	protected String getDisplayText(final CUserProjectSettings settings, final String type) {
		Check.notNull(settings, "Settings cannot be null when getting display text");
		try {
			switch (type) {
			case "project":
				Check.notNull(settings.getProject(), "Project cannot be null");
				return CColorUtils.getDisplayTextFromEntity(settings.getProject());
			case "user":
				Check.notNull(settings.getUser(), "User cannot be null");
				return CColorUtils.getDisplayTextFromEntity(settings.getUser());
			case "role":
				return settings.getRole() != null ? CColorUtils.getDisplayTextFromEntity(settings.getRole()) : "";
			case "permission":
				return settings.getPermission() != null ? settings.getPermission() : "";
			default:
				return "";
			}
		} catch (Exception e) {
			LOGGER.error("Failed to get display text for type {}: {}", type, e.getMessage());
			return "";
		}
	}

	protected boolean isUserMaster() {
		// return true is MasterClass is CUser
		return CUser.class.equals(getEntityClass());
	}

	/** Abstract methods that subclasses must implement */
	@Override
	protected void onSettingsSaved(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when saving");
		LOGGER.debug("Saving user project settings: {}", settings);
		try {
			final CUserProjectSettings savedSettings = settings.getId() == null ? userProjectSettingsService.addUserToProject(settings.getUser(),
					settings.getProject(), settings.getRole(), settings.getPermission()) : userProjectSettingsService.save(settings);
			LOGGER.info("Successfully saved user project settings: {}", savedSettings);
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Error saving user project settings.");
			throw e;
		}
	}

	@Override
	protected abstract void openAddDialog() throws Exception;
	@Override
	protected abstract void openEditDialog() throws Exception;
	/** Abstract method for setting up data accessors - subclasses provide specific implementations */
	@Override
	protected abstract void setupDataAccessors();

	/** Sets up the grid with enhanced visual styling including colors, avatars and consistent headers. Uses entity decorations with colors and icons
	 * for better visual representation. */
	@Override
	protected void setupGrid(final Grid<CUserProjectSettings> grid) {
		try {
			super.setupGrid(grid);
			if (isUserMaster()) {
				// User-centric: User->Project
				grid.addComponentColumn(settings -> {
					try {
						return CColorUtils.getEntityWithIcon(settings.getProject());
					} catch (Exception e) {
						LOGGER.error("Failed to create project component.");
						return new com.vaadin.flow.component.html.Span(getDisplayText(settings, "project"));
					}
				}).setHeader(createStyledHeader("Project", "#2E7D32")).setAutoWidth(true).setSortable(true);
			} else {
				// Project-centric: Project->User
				grid.addComponentColumn(settings -> {
					try {
						return CColorUtils.getEntityWithIcon(settings.getUser());
					} catch (Exception e) {
						LOGGER.error("Failed to create user component.");
						return new com.vaadin.flow.component.html.Span(getDisplayText(settings, "user"));
					}
				}).setHeader(createStyledHeader("User", "#1565C0")).setAutoWidth(true).setSortable(true);
			}
			grid.addColumn(settings -> getDisplayText(settings, "role")).setHeader(createStyledHeader("Role", "#F57F17")).setAutoWidth(true)
					.setSortable(true);
			grid.addColumn(settings -> getDisplayText(settings, "permission")).setHeader(createStyledHeader("Permissions", "#8E24AA"))
					.setAutoWidth(true).setSortable(true);
		} catch (Exception e) {
			LOGGER.error("Failed to setup grid.");
			throw new RuntimeException("Failed to setup grid", e);
		}
	}
}
