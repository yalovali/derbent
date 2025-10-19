package tech.derbent.users.service;

import java.util.List;
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
import tech.derbent.users.domain.CUserProjectSettings;

/** Initializes default UI configuration for {@link CUserProjectSettings}. */
public final class CUserProjectSettingsInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Project Membership";
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectSettingsInitializerService.class);
	private static final Class<?> ENTITY_CLASS = CUserProjectSettings.class;
	private static final String menuTitle = "Relations.Project Memberships";
	private static final String pageTitle = "Project Membership Management";
	private static final String pageDescription = "Manage user assignments and permissions for projects";
	private static final String menuOrder = "1.1";
	private static final boolean showInQuickToolbar = false;

	private static void addOptionalField(final CDetailSection detailSection, final String fieldName) {
		try {
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, fieldName));
		} catch (final NoSuchFieldException ex) {
			LOGGER.debug("Skipping optional field {} for {}", fieldName, ENTITY_CLASS.getSimpleName());
		}
	}

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		Check.notNull(project, "project cannot be null");
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, ENTITY_CLASS);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "user"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "role"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Permissions"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "permission"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "active"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			addOptionalField(detailSection, "createdDate");
			addOptionalField(detailSection, "lastModifiedDate");
			addOptionalField(detailSection, "id");
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating user project settings view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, ENTITY_CLASS);
		grid.setColumnFields(List.of("id", "user", "project", "role", "permission", "active"));
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

	private CUserProjectSettingsInitializerService() {}
}
