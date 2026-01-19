package tech.derbent.plm.decisions.service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.plm.decisions.domain.CDecision;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;

public class CDecisionInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CDecision.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".3";
	private static final String menuTitle = MenuTitle_PROJECT + ".Decisions";
	private static final String pageDescription = "Decision tracking and accountability";
	private static final String pageTitle = "Decision Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			// create screen lines
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Schedule"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "reviewDate"));
   
			detailSection.addScreenLine(CDetailLinesService.createSection("Associations"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "estimatedCost"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "implementationDate"));
   
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);
   
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addCommentsSection(detailSection, clazz);
   
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating decision view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "project", "entityType", "status", "assignedTo", "createdBy",
				"createdDate", "implementationDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}
}
