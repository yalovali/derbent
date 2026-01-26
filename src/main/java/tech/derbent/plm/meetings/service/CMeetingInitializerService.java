package tech.derbent.plm.meetings.service;

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
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.meetings.domain.CMeetingType;

public class CMeetingInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Meeting Information";
	private static final Class<?> ENTITY_CLASS = CMeeting.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".5";
	private static final String menuTitle = MenuTitle_PROJECT + ".Meetings";
	private static final String pageDescription = "Meeting management with scheduling and participant tracking";
	private static final String pageTitle = "Meeting Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, ENTITY_CLASS);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "entityType"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "project"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Schedule"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "startDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "startTime"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "endDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "endTime"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "lastModifiedDate"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Participants"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "attendees"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "participants"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Agenda & Location"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "agenda"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "location"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Status & Follow-up"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "relatedActivity"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "assignedTo"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "minutes"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "linkedElement"));
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addDefaultSection(detailSection, ENTITY_CLASS);
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(detailSection, ENTITY_CLASS);
			// Agile Parent section - standard section for entities with agile hierarchy
			CAgileParentRelationInitializerService.addDefaultSection(detailSection, ENTITY_CLASS, project);
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating meeting view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, ENTITY_CLASS);
		grid.setColumnFields(List.of("id", "name", "description", "entityType", "project", "startDate", "startTime", "endDate", "endTime",
				"createdBy", "createdDate", "status", "location"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(ENTITY_CLASS, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	/**
	 * Initialize sample meetings for a project with relationships (comments, participants).
	 *
	 * @param project the project to create meetings for
	 * @param minimal if true, creates only 1 meeting; if false, creates 2 meetings
	 */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		// Seed data for sample meetings
		record MeetingSeed(String name, String description, String location, String agenda, int startDaysOffset, int durationDays) {}

		final List<MeetingSeed> seeds = List.of(
				new MeetingSeed("Q1 Planning Session", "Quarterly planning session to review goals and set priorities",
						"Conference Room A / Virtual",
						"1. Review Q4 achievements\n2. Discuss Q1 objectives\n3. Resource allocation\n4. Budget planning", 250, 3),
				new MeetingSeed("Technical Architecture Review", "Review and discuss technical architecture decisions and implementation approach",
						"Engineering Lab / Teams",
						"1. Architecture proposal presentation\n2. Security considerations\n3. Scalability discussion\n4. Technology stack decisions",
						150, 2));

		try {
			final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
			final CMeetingTypeService meetingTypeService = CSpringContext.getBean(CMeetingTypeService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);

			final List<CMeeting> createdMeetings = new java.util.ArrayList<>();
			int index = 0;
			
			for (final MeetingSeed seed : seeds) {
				final CMeetingType type = meetingTypeService.getRandom(project.getCompany());
				final CUser user1 = userService.getRandom(project.getCompany());
				final CUser user2 = userService.getRandom(project.getCompany());

				final CMeeting meeting = new CMeeting(seed.name(), project);
				meeting.setEntityType(type);
				meeting.setDescription(seed.description());

				// Set initial status from workflow
				if (type != null && type.getWorkflow() != null) {
					final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(meeting);
					if (!initialStatuses.isEmpty()) {
						meeting.setStatus(initialStatuses.get(0));
					}
				}

				meeting.setAssignedTo(user1);
				meeting.setStartDate(LocalDate.now().plusDays((int) (Math.random() * seed.startDaysOffset())));
				meeting.setEndDate(meeting.getStartDate().plusDays((int) (Math.random() * seed.durationDays())));
				meeting.setLocation(seed.location());
				meeting.setAgenda(seed.agenda());
				meeting.addParticipant(user1);
				meeting.addParticipant(user2);
				meetingService.save(meeting);
				
				createdMeetings.add(meeting);
				index++;
				
				if (minimal) {
					break;
				}
			}

			// Add relationships: comments (only if not minimal)
			if (!minimal && !createdMeetings.isEmpty()) {
				addRelationshipsToMeetings(createdMeetings, userService, meetingService);
			}

			LOGGER.debug("Created {} sample meeting(s) for project: {}", index, project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample meetings for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample meetings for project: " + project.getName(), e);
		}
	}

	/**
	 * Add relationships (comments) to sample meetings.
	 */
	private static void addRelationshipsToMeetings(final List<CMeeting> meetings, final CUserService userService,
			final CMeetingService meetingService) {
		try {
			// Add comments to first meeting using helper
			if (meetings.size() > 0) {
				final CMeeting meeting1 = meetings.get(0);
				tech.derbent.api.screens.service.CRelationshipSampleHelper.addSampleComments(
					meeting1,
					new String[] {
						"Please prepare Q4 performance metrics before the meeting",
						"Meeting link: https://teams.microsoft.com/meeting/..."
					},
					new boolean[] { true, false }  // First comment is important
				);
				meetingService.save(meeting1);
				LOGGER.debug("Added comments to meeting: {}", meeting1.getName());
			}

			// Add comments to second meeting using helper
			if (meetings.size() > 1) {
				final CMeeting meeting2 = meetings.get(1);
				tech.derbent.api.screens.service.CRelationshipSampleHelper.addSampleComments(
					meeting2,
					"Architecture diagrams will be shared 24 hours before the meeting"
				);
				meetingService.save(meeting2);
				LOGGER.debug("Added comments to meeting: {}", meeting2.getName());
			}

			LOGGER.info("Added comments to {} meetings", meetings.size());
		} catch (final Exception e) {
			LOGGER.warn("Error adding relationships to meetings: {}", e.getMessage(), e);
			// Don't fail the whole initialization if relationships fail
		}
	}
}
