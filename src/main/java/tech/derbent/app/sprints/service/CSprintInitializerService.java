package tech.derbent.app.sprints.service;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.screens.service.CInitializerServiceProjectItem;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintType;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.app.attachments.service.CAttachmentInitializerService;
import tech.derbent.app.comments.service.CCommentInitializerService;

/** CSprintInitializerService - Initializer service for sprint management. Creates UI configuration and sample data for sprints. */
public class CSprintInitializerService extends CInitializerServiceProjectItem {

	static final Class<?> clazz = CSprint.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".3";
	private static final String menuTitle = MenuTitle_PROJECT + ".Sprints";
	private static final String pageDescription = "Sprint management for agile development";
	private static final String pageTitle = "Sprint Management";
	private static final boolean showInQuickToolbar = true;

	public static CGridEntity create_SprintEditingGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("componentWidget"));
		return grid;
	}

	public static CDetailSection create_SprintEditingView(final CProject project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			// CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
			// scr.addScreenLine(CDetailLinesService.createSection("Sprint Items"));
			scr.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "backlogItems"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "itemDetails"));
			scr.addScreenLine(CDetailLinesService.createSection("Items in Sprint"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sprintItems"));
			scr.addScreenLine(CDetailLinesService.createSection("Details"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "itemCount"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "startDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "endDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating sprint view.");
			throw e;
		}
	}

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
			scr.addScreenLine(CDetailLinesService.createSection("Sprint Details"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			
			// Scrum Guide 2020 - Sprint Goal & Definition of Done
			scr.addScreenLine(CDetailLinesService.createSection("Scrum Guide 2020 - Sprint Artifacts"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sprintGoal"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "definitionOfDone"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "velocity"));
			
			scr.addScreenLine(CDetailLinesService.createSection("Schedule"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "startDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "endDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.addScreenLine(CDetailLinesService.createSection("Sprint Items"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sprintItems"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "itemCount"));
			
			// Scrum Guide 2020 - Sprint Retrospective
			scr.addScreenLine(CDetailLinesService.createSection("Sprint Retrospective (Scrum Guide 2020)"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retrospectiveNotes"));
			
			scr.addScreenLine(CDetailLinesService.createSection("Additional Information"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentId"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addAttachmentsSection(scr, clazz);
			
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addCommentsSection(scr, clazz);
			
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating sprint view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "entityType", "description", "startDate", "endDate", "status", "color", "assignedTo", "itemCount",
				"project", "createdDate", "lastModifiedDate"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
		// initialize another sprint view screen
		final CDetailSection detailSection_2 = create_SprintEditingView(project);
		detailSection_2.setName(detailSection.getName() + "_Editing");
		final CGridEntity grid_2 = create_SprintEditingGridEntity(project);
		grid_2.setName(grid.getName() + "_Editing");
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection_2, grid_2, menuTitle + "_2",
				pageTitle + "_2", pageDescription + "_2", showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
		try {
			// LOGGER.debug("Initializing sample sprints for project: {}", project.getName());
			// Get services
			final CSprintService sprintService = CSpringContext.getBean(CSprintService.class);
			final CSprintTypeService sprintTypeService = CSpringContext.getBean(CSprintTypeService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
			final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
			final CProjectItemStatusService projectItemStatusService =
					CSpringContext.getBean(CProjectItemStatusService.class);
			
			// Scrum Guide 2020 - Sprint Goal examples
			final String[] sprintGoals = {
				"Complete user authentication and authorization features",
				"Implement data export and reporting capabilities"
			};
			final String[] definitionsOfDone = {
				"- All acceptance criteria met\n- Code reviewed and approved\n- Unit tests pass (>80% coverage)\n- Integration tests pass\n- Documentation updated\n- Product Owner accepts",
				"- Feature complete and tested\n- Performance benchmarks met\n- Security review passed\n- User documentation created\n- Demo ready for stakeholders"
			};
			
			// Create sprints with Scrum Guide 2020 compliant data
			final int sprintCount = minimal ? 1 : 2;
			for (int i = 1; i <= sprintCount; i++) {
				final CSprintType sprintType = sprintTypeService.getRandom(project.getCompany());
				final CUser assignedUser = userService.getRandom(project.getCompany());
				final CSprint sprint = new CSprint("Sprint " + i, project);
				sprint.setDescription("Sprint " + i + " - Development iteration");
				sprint.setEntityType(sprintType);
				sprint.setAssignedTo(assignedUser);
				sprint.setColor(CSprint.DEFAULT_COLOR);
				sprint.setStartDate(LocalDate.now().plusWeeks((i - 1) * 2));
				sprint.setEndDate(LocalDate.now().plusWeeks(i * 2));
				
				// Scrum Guide 2020 - Set Sprint Goal and Definition of Done
				if (i <= sprintGoals.length) {
					sprint.setSprintGoal(sprintGoals[i - 1]);
					sprint.setDefinitionOfDone(definitionsOfDone[i - 1]);
				}
				
				// Set initial status from workflow (CRITICAL: all project items must have status)
				if (sprintType != null && sprintType.getWorkflow() != null) {
					final List<CProjectItemStatus> initialStatuses =
							projectItemStatusService.getValidNextStatuses(sprint);
					if (!initialStatuses.isEmpty()) {
						sprint.setStatus(initialStatuses.get(0));
					}
				}
				// Add random activities and meetings to sprint
				CActivity activity = activityService.getRandom(project);
				if (activity != null) {
					sprint.addItem(activity);
				}
				activity = activityService.getRandom(project);
				if (activity != null) {
					sprint.addItem(activity);
				}
				final CMeeting meeting = meetingService.getRandom(project);
				if (meeting != null) {
					sprint.addItem(meeting);
				}
				
				// Save sprint first to get ID
				sprintService.save(sprint);
				
				// Calculate velocity for completed sprints (Sprint 1 if we're creating Sprint 2)
				if (i == 1 && sprintCount > 1) {
					// Simulate completed sprint with velocity
					sprint.calculateVelocity();
					sprint.setRetrospectiveNotes(
						"WHAT WENT WELL:\n" +
						"- Team collaboration was excellent\n" +
						"- Daily standups kept everyone aligned\n" +
						"- Early testing caught issues\n\n" +
						"WHAT NEEDS IMPROVEMENT:\n" +
						"- Estimation accuracy needs work\n" +
						"- Technical debt growing\n\n" +
						"ACTION ITEMS:\n" +
						"- Schedule estimation workshop next sprint\n" +
						"- Allocate 20% capacity to refactoring"
					);
					sprintService.save(sprint);
				}
				// LOGGER.debug("Created sample sprint: {} with {} items", sprint.getName(), sprint.getItemCount());
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating sample sprints", e);
			throw e;
		}
	}
}
