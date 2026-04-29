package tech.derbent.plm.agile.service;

import java.math.BigDecimal;
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
import tech.derbent.api.screens.service.CEntityNamedInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CProjectItemInitializerService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.activities.service.CActivityPriorityService;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CFeatureType;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

public class CFeatureInitializerService extends CProjectItemInitializerService {

	static final Class<?> clazz = CFeature.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CFeatureInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".12";
	private static final String menuTitle = MenuTitle_PROJECT + ".Features";
	private static final String pageDescription = "Feature management for projects";
	private static final String pageTitle = "Feature Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CEntityNamedInitializerService.createBasicView(scr, clazz, project, true);
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
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			CParentRelationInitializerService.addDefaultSection(scr, clazz, project);
			CParentRelationInitializerService.addDefaultChildrenSection(scr, clazz, project);
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating feature view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "componentWidget", "entityType", "assignedTo", "createdBy",
				"startDate", "dueDate", "completionDate", "progressPercentage", "estimatedHours", "actualHours",
				"remainingHours", "status", "priority", "project", "createdDate", "lastModifiedDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
			throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
				menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder, null);
	}

	/** Initialize sample features for a project.
	 * @param project     the project to create features for
	 * @param minimal     if true, creates only 1 feature; if false, creates 2 features
	 * @param sampleEpic1 the first epic to link features to (can be null)
	 * @param sampleEpic2 the second epic to link second feature to (can be null)
	 * @return array of created features [feature1, feature2] where feature2 may be null if minimal is true */
	public static CFeature[] initializeSample(final CProject<?> project, final boolean minimal, final CEpic sampleEpic1,
			final CEpic sampleEpic2) throws Exception {
		record FeatureSeed(String name, String description, String acceptanceCriteria, String notes,
				int parentEpicIndex, int startOffsetDays, int durationDays, int storyPoints, int estimatedHours,
				int actualHours, int progressPercentage) {}
		final List<FeatureSeed> seeds = List.of(new FeatureSeed("Requirement Group: Core Platform",
				"Sub-requirement group under the requirements epic, focusing on core platform and architecture items.",
				"Leaf requirements exist under this feature and include activities, milestones, and deliverables.",
				"Sample sub-requirement bucket for platform enablement work.", 0, 42, 55, 13, 86, 28, 24),
				new FeatureSeed("Requirement Group: Customer Delivery",
						"Sub-requirement group under the requirements epic, focusing on delivery artifacts and execution tracking.",
						"Deliverables and activities roll up to leaf requirements under this feature.",
						"Sample sub-requirement bucket for milestone-driven delivery.", 0, 35, 50, 11, 74, 22, 20),
				new FeatureSeed("MFA Enrollment and Recovery",
						"Enable users to enroll multi-factor authentication and recover access without manual support intervention.",
						"Enrollment, recovery codes, and admin override flow are validated in staging.",
						"Anchors the identity modernization epic.", 0, 45, 70, 21, 120, 55, 45),
				new FeatureSeed("Session Security and Audit Review",
						"Provide suspicious session review, forced sign-out, and audit trail visibility for administrators.",
						"Admins can review active sessions, revoke them, and export audit evidence.",
						"Needed for security review gates and SOC2 controls.", 0, 30, 65, 18, 105, 48, 38),
				new FeatureSeed("Profile and Preferences Workspace",
						"Allow customers to manage profile details, billing contacts, and communication preferences from one workspace.",
						"Workspace updates persist immediately and show confirmation for key customer profile changes.",
						"Customer-facing UX feature tied to support-deflection goals.", 1, 25, 60, 16, 96, 40, 32),
				new FeatureSeed("Saved Searches and Dashboard Views",
						"Add reusable saved filters, dashboard widgets, and pinned views for daily customer operations.",
						"Customers can save, rename, pin, and reuse search criteria across sessions.",
						"Expected to create visible backlog breadth for workspace-focused teams.", 1, 18, 55, 14, 88,
						30, 24),
				new FeatureSeed("Invoice Dispute Triage",
						"Support invoice dispute intake, SLA tracking, and evidence gathering for finance operations.",
						"Disputes can be created, triaged, and resolved with SLA visibility and linked evidence.",
						"Shared feature across product, finance, and customer success.", 2, 10, 50, 13, 74, 24, 18),
				new FeatureSeed("Release Command Center",
						"Create release go-live checklist, risk review, and observability handoff workflows for launch teams.",
						"Release managers can verify launch criteria, monitor blockers, and record go-live decisions.",
						"Used to keep a meaningful future backlog beyond the first active sprints.", 3, 0, 45, 11, 64,
						15, 12));
		try {
			final CFeatureService featureService = CSpringContext.getBean(CFeatureService.class);
			final CEpicService epicService = CSpringContext.getBean(CEpicService.class);
			final CFeatureTypeService featureTypeService = CSpringContext.getBean(CFeatureTypeService.class);
			final CActivityPriorityService activityPriorityService =
					CSpringContext.getBean(CActivityPriorityService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
			final List<CEpic> availableEpics = epicService.listByProject(project);
			final List<CFeatureType> availableTypes = featureTypeService.listByCompany(project.getCompany());
			final List<CActivityPriority> availablePriorities =
					activityPriorityService.listByCompany(project.getCompany());
			final List<CUser> availableUsers = userService.listByCompany(project.getCompany());
			final CEpic[] parentEpics = {
					sampleEpic1, sampleEpic2
			};
			final CFeature[] createdFeatures = new CFeature[2];
			int createdCount = 0;
			int returnIndex = 0;
			for (final FeatureSeed seed : seeds) {
				final CFeatureType type = availableTypes.isEmpty() ? featureTypeService.getRandom(project.getCompany())
						: availableTypes.get(createdCount % availableTypes.size());
				final CActivityPriority priority =
						availablePriorities.isEmpty() ? activityPriorityService.getRandom(project.getCompany())
								: availablePriorities.get(createdCount % availablePriorities.size());
				final CUser user = availableUsers.isEmpty() ? userService.getRandom(project.getCompany())
						: availableUsers.get(createdCount % availableUsers.size());
				CFeature feature = new CFeature(seed.name(), project);
				feature.setDescription(seed.description());
				feature.setEntityType(type);
				feature.setPriority(priority);
				feature.setAssignedTo(user);
				feature.setAcceptanceCriteria(seed.acceptanceCriteria());
				feature.setNotes(seed.notes());
				feature.setStartDate(LocalDate.now().minusDays(seed.startOffsetDays()));
				feature.setDueDate(feature.getStartDate().plusDays(seed.durationDays()));
				feature.setStoryPoint(Long.valueOf(seed.storyPoints()));
				feature.setEstimatedHours(BigDecimal.valueOf(seed.estimatedHours()));
				feature.setActualHours(BigDecimal.valueOf(seed.actualHours()));
				feature.setRemainingHours(BigDecimal.valueOf(Math.max(seed.estimatedHours() - seed.actualHours(), 0)));
				feature.setHourlyRate(BigDecimal.valueOf(130));
				feature.setEstimatedCost(feature.getHourlyRate().multiply(feature.getEstimatedHours()));
				feature.setActualCost(feature.getHourlyRate().multiply(feature.getActualHours()));
				feature.setProgressPercentage(seed.progressPercentage());
				feature.setResults(seed.progressPercentage() >= 35 ? "Cross-team delivery slices are underway." : "");
				if (type != null && type.getWorkflow() != null) {
					final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(feature);
					if (!initialStatuses.isEmpty()) {
						feature.setStatus(initialStatuses.get(0));
					}
				}
				CEpic parentEpic =
						!availableEpics.isEmpty() ? availableEpics.get(seed.parentEpicIndex() % availableEpics.size())
								: parentEpics[Math.min(seed.parentEpicIndex(), parentEpics.length - 1)];
				if (!minimal && sampleEpic1 != null && createdCount < 2) {
					// Ensure the first two sample features form an explicit sub-requirement chain under the first epic.
					parentEpic = sampleEpic1;
				}
				if (parentEpic != null) {
					feature.setParentItem(parentEpic);
				} else if (sampleEpic1 != null) {
					// Fallback to first epic if specified parent not available
					feature.setParentItem(sampleEpic1);
				}
				feature = featureService.save(feature);
				createdCount++;
				if (returnIndex < createdFeatures.length) {
					createdFeatures[returnIndex++] = feature;
				}
				if (minimal) {
					break;
				}
			}
			LOGGER.debug("Created {} sample feature(s) for project: {}", createdCount, project.getName());
			return createdFeatures;
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample features for project: {} reason={}", project.getName(),
					e.getMessage());
			throw new RuntimeException("Failed to initialize sample features for project: " + project.getName(), e);
		}
	}
}
