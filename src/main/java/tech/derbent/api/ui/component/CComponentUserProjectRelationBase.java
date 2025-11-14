package tech.derbent.api.ui.component;

import com.vaadin.flow.component.grid.Grid;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.service.CCompanyService;
import tech.derbent.app.projects.service.CProjectService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.domain.CUserProjectSettings;
import tech.derbent.base.users.service.CUserProjectSettingsService;

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

	public CComponentUserProjectRelationBase(final String title, final Class<MasterClass> entityClass,
			final CAbstractService<MasterClass> entityService, ISessionService sessionService) {
		super(title, entityClass, CUserProjectSettings.class, entityService,
				CSpringContext.<CUserProjectSettingsService>getBean(CUserProjectSettingsService.class), sessionService);
		userProjectSettingsService = CSpringContext.<CUserProjectSettingsService>getBean(CUserProjectSettingsService.class);
		projectService = CSpringContext.<CProjectService>getBean(CProjectService.class);
		companyService = CSpringContext.<CCompanyService>getBean(CCompanyService.class);
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
		try {
			Check.notNull(settings, "Settings cannot be null when saving");
			LOGGER.debug("Saving user project settings: {}", settings);
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
			LOGGER.debug("Setting up grid for User-Project relationship component.");
			if (isUserMaster()) {
				// User-centric: User->Project
				grid.addComponentColumn(settings -> {
					try {
						return CColorUtils.getEntityWithIcon(settings.getProject());
					} catch (Exception e) {
						LOGGER.error("Failed to create project component.");
						return new com.vaadin.flow.component.html.Span(getDisplayText(settings, "project"));
					}
				}).setHeader(CColorUtils.createStyledHeader("Project", "#2E7D32")).setAutoWidth(true).setSortable(true);
			} else {
				// Project-centric: Project->User
				grid.addComponentColumn(settings -> {
					try {
						return CColorUtils.getEntityWithIcon(settings.getUser());
					} catch (Exception e) {
						LOGGER.error("Failed to create user component.");
						return new com.vaadin.flow.component.html.Span(getDisplayText(settings, "user"));
					}
				}).setHeader(CColorUtils.createStyledHeader("User", "#1565C0")).setAutoWidth(true).setSortable(true);
			}
			grid.addColumn(settings -> getDisplayText(settings, "role")).setHeader(CColorUtils.createStyledHeader("Role", "#F57F17"))
					.setAutoWidth(true).setSortable(true);
			grid.addColumn(settings -> getDisplayText(settings, "permission")).setHeader(CColorUtils.createStyledHeader("Permissions", "#8E24AA"))
					.setAutoWidth(true).setSortable(true);
		} catch (Exception e) {
			LOGGER.error("Failed to setup grid.");
			throw e;
		}
	}
}
