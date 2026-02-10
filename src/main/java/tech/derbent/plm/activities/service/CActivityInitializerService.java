package tech.derbent.plm.activities.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.screens.service.CInitializerServiceProjectItem;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.activities.domain.CActivityType;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.decisions.domain.CDecision;
import tech.derbent.plm.decisions.service.CDecisionService;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.service.CLinkInitializerService;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.meetings.service.CMeetingService;

public class CActivityInitializerService extends CInitializerServiceProjectItem {

	static final Class<?> clazz = CActivity.class;
	static Map<String, EntityFieldInfo> fields;
	static EntityFieldInfo info;
	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".2";
	private static final String menuTitle = MenuTitle_PROJECT + ".Activities";
	private static final String pageDescription = "Activity management for projects";
	private static final String pageTitle = "Activity Management";
	private static final boolean showInQuickToolbar = true;

	/** Add relationships (comments, attachments, links) to sample activities. */
	private static void addRelationshipsToActivities(final List<CActivity> activities, final CProject<?> project) {
		try {
			final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
			// Add comments to first activity
			if (activities.size() > 0) {
				final CActivity activity1 = activities.get(0);
				final List<CComment> comments = CCommentInitializerService.createSampleComments(new String[] {
						"Started implementation of login UI components", "Need to review accessibility requirements for form fields"
				}, new boolean[] {
						false, true
				} // Second comment is important
				);
				activity1.getComments().addAll(comments);
				activityService.save(activity1);
			}
			// Add attachments to second activity
			if (activities.size() > 1) {
				final CActivity activity2 = activities.get(1);
				final List<CAttachment> attachments = CAttachmentInitializerService.createSampleAttachments(new String[][] {
						{
								"API_Design_Spec.pdf", "API design specification for authentication endpoints", "245760"
						}, {
								"Auth_Sequence_Diagram.png", "UML sequence diagram for authentication flow", "89340"
						}
				}, project.getCompany());
				activity2.getAttachments().addAll(attachments);
				activityService.save(activity2);
			}
			// Add links to random related entities
			if (activities.size() > 0) {
				final CActivity activity = activities.get(0);
				// Link to random meeting
				final CLink linkToMeeting = CLinkInitializerService.createRandomLink(activity, project, CMeeting.class, CMeetingService.class,
						"Discussed In", "Activity discussed in planning meeting", project.getCompany());
				if (linkToMeeting != null) {
					activity.getLinks().add(linkToMeeting);
				}
				// Link to random decision
				final CLink linkToDecision = CLinkInitializerService.createRandomLink(activity, project,
						CDecision.class, CDecisionService.class, "Implements", "Activity implements strategic decision", project.getCompany());
				if (linkToDecision != null) {
					activity.getLinks().add(linkToDecision);
				}
				activityService.save(activity);
			}
			LOGGER.info("Added relationships (comments, attachments, links) to {} activities", activities.size());
		} catch (final Exception e) {
			LOGGER.warn("Error adding relationships to activities: {}", e.getMessage(), e);
			// Don't fail the whole initialization if relationships fail
		}
	}

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
			/******************/
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addDefaultSection(scr, clazz);
			/******************/
			// Links section - standard section for entities that can be linked
			CLinkInitializerService.addDefaultSection(scr, clazz);
			/******************/
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(scr, clazz);
			/******************/
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
			LOGGER.error("Error creating activity view.");
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

	/** Initialize sample activities for a project with relationships (comments, attachments, links).
	 * @param project          the project to create activities for
	 * @param minimal          if true, creates only 1 activity; if false, creates 3 activities
	 * @param sampleUserStory1 the first user story to link activities to (can be null)
	 * @param sampleUserStory2 the second user story to link third activity to (can be null) */
	public static void initializeSample(final CProject<?> project, final boolean minimal, final CUserStory sampleUserStory1,
			final CUserStory sampleUserStory2) throws Exception {
		// Seed data for sample activities with parent user story index and estimated hours
		record ActivitySeed(String name, String description, int parentUserStoryIndex, int estimatedHours) {}
		final List<ActivitySeed> seeds =
				List.of(new ActivitySeed("Implement Login Form UI", "Create responsive login form with email and password fields", 0, 8),
						new ActivitySeed("Implement Authentication API", "Create backend API for user authentication and session management", 0, 16),
						new ActivitySeed("Create Profile Edit Form", "Build form for users to update their profile information", 1, 10));
		try {
			final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
			final CActivityTypeService activityTypeService = CSpringContext.getBean(CActivityTypeService.class);
			final CActivityPriorityService activityPriorityService = CSpringContext.getBean(CActivityPriorityService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
			final CUserStory[] parentUserStories = {
					sampleUserStory1, sampleUserStory2
			};
			final List<CActivity> createdActivities = new java.util.ArrayList<>();
			// Create activities
			for (final ActivitySeed seed : seeds) {
				final CActivityType type = activityTypeService.getRandom(project.getCompany());
				final CActivityPriority priority = activityPriorityService.getRandom(project.getCompany());
				final CUser user = userService.getRandom(project.getCompany());
				final CActivity activity = new CActivity(seed.name(), project);
				activity.setDescription(seed.description());
				activity.setEntityType(type);
				activity.setPriority(priority);
				activity.setAssignedTo(user);
				activity.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 60)));
				activity.setDueDate(activity.getStartDate().plusDays((long) (Math.random() * 30)));
				activity.setEstimatedHours(java.math.BigDecimal.valueOf(seed.estimatedHours()));
				if (type != null && type.getWorkflow() != null) {
					final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(activity);
					if (!initialStatuses.isEmpty()) {
						activity.setStatus(initialStatuses.get(0));
					}
				}
				// Link Activity to UserStory parent (type-safe)
				final CUserStory parentUserStory = parentUserStories[seed.parentUserStoryIndex()];
				if (parentUserStory != null) {
					activity.setParentUserStory(parentUserStory);
				} else if (sampleUserStory1 != null) {
					// Fallback to first user story if specified parent not available
					activity.setParentUserStory(sampleUserStory1);
				}
				activityService.save(activity);
				createdActivities.add(activity);
				if (minimal) {
					break;
				}
			}
			// Add relationships: comments, attachments, links (only if not minimal)
			if (!minimal && !createdActivities.isEmpty()) {
				addRelationshipsToActivities(createdActivities, project);
			}
			// LOGGER.debug("Created {} sample activit(y|ies) for project: {} (linked to UserStories in agile hierarchy)", index, project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample activities for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample activities for project: " + project.getName(), e);
		}
	}
}
