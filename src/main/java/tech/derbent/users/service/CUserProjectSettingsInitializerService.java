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
import tech.derbent.users.domain.CUserProjectSettings;

/** Initializes default UI configuration for {@link CUserProjectSettings}. */
public final class CUserProjectSettingsInitializerService extends CInitializerServiceBase {

        public static final String BASE_PANEL_NAME = "Project Membership";
        private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectSettingsInitializerService.class);
        private static final Class<?> ENTITY_CLASS = CUserProjectSettings.class;

        private CUserProjectSettingsInitializerService() {}

        public static CDetailSection createBasicView(final CProject project) {
                Check.notNull(project, "project cannot be null");
                try {
                        final CDetailSection detailSection = createBaseScreenEntity(project, ENTITY_CLASS);
                        detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "user"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "project"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "role"));
                        detailSection.addScreenLine(CDetailLinesService.createSection("Permissions"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "permission"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "isActive"));
                        detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "createdDate"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "lastModifiedDate"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "id"));
                        detailSection.debug_printScreenInformation();
                        return detailSection;
                } catch (final Exception e) {
                        LOGGER.error("Error creating user project settings view: {}", e.getMessage(), e);
                        throw new RuntimeException("Failed to create user project settings view", e);
                }
        }

        public static CGridEntity createGridEntity(final CProject project) {
                final CGridEntity grid = createBaseGridEntity(project, ENTITY_CLASS);
                grid.setSelectedFields("id,user,project,role,permission,isActive");
                return grid;
        }

        public static void initialize(final CProject project, final CGridEntityService gridEntityService,
                        final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService,
                        final boolean showInQuickToolbar) throws Exception {
                Check.notNull(project, "project cannot be null");
                final CDetailSection detailSection = createBasicView(project);
                final CGridEntity grid = createGridEntity(project);
                initBase(ENTITY_CLASS, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
                                "Relations.Project Memberships", "Project Membership Management",
                                "Manage user assignments and permissions for projects", showInQuickToolbar);
        }
}

