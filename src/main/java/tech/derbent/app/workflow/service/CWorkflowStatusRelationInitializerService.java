package tech.derbent.app.workflow.service;

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
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;

/** Initializes default UI configuration for {@link CWorkflowStatusRelation}. */
public final class CWorkflowStatusRelationInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Workflow Status Transitions";
	private static final Class<?> ENTITY_CLASS = CWorkflowStatusRelation.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CWorkflowStatusRelationInitializerService.class);
	private static final String menuOrder = "1.2";
	private static final String menuTitle = "Relations.Workflow Status Transitions";
	private static final String pageDescription = "Manage workflow status transitions and role permissions";
	private static final String pageTitle = "Workflow Status Transition Management";
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
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "workflowentity"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "fromStatus"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "toStatus"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "role"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "active"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			addOptionalField(detailSection, "createdDate");
			addOptionalField(detailSection, "lastModifiedDate");
			addOptionalField(detailSection, "id");
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating workflow status relation view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, ENTITY_CLASS);
		grid.setColumnFields(List.of("id", "workflowentity", "fromStatus", "toStatus", "role", "active"));
		return grid;
	}

        public static void initialize(final CProject project, final CGridEntityService gridEntityService,
                        final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
                        throws Exception {
                Check.notNull(project, "project cannot be null");
                final CDetailSection detailSection = createBasicView(project);
                final CGridEntity grid = createGridEntity(project);
                initBase(ENTITY_CLASS, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
                                pageDescription, showInQuickToolbar, menuOrder);
        }

	private CWorkflowStatusRelationInitializerService() {}
}
