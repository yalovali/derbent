package tech.derbent.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;

/** CUserProjectRoleInitializer - Initializer for CUserProjectRole entities. Creates view pages and grids for user project role management. */
public class CUserProjectRoleInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Project Role Information";
	private static final Class<?> clazz = CUserProjectRole.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectRoleInitializerService.class);

	/** Create basic view for project roles.
	 * @param project the project context
	 * @return the detail section */
	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Role Type Configuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isAdmin"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isUser"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isGuest"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Display Configuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNonDeletable"));
			return detailSection;
		} catch (Exception e) {
			LOGGER.error("Error creating basic view for CUserProjectRole", e);
			throw new RuntimeException("Failed to create basic view for CUserProjectRole", e);
		}
	}

	/** Create basic grid for project roles.
	 * @param project the project context
	 * @return the grid entity */
	public static CGridEntity createBasicGrid(final CProject project) {
		try {
			final CGridEntity gridEntity = createBaseGridEntity(project, clazz);
			// Configure grid columns to show relevant role information including color
			gridEntity.setSelectedFields("id,name,description,color,sortOrder,isAdmin,isUser,isGuest,attributeNonDeletable,project");
			return gridEntity;
		} catch (Exception e) {
			LOGGER.error("Error creating basic grid for CUserProjectRole", e);
			throw new RuntimeException("Failed to create basic grid for CUserProjectRole", e);
		}
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService, final CDetailSectionService screenService,
			final CPageEntityService pageEntityService, final boolean createSampleData) {
		Check.notNull(project, "Project must not be null");
		Check.notNull(gridEntityService, "GridEntityService must not be null");
		Check.notNull(screenService, "ScreenService must not be null");
		Check.notNull(pageEntityService, "PageEntityService must not be null");
		try {
			LOGGER.info("Initializing CUserProjectRole for project: {}", project.getName());
			// Create and save basic view
			final CDetailSection detailSection = createBasicView(project);
			screenService.save(detailSection);
			// Create and save basic grid
			final CGridEntity gridEntity = createBasicGrid(project);
			gridEntityService.save(gridEntity);
			// Create and save page entity
			final CPageEntity page = createPageEntity(clazz, project, gridEntity, detailSection, "User Management.Project Roles",
					"Project Role Management", "Manage user roles within the project context", "1.1");
			page.setAttributeShowInQuickToolbar(false); // Don't show in quick toolbar by default
			pageEntityService.save(page);
			LOGGER.info("Successfully initialized CUserProjectRole for project: {}", project.getName());
		} catch (Exception e) {
			LOGGER.error("Error initializing CUserProjectRole for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize CUserProjectRole", e);
		}
	}
}
