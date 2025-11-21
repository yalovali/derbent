package tech.derbent.app.comments.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

public final class CCommentInitializerService extends CInitializerServiceBase {

        public static final String BASE_PANEL_NAME = "Comment Details";
        private static final Class<?> clazz = CComment.class;
        private static final Logger LOGGER = LoggerFactory.getLogger(CCommentInitializerService.class);
        private static final String menuOrder = Menu_Order_PROJECT + ".6";
        private static final String menuTitle = MenuTitle_PROJECT + ".Comments";
        private static final String pageDescription = "Manage comments captured on project activities";
        private static final String pageTitle = "Comment Management";
        private static final boolean showInQuickToolbar = false;

        public static CDetailSection createBasicView(final CProject project) throws Exception {
                Check.notNull(project, "project cannot be null");
                try {
                        final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
                        detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "commentText"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "activity"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priority"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "important"));
                        detailSection.addScreenLine(CDetailLinesService.createSectionEnd());
                        detailSection.addScreenLine(CDetailLinesService.createSection("Author & Timing"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "author"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "eventDate"));
                        detailSection.addScreenLine(CDetailLinesService.createSectionEnd());
                        detailSection.addScreenLine(CDetailLinesService.createSection("System"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
                        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id"));
                        detailSection.addScreenLine(CDetailLinesService.createSectionEnd());
                        detailSection.debug_printScreenInformation();
                        return detailSection;
                } catch (final Exception e) {
                        LOGGER.error("Error creating comment view.");
                        throw e;
                }
        }

        public static CGridEntity createGridEntity(final CProject project) {
                final CGridEntity grid = createBaseGridEntity(project, clazz);
                grid.setColumnFields(List.of("id", "commentText", "activity", "priority", "important", "author", "eventDate", "active"));
                return grid;
        }

        public static void initialize(final CProject project, final CGridEntityService gridEntityService,
                        final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
                final CDetailSection detailSection = createBasicView(project);
                final CGridEntity grid = createGridEntity(project);
                initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
                                pageDescription, showInQuickToolbar, menuOrder);
        }

        private CCommentInitializerService() {}
}
