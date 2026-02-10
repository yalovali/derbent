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
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.decisions.domain.CDecision;
import tech.derbent.plm.decisions.domain.CDecisionType;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.service.CLinkInitializerService;

public class CDecisionInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CDecision.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".3";
	private static final String menuTitle = MenuTitle_PROJECT + ".Decisions";
	private static final String pageDescription = "Decision tracking and accountability";
	private static final String pageTitle = "Decision Management";
	private static final boolean showInQuickToolbar = false;

	/** Add relationships (comments, links) to sample decisions. */
	private static void addRelationshipsToDecisions(final List<CDecision> decisions, final CDecisionService decisionService,
			final CProject<?> project) {
		try {
			// Add comments to first decision
			final CDecision decision1 = decisions.get(0);
			final List<CComment> comments1 = CCommentInitializerService.createSampleComments(new String[] {
					"This decision aligns with our digital transformation strategy", "Cost-benefit analysis shows 3x ROI within 18 months"
			}, new boolean[] {
					false, true
			} // Second comment is important
			);
			decision1.getComments().addAll(comments1);
			decisionService.save(decision1);
			LOGGER.debug("Added comments to decision: {}", decision1.getName());
			// Add comment to second decision
			final CDecision decision2 = decisions.get(1);
			final List<CComment> comments2 =
					CCommentInitializerService.createSampleComments("Team training will begin in Q1 to support this transition");
			decision2.getComments().addAll(comments2);
			// Link second decision to first decision
			final CLink link = CLinkInitializerService.createRandomLink(decision2, project, CDecision.class, CDecisionService.class, "Supports",
					"Agile methodology supports cloud-native architecture adoption", project.getCompany());
			if (link != null) {
				decision2.getLinks().add(link);
			}
			decisionService.save(decision2);
			LOGGER.debug("Added comments and link to decision: {}", decision2.getName());
			LOGGER.info("Added relationships (comments, links) to {} decisions", decisions.size());
		} catch (final Exception e) {
			LOGGER.warn("Error adding relationships to decisions: {}", e.getMessage(), e);
			// Don't fail the whole initialization if relationships fail
		}
	}

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
		grid.setColumnFields(List.of("id", "name", "description", "project", "entityType", "status", "assignedTo", "createdBy", "createdDate",
				"implementationDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	/** Initialize sample decisions for a project with relationships (comments, links).
	 * @param project the project to create decisions for
	 * @param minimal if true, creates only 1 decision; if false, creates 2 decisions */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		// Seed data for sample decisions
		record DecisionSeed(String name, String description, String estimatedCost, int implementationDays, int reviewDays) {}
		final List<DecisionSeed> seeds = List.of(
				new DecisionSeed("Adopt Cloud-Native Architecture",
						"Strategic decision to migrate to cloud-native architecture for improved scalability", "50000.00", 30, 90),
				new DecisionSeed("Implement Agile Methodology", "Operational decision to transition from waterfall to agile development methodology",
						"25000.00", 15, 60));
		try {
			final CDecisionService decisionService = CSpringContext.getBean(CDecisionService.class);
			final CDecisionTypeService decisionTypeService = CSpringContext.getBean(CDecisionTypeService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final List<CDecision> createdDecisions = new java.util.ArrayList<>();
			int index = 0;
			for (final DecisionSeed seed : seeds) {
				final CDecisionType type = decisionTypeService.getRandom(project.getCompany());
				final CProjectItemStatus status = statusService.getRandom(project.getCompany());
				final CUser user = userService.getRandom(project.getCompany());
				final CDecision decision = new CDecision(seed.name(), project);
				decision.setDescription(seed.description());
				decision.setEntityType(type);
				decision.setStatus(status);
				decision.setAssignedTo(user);
				decision.setEstimatedCost(new BigDecimal(seed.estimatedCost()));
				decision.setImplementationDate(LocalDateTime.now().plusDays(seed.implementationDays()));
				decision.setReviewDate(LocalDateTime.now().plusDays(seed.reviewDays()));
				decisionService.save(decision);
				createdDecisions.add(decision);
				index++;
				if (minimal) {
					break;
				}
			}
			// Add relationships: comments and links (only if not minimal)
			if (!minimal && createdDecisions.size() == 2) {
				addRelationshipsToDecisions(createdDecisions, decisionService, project);
			}
			LOGGER.debug("Created {} sample decision(s) for project: {}", index, project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample decisions for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample decisions for project: " + project.getName(), e);
		}
	}
}
