package tech.derbent.plm.requirements.requirement.service;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.parentrelation.service.CParentRelationInitializerService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CEntityOfProjectInitializerService;
import tech.derbent.api.screens.service.CProjectItemInitializerService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;
import tech.derbent.plm.requirements.requirement.domain.CRequirement;
import tech.derbent.plm.requirements.requirementtype.domain.CRequirementType;
import tech.derbent.plm.requirements.requirementtype.service.CRequirementTypeService;

public class CRequirementInitializerService extends CProjectItemInitializerService {

	private static final Class<?> clazz = CRequirement.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CRequirementInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".6";
	private static final String menuTitle = MenuTitle_PROJECT + ".Requirements";
	private static final String pageDescription = "Requirement management with generic hierarchy support";
	private static final String pageTitle = "Requirement Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection detailSection = CEntityOfProjectInitializerService.createBasicView(project, clazz, true);
		detailSection.addScreenLine(CDetailLinesService.createSection("Planning"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "startDate"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dueDate"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
		detailSection.addScreenLine(CDetailLinesService.createSection("Definition"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "acceptanceCriteria"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "source"));
		CAttachmentInitializerService.addDefaultSection(detailSection, clazz);
		CLinkInitializerService.addDefaultSection(detailSection, clazz);
		CCommentInitializerService.addDefaultSection(detailSection, clazz);
		CParentRelationInitializerService.addDefaultSection(detailSection, clazz, project);
		CParentRelationInitializerService.addDefaultChildrenSection(detailSection, clazz, project);
		return detailSection;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "entityType", "assignedTo", "startDate", "dueDate", "status", "project", "createdDate"));
		grid.setEditableColumnFields(List.of("name", "assignedTo", "startDate", "dueDate", "status"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder, null);
	}

	public static CRequirement[] initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		record RequirementSeed(String name, String description, String source, String acceptanceCriteria, int typeIndex, int startOffsetDays,
				int durationDays, Integer parentIndex) {}
		final List<RequirementSeed> seeds = List.of(
				new RequirementSeed("Customer Workspace Reliability", "Top-level reliability theme for customer-facing workspace experiences",
						"Portfolio Steering Committee", "Reliability work is grouped under a shared objective and measurable outcomes.", 0, 28, 50,
						null),
				new RequirementSeed("Authentication Resilience Capability", "Capability requirement for stronger session and identity handling",
						"Security Architecture Review",
						"Capability definition covers MFA enrollment, suspicious session revocation, and recovery options.", 1, 24, 35, 0),
				new RequirementSeed("Allow self-service MFA enrollment",
						"Detailed requirement for enabling workspace admins to enroll MFA without operator support",
						"Enterprise customer request #4821",
						"Workspace admins can enroll MFA, receive recovery codes, and verify setup in a single guided flow.", 2, 21, 14, 1),
				new RequirementSeed("Support audit-backed session revocation",
						"Detailed requirement for revoking suspicious sessions with traceable operator actions", "Security operations backlog",
						"Analysts can revoke a session, notify the user, and review the resulting audit event.", 2, 18, 10, 1));
		try {
			final CRequirementService requirementService = CSpringContext.getBean(CRequirementService.class);
			final CRequirementTypeService requirementTypeService = CSpringContext.getBean(CRequirementTypeService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
			final List<CRequirementType> types = requirementTypeService.listByCompany(project.getCompany());
			final List<CUser> users = userService.listByCompany(project.getCompany());
			final CRequirement[] createdRequirements = new CRequirement[2];
			final java.util.ArrayList<CRequirement> savedRequirements = new java.util.ArrayList<>();
			int createdCount = 0;
			for (final RequirementSeed seed : seeds) {
				final CRequirement requirement = new CRequirement(seed.name(), project);
				requirement.setDescription(seed.description());
				requirement.setSource(seed.source());
				requirement.setAcceptanceCriteria(seed.acceptanceCriteria());
				requirement.setStartDate(LocalDate.now().minusDays(seed.startOffsetDays()));
				requirement.setDueDate(requirement.getStartDate().plusDays(seed.durationDays()));
				requirement.setEntityType(types.get(Math.min(seed.typeIndex(), types.size() - 1)));
				if (!users.isEmpty()) {
					requirement.setAssignedTo(users.get(createdCount % users.size()));
				}
				final List<CProjectItemStatus> statuses = statusService.getValidNextStatuses(requirement);
				if (!statuses.isEmpty()) {
					requirement.setStatus(statuses.get(0));
				}
				if (seed.parentIndex() != null && seed.parentIndex() < savedRequirements.size()) {
					requirement.setParentItem(savedRequirements.get(seed.parentIndex()));
				}
				final CRequirement savedRequirement = requirementService.save(requirement);
				savedRequirements.add(savedRequirement);
				if (createdCount < createdRequirements.length) {
					createdRequirements[createdCount] = savedRequirement;
				}
				createdCount++;
				if (minimal) {
					break;
				}
			}
			return createdRequirements;
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample requirements for project: {} reason={}", project.getName(), e.getMessage());
			throw e;
		}
	}
}
