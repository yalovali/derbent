package tech.derbent.plm.decisions.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.decisions.domain.CDecision;
import tech.derbent.plm.decisions.domain.CDecisionType;

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
			CAttachmentInitializerService.addDefaultSection(detailSection, clazz);
   
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(detailSection, clazz);
   
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

	/**
	 * Initialize sample decisions for a project.
	 *
	 * @param project the project to create decisions for
	 * @param minimal if true, creates only 1 decision; if false, creates 2 decisions
	 */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		try {
			final CDecisionService decisionService = CSpringContext.getBean(CDecisionService.class);
			final CDecisionTypeService decisionTypeService = CSpringContext.getBean(CDecisionTypeService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);

			// Get random values from database for dependencies
			final CDecisionType type1 = decisionTypeService.getRandom(project.getCompany());
			final CProjectItemStatus status1 = statusService.getRandom(project.getCompany());
			final CUser user1 = userService.getRandom(project.getCompany());

			// Create first decision
			final CDecision decision1 = new CDecision("Adopt Cloud-Native Architecture", project);
			decision1.setDescription("Strategic decision to migrate to cloud-native architecture for improved scalability");
			decision1.setEntityType(type1);
			decision1.setStatus(status1);
			decision1.setAssignedTo(user1);
			decision1.setEstimatedCost(new BigDecimal("50000.00"));
			decision1.setImplementationDate(LocalDateTime.now().plusDays(30));
			decision1.setReviewDate(LocalDateTime.now().plusDays(90));
			decisionService.save(decision1);

			if (minimal) {
				return;
			}

			// Create second decision
			final CDecisionType type2 = decisionTypeService.getRandom(project.getCompany());
			final CProjectItemStatus status2 = statusService.getRandom(project.getCompany());
			final CUser user2 = userService.getRandom(project.getCompany());

			final CDecision decision2 = new CDecision("Implement Agile Methodology", project);
			decision2.setDescription("Operational decision to transition from waterfall to agile development methodology");
			decision2.setEntityType(type2);
			decision2.setStatus(status2);
			decision2.setAssignedTo(user2);
			decision2.setEstimatedCost(new BigDecimal("25000.00"));
			decision2.setImplementationDate(LocalDateTime.now().plusDays(15));
			decision2.setReviewDate(LocalDateTime.now().plusDays(60));
			decisionService.save(decision2);

			LOGGER.debug("Created sample decisions for project: {}", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample decisions for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample decisions for project: " + project.getName(), e);
		}
	}
}
