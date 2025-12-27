package tech.derbent.app.gannt.ganntitem.service;

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
import tech.derbent.app.gannt.ganntitem.domain.CGanntItem;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

public class CGanntItemInitializerService extends CInitializerServiceBase {

        private static final Class<?> clazz = CGanntItem.class;
        private static final Logger LOGGER = LoggerFactory.getLogger(CGanntItemInitializerService.class);
        private static final String menuOrder = Menu_Order_PROJECT + ".96";
        private static final String menuTitle = MenuTitle_PROJECT + ".Gantt Items";
        private static final String pageDescription = "Project items prepared for Gantt timelines";
        private static final String pageTitle = "Gantt Items";
        private static final boolean showInQuickToolbar = false;

        public static CDetailSection createBasicView(final CProject project) throws Exception {
                try {
                        final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
                        detailSection.debug_printScreenInformation();
                        return detailSection;
                } catch (final Exception e) {
                        LOGGER.error("Error creating Gantt item view.");
                        throw e;
                }
        }

        public static CGridEntity createGridEntity(final CProject project) {
                final CGridEntity grid = createBaseGridEntity(project, clazz);
                grid.setColumnFields(List.of("id", "description", "project", "createdDate", "lastModifiedDate"));
                return grid;
        }

        public static void initialize(final CProject project, final CGridEntityService gridEntityService,
                        final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
                final CDetailSection detailSection = createBasicView(project);
                final CGridEntity grid = createGridEntity(project);
                initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
                                pageDescription, showInQuickToolbar, menuOrder);
        }

        public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
                final String[][] sampleItems = { { "Gantt Placeholder", "Synthetic Gantt entry" } };
                initializeProjectEntity(sampleItems,
                                (CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
                                null);
        }
}
