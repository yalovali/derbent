package tech.derbent.app.gannt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;
import tech.derbent.app.gannt.domain.CGanntViewEntity;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

/** CGannttItemInitializerService - Wraps Gantt initializer logic and seeds a default project timeline view. */
public final class CGannttItemInitializerService extends CInitializerServiceBase {

        private static final Logger LOGGER = LoggerFactory.getLogger(CGannttItemInitializerService.class);
        private static final String DEFAULT_VIEW_DESCRIPTION =
                        "Automatically generated Gantt timeline that combines activities and meetings.";
        private static final String DEFAULT_VIEW_NAME = "Project Timeline";

        private CGannttItemInitializerService() {}

        public static void initialize(final CProject project, final CGridEntityService gridEntityService,
                        final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService,
                        final CGanntViewEntityService ganntViewEntityService) throws Exception {
                Check.notNull(project, "project cannot be null");
                Check.notNull(gridEntityService, "gridEntityService cannot be null");
                Check.notNull(detailSectionService, "detailSectionService cannot be null");
                Check.notNull(pageEntityService, "pageEntityService cannot be null");
                Check.notNull(ganntViewEntityService, "ganntViewEntityService cannot be null");
                LOGGER.debug("Initializing Gantt metadata and default view for project: {}", project.getName());
                CGanntInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
                ensureDefaultView(project, ganntViewEntityService);
        }

        private static void ensureDefaultView(final CProject project, final CGanntViewEntityService ganntViewEntityService) {
                ganntViewEntityService.findByNameAndProject(DEFAULT_VIEW_NAME, project).ifPresentOrElse(existing -> {
                        LOGGER.debug("Default Gantt view already exists for project {} with id {}", project.getName(),
                                        existing.getId());
                }, () -> {
                        LOGGER.info("Creating default Gantt view '{}' for project {}", DEFAULT_VIEW_NAME, project.getName());
                        final CGanntViewEntity view = ganntViewEntityService.newEntity(DEFAULT_VIEW_NAME, project);
                        view.setDescription(DEFAULT_VIEW_DESCRIPTION);
                        view.setActive(true);
                        ganntViewEntityService.save(view);
                });
        }
}
