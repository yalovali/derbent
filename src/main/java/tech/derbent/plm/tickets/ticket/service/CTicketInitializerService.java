package tech.derbent.plm.tickets.ticket.service;

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
import tech.derbent.plm.tickets.ticket.domain.CTicket;
import tech.derbent.plm.tickets.ticketpriority.domain.CTicketPriority;
import tech.derbent.plm.tickets.ticketpriority.service.CTicketPriorityService;
import tech.derbent.plm.tickets.tickettype.domain.CTicketType;
import tech.derbent.plm.tickets.tickettype.service.CTicketTypeService;

public class CTicketInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CTicket.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CTicketInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".20";
	private static final String menuTitle = MenuTitle_PROJECT + ".Tickets";
	private static final String pageDescription = "Ticket management";
	private static final String pageTitle = "Ticket Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);

			// Identity and Request Metadata
			detailSection.addScreenLine(CDetailLinesService.createSection("Request Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "externalReference"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requestor"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "origin"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "duplicateOf"));

			// Priority and Urgency
			detailSection.addScreenLine(CDetailLinesService.createSection("Priority & Classification"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "urgency"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "criticality"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priority"));

			// Time Tracking
			detailSection.addScreenLine(CDetailLinesService.createSection("Time Tracking"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "initialDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "plannedDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dueDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workHoursEstimated"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workHoursReal"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workHoursLeft"));

			// Status and Assignment
			detailSection.addScreenLine(CDetailLinesService.createSection("Status & Assignment"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "serviceDepartment"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));

			// Resolution
			detailSection.addScreenLine(CDetailLinesService.createSection("Resolution"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "resolution"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "resolutionDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isRegression"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "result"));

			// Planning
			detailSection.addScreenLine(CDetailLinesService.createSection("Planning"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "plannedActivity"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "targetMilestone"));

			// Product/Component
			detailSection.addScreenLine(CDetailLinesService.createSection("Product & Component"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "product"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "component"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "affectedVersions"));

			// Context
			detailSection.addScreenLine(CDetailLinesService.createSection("Additional Context"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "contextInformation"));

			// Audit
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));

			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addDefaultSection(detailSection, clazz);

			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(detailSection, clazz);

			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating ticket view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "status", "project", "assignedTo", "createdBy", "createdDate"));
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
	 * Initialize sample tickets for a project.
	 *
	 * @param project the project to create tickets for
	 * @param minimal if true, creates only 1 ticket; if false, creates 2 tickets
	 */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		try {
			final CTicketService ticketService = CSpringContext.getBean(CTicketService.class);
			final CTicketTypeService ticketTypeService = CSpringContext.getBean(CTicketTypeService.class);
			final CTicketPriorityService ticketPriorityService = CSpringContext.getBean(CTicketPriorityService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);

			final CTicketType type1 = ticketTypeService.getRandom(project.getCompany());
			final CUser user1 = userService.getRandom(project.getCompany());

			List<CTicketPriority> priorities = ticketPriorityService.listByCompany(project.getCompany());
			if (priorities.isEmpty()) {
				final CTicketPriority defaultPriority = new CTicketPriority("Default Priority", project.getCompany());
				defaultPriority.setIsDefault(true);
				defaultPriority.setPriorityLevel(3);
				ticketPriorityService.save(defaultPriority);
				priorities = List.of(defaultPriority);
			}

			final CTicketPriority priority1 = priorities.get(0);
			final CTicket ticket1 = new CTicket("Login Authentication Bug", project);
			ticket1.setDescription("Users unable to login with correct credentials");
			ticket1.setEntityType(type1);
			ticket1.setAssignedTo(user1);
			if (priority1 != null) {
				ticket1.setPriority(priority1);
			}
			if (type1 != null && type1.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(ticket1);
				if (!initialStatuses.isEmpty()) {
					ticket1.setStatus(initialStatuses.get(0));
				}
			}
			ticketService.save(ticket1);

			if (minimal) {
				return;
			}

			final CTicketType type2 = ticketTypeService.getRandom(project.getCompany());
			final CUser user2 = userService.getRandom(project.getCompany());
			final CTicketPriority priority2 = priorities.get(0);

			final CTicket ticket2 = new CTicket("Dashboard Customization Feature", project);
			ticket2.setDescription("Allow users to customize their dashboard layout");
			ticket2.setEntityType(type2);
			ticket2.setAssignedTo(user2);
			if (priority2 != null) {
				ticket2.setPriority(priority2);
			}
			if (type2 != null && type2.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(ticket2);
				if (!initialStatuses.isEmpty()) {
					ticket2.setStatus(initialStatuses.get(0));
				}
			}
			ticketService.save(ticket2);

			LOGGER.debug("Created sample tickets for project: {}", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample tickets for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample tickets for project: " + project.getName(), e);
		}
	}
}
