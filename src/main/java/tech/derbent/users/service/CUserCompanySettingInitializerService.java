package tech.derbent.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.utils.Check;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;
import tech.derbent.users.domain.CUserCompanySetting;

/** Initializes UI configuration for {@link CUserCompanySetting}. Creates default grid and detail views to manage company memberships. */
public final class CUserCompanySettingInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Company Membership";
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserCompanySettingInitializerService.class);
	private static final Class<?> ENTITY_CLASS = CUserCompanySetting.class;
	private static final String menuTitle = "Relations.Company Memberships";
	private static final String pageTitle = "Company Membership Management";
	private static final String pageDescription = "Manage user memberships and roles within companies";
	private static final String menuOrder = "1.1";
	private static final boolean showInQuickToolbar = false;

	private CUserCompanySettingInitializerService() {}

	public static CDetailSection createBasicView(final CProject project) {
		Check.notNull(project, "project cannot be null");
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, ENTITY_CLASS);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "user"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "company"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "role"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Ownership & Privileges"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "ownershipLevel"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "privileges"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "isActive"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Administration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "grantedByUserId"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			addOptionalField(detailSection, "createdDate");
			addOptionalField(detailSection, "lastModifiedDate");
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "id"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating user company setting view.");
			throw new RuntimeException("Failed to create user company setting view", e);
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, ENTITY_CLASS);
		grid.setSelectedFields("id,user,company,role,ownershipLevel,privileges,isActive,grantedByUserId");
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService, final boolean showInQuickToolbarParam)
			throws Exception {
		Check.notNull(project, "project cannot be null");
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(ENTITY_CLASS, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	private static void addOptionalField(final CDetailSection detailSection, final String fieldName) {
		try {
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, fieldName));
		} catch (final NoSuchFieldException ex) {
			LOGGER.debug("Skipping optional field {} for {}", fieldName, ENTITY_CLASS.getSimpleName());
		}
	}
}
