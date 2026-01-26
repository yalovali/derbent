package tech.derbent.plm.agile.service;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.agileparentrelation.service.CAgileParentRelationInitializerService;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.screens.service.CInitializerServiceProjectItem;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.activities.service.CActivityPriorityService;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CEpicType;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

public class CEpicInitializerService extends CInitializerServiceProjectItem {

	static final Class<?> clazz = CEpic.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CEpicInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".10";
	private static final String menuTitle = MenuTitle_PROJECT + ".Epics";
	private static final String pageDescription = "Epic management for projects";
	private static final String pageTitle = "Epic Management";
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
			LOGGER.error("Error creating epic view.");
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

	/**
	 * Initialize sample epics for a project.
	 *
	 * @param project the project to create epics for
	 * @param minimal if true, creates only 1 epic; if false, creates 2 epics
	 * @return array of created epics [epic1, epic2] where epic2 may be null if minimal is true
	 */
	public static CEpic[] initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		try {
			final CEpicService epicService = CSpringContext.getBean(CEpicService.class);
			final CEpicTypeService epicTypeService = CSpringContext.getBean(CEpicTypeService.class);
			final CActivityPriorityService activityPriorityService = CSpringContext.getBean(CActivityPriorityService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);

			final CEpicType type1 = epicTypeService.getRandom(project.getCompany());
			final CActivityPriority priority1 = activityPriorityService.getRandom(project.getCompany());
			final CUser user1 = userService.getRandom(project.getCompany());

			CEpic sampleEpic1 = new CEpic("Customer Portal Platform", project);
			sampleEpic1.setDescription("Build comprehensive customer portal for self-service and support");
			sampleEpic1.setEntityType(type1);
			sampleEpic1.setPriority(priority1);
			sampleEpic1.setAssignedTo(user1);
			sampleEpic1.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 180)));
			sampleEpic1.setDueDate(sampleEpic1.getStartDate().plusDays((long) (Math.random() * 365)));
			if (type1 != null && type1.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(sampleEpic1);
				if (!initialStatuses.isEmpty()) {
					sampleEpic1.setStatus(initialStatuses.get(0));
				}
			}
			// Epic has no parent - it's root level
			sampleEpic1 = epicService.save(sampleEpic1);
			LOGGER.info("Created Epic '{}' (ID: {}) - ROOT LEVEL (no parent)", sampleEpic1.getName(), sampleEpic1.getId());

			if (minimal) {
				return new CEpic[] { sampleEpic1, null };
			}

			final CEpicType type2 = epicTypeService.getRandom(project.getCompany());
			final CActivityPriority priority2 = activityPriorityService.getRandom(project.getCompany());
			final CUser user2 = userService.getRandom(project.getCompany());

			CEpic sampleEpic2 = new CEpic("Mobile Application Development", project);
			sampleEpic2.setDescription("Develop iOS and Android mobile applications with full feature parity");
			sampleEpic2.setEntityType(type2);
			sampleEpic2.setPriority(priority2);
			sampleEpic2.setAssignedTo(user2);
			sampleEpic2.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 180)));
			sampleEpic2.setDueDate(sampleEpic2.getStartDate().plusDays((long) (Math.random() * 365)));
			if (type2 != null && type2.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(sampleEpic2);
				if (!initialStatuses.isEmpty()) {
					sampleEpic2.setStatus(initialStatuses.get(0));
				}
			}
			// Epic has no parent - it's root level
			sampleEpic2 = epicService.save(sampleEpic2);
			LOGGER.info("Created Epic '{}' (ID: {}) - ROOT LEVEL (no parent)", sampleEpic2.getName(), sampleEpic2.getId());

			LOGGER.debug("Created sample epics for project: {}", project.getName());
			return new CEpic[] { sampleEpic1, sampleEpic2 };
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample epics for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample epics for project: " + project.getName(), e);
		}
	}
}
