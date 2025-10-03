package tech.derbent.api.views.components;

import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.grid.Grid;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySetting;
import tech.derbent.users.service.CUserCompanySettingsService;

/** Base class for managing user-company relationships in both directions. This class provides common functionality for both user->company and
 * company->user panels. */
public abstract class CComponentUserCompanyBase<MasterClass extends CEntityNamed<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CComponentRelationPanelBase<MasterClass, CUserCompanySetting> {

	private static final long serialVersionUID = 1L;
	protected CUserCompanySettingsService userCompanySettingsService;

	public CComponentUserCompanyBase(final String title, final Class<MasterClass> entityClass, final CAbstractService<MasterClass> entityService,
			final ApplicationContext applicationContext) {
		super(title, entityClass, CUserCompanySetting.class, entityService, applicationContext.getBean(CUserCompanySettingsService.class),
				applicationContext);
		userCompanySettingsService = (CUserCompanySettingsService) relationService;
	}

	@Override
	protected void deleteRelation(CUserCompanySetting selected) throws Exception {
		userCompanySettingsService.deleteByUserCompany(selected.getUser(), selected.getCompany());
	}

	@Override
	protected String getDeleteConfirmationMessage(final CUserCompanySetting selected) {
		Check.notNull(selected, "Selected settings cannot be null");
		Check.notNull(selected.getCompany(), "Company cannot be null");
		final String companyName = selected.getCompany().getName();
		return String.format("Are you sure you want to delete the company setting for '%s'? This action cannot be undone.", companyName);
	}

	@Override
	protected String getDisplayText(final CUserCompanySetting settings, final String type) {
		Check.notNull(settings, "Settings cannot be null when getting display text");
		try {
			switch (type) {
			case "company":
				Check.notNull(settings.getCompany(), "Company cannot be null");
				return CColorUtils.getDisplayTextFromEntity(settings.getCompany());
			case "user":
				Check.notNull(settings.getUser(), "User cannot be null");
				return CColorUtils.getDisplayTextFromEntity(settings.getUser());
			case "role":
				return settings.getRole() != null ? CColorUtils.getDisplayTextFromEntity(settings.getRole()) : "";
			case "ownership":
				return settings.getOwnershipLevel() != null ? settings.getOwnershipLevel() : "";
			default:
				return "";
			}
		} catch (Exception e) {
			LOGGER.error("Failed to get display text for type {}: {}", type, e.getMessage(), e);
			return "";
		}
	}

	@Override
	protected void onSettingsSaved(final CUserCompanySetting settings) {
		Check.notNull(settings, "Settings cannot be null when saving");
		LOGGER.debug("Saving user company settings: {}", settings);
		try {
			final CUserCompanySetting savedSettings = settings.getId() == null ? userCompanySettingsService.addUserToCompany(settings.getUser(),
					settings.getCompany(), settings.getOwnershipLevel(), settings.getRole()) : userCompanySettingsService.save(settings);
			LOGGER.info("Successfully saved user company settings: {}", savedSettings);
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Error saving user company settings: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to save user company settings: " + e.getMessage(), e);
		}
	}

	protected boolean isUserMaster() {
		// return true is MasterClass is CUser
		return CUser.class.equals(getEntityClass());
	}

	/** Abstract method to open the add dialog
	 * @throws Exception */
	@Override
	protected abstract void openAddDialog() throws Exception;
	/** Abstract method to open the edit dialog
	 * @throws Exception */
	@Override
	protected abstract void openEditDialog() throws Exception;
	/** Abstract method for setting up data accessors - subclasses provide specific implementations */
	@Override
	protected abstract void setupDataAccessors();

	/** Sets up the grid with enhanced visual styling including colors, avatars and consistent headers. Uses entity decorations with colors and icons
	 * for better visual representation. */
	@Override
	protected void setupGrid(final Grid<CUserCompanySetting> grid) {
		super.setupGrid(grid);
		// Add columns with enhanced styling and colors
		// grid.addColumn(CUserCompanySetting::getId).setHeader(createStyledHeader("ID", "#424242")).setAutoWidth(true);
		if (isUserMaster()) {
			grid.addComponentColumn(settings -> {
				try {
					return CColorUtils.getEntityWithIcon(settings.getCompany());
				} catch (Exception e) {
					LOGGER.error("Failed to create company component: {}", e.getMessage(), e);
					return new com.vaadin.flow.component.html.Span(getDisplayText(settings, "company"));
				}
			}).setHeader(createStyledHeader("Company", "#D32F2F")).setAutoWidth(true).setSortable(true);
		} else {
			grid.addComponentColumn(settings -> {
				try {
					return CColorUtils.getEntityWithIcon(settings.getUser());
				} catch (Exception e) {
					LOGGER.error("Failed to create user component: {}", e.getMessage(), e);
					return new com.vaadin.flow.component.html.Span(getDisplayText(settings, "user"));
				}
			}).setHeader(createStyledHeader("User", "#1565C0")).setAutoWidth(true);
		}
		grid.addComponentColumn(settings -> {
			try {
				return settings.getRole() != null ? CColorUtils.getEntityWithIcon(settings.getRole()) : new com.vaadin.flow.component.html.Span("");
			} catch (Exception e) {
				LOGGER.error("Failed to create role component: {}", e.getMessage(), e);
				return new com.vaadin.flow.component.html.Span(getDisplayText(settings, "role"));
			}
		}).setHeader(createStyledHeader("Role", "#F57F17")).setAutoWidth(true);
		grid.addColumn(settings -> getDisplayText(settings, "ownership")).setHeader(createStyledHeader("Ownership", "#8E24AA")).setAutoWidth(true);
		// Apply consistent grid styling
	}
}
