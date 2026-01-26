package tech.derbent.plm.agile.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.agileparentrelation.service.CAgileParentRelationInitializerService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.screens.service.CInitializerServiceProjectItem;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

public class CUserStoryInitializerService extends CInitializerServiceProjectItem {

	static final Class<?> clazz = CUserStory.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserStoryInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".11";
	private static final String menuTitle = MenuTitle_PROJECT + ".User Stories";
	private static final String pageDescription = "User story management for projects";
	private static final String pageTitle = "User Story Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
			scr.addScreenLine(CDetailLinesService.createSection("System Access"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CDetailLinesService.createSection("Schedule"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "startDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dueDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "completionDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "progressPercentage"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "estimatedHours"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actualHours"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "remainingHours"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priority"));
			scr.addScreenLine(CDetailLinesService.createSection("Financials"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "estimatedCost"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actualCost"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "hourlyRate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "acceptanceCriteria"));
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addDefaultSection(scr, clazz);
			// Links section - standard section for entities that can be linked
			CLinkInitializerService.addDefaultSection(scr, clazz);
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(scr, clazz);
			scr.addScreenLine(CDetailLinesService.createSection("Additional Information"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "notes"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "results"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentId"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			CAgileParentRelationInitializerService.addDefaultSection(scr, clazz, project);
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating user story view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "componentWidget", "entityType", "assignedTo", "createdBy", "startDate", "dueDate",
				"completionDate", "progressPercentage", "estimatedHours", "actualHours", "remainingHours", "status", "priority", "project",
				"createdDate", "lastModifiedDate"));
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
