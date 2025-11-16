package tech.derbent.app.roles.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.roles.domain.CUserProjectRole;

public class CUserProjectRoleInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Project Role Details";
	private static final Class<?> ENTITY_CLASS = CUserProjectRole.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectRoleInitializerService.class);
	private static final String menuTitle = MenuTitle_ROLES + ".User Project Roles";
	private static final String pageTitle = "User Project Role Management";
	private static final String pageDescription = "Manage project-specific roles and permissions";
	private static final String menuOrder = Menu_Order_ROLES + ".1";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
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
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "active"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "id"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating user project role view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project, final boolean attributeNone) {
		final CGridEntity grid = createBaseGridEntity(project, ENTITY_CLASS);
		// hide grid actions when needed
		grid.setAttributeNone(attributeNone);
		grid.setColumnFields(List.of("id", "name", "description", "project", "isAdmin", "isUser", "isGuest", "color", "sortOrder", "active",
				"attributeNonDeletable"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project, false);
		initBase(ENTITY_CLASS, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
		// Project role data: [name, description, isAdmin, isUser, isGuest]
		final String[][] roleData = {
				{
						"Project Admin", "Administrative role with full project access", "true", "false", "false"
				}, {
						"Project User", "Standard user role with regular access", "false", "true", "false"
				}, {
						"Project Guest", "Guest role with limited access", "false", "false", "true"
				}
		};
		// Use consumer pattern to set role-specific fields
		final int[] index = {
				0
		}; // Mutable counter for array access in lambda
		initializeProjectEntity(roleData,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(ENTITY_CLASS)), project, minimal, item -> {
					final CUserProjectRole role = (CUserProjectRole) item;
					final String[] data = roleData[index[0]++];
					role.setIsAdmin(Boolean.parseBoolean(data[2]));
					role.setIsUser(Boolean.parseBoolean(data[3]));
					role.setIsGuest(Boolean.parseBoolean(data[4]));
					role.setColor(CColorUtils.getRandomColor(true));
				});
	}
}
