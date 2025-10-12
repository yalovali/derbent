package tech.derbent.api.roles.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.utils.Check;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;

public class CUserProjectRoleInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Project Role Details";
	private static final Class<?> ENTITY_CLASS = CUserProjectRole.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectRoleInitializerService.class);

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, ENTITY_CLASS);
			// Basic role information
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "project"));
			// Role classification and permissions
			detailSection.addScreenLine(CDetailLinesService.createSection("Role Classification"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "isAdmin"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "isUser"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "isGuest"));
			// Display configuration
			detailSection.addScreenLine(CDetailLinesService.createSection("Display Configuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "color"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "sortOrder"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "attributeNonDeletable"));
			// Audit and assignment details
			detailSection.addScreenLine(CDetailLinesService.createSection("Assignments"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "assignedTo"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "lastModifiedDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "isActive"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "id"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating user project role view.");
			throw new RuntimeException("Failed to create user project role view", e);
		}
	}

	public static CGridEntity createGridEntity(final CProject project, boolean attributeNone) {
		CGridEntity grid = createBaseGridEntity(project, ENTITY_CLASS);
		// hide grid actions when needed
		grid.setAttributeNone(attributeNone);
		grid.setSelectedFields("id,name,description,project,isAdmin,isUser,isGuest,color,sortOrder,isActive,attributeNonDeletable");
		return grid;
	}

	public static void initialize(CProject project, CGridEntityService gridEntityService, CDetailSectionService detailSectionService,
			CPageEntityService pageEntityService, boolean showInQuickToolbar) throws Exception {
		Check.notNull(project, "project cannot be null");
		Check.notNull(gridEntityService, "gridEntityService cannot be null");
		Check.notNull(detailSectionService, "detailSectionService cannot be null");
		Check.notNull(pageEntityService, "pageEntityService cannot be null");
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project, false);
		initBase(ENTITY_CLASS, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, "Setup.Project Roles",
				"Project Role Management", "Manage project-specific roles and permissions", showInQuickToolbar);
	}
}
