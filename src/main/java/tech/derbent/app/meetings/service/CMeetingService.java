package tech.derbent.app.meetings.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.domain.CPageServiceMeeting;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMeetingService extends CProjectItemService<CMeeting> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingService.class);
	private final CMeetingTypeService meetingTypeService;

	CMeetingService(final IMeetingRepository repository, final Clock clock, final ISessionService sessionService,
			final CMeetingTypeService meetingTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.meetingTypeService = meetingTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CMeeting entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	@Transactional
	public void delete(final CMeeting meeting) {
		Check.notNull(meeting, "Meeting cannot be null");
		Check.notNull(meeting.getId(), "Meeting ID cannot be null");
		// The OneToOne relationship with cascade = CascadeType.ALL and orphanRemoval =
		// true
		// will automatically delete the sprint item when the meeting is deleted.
		// No need to manually detach and save, which would violate @NotNull constraint.
		super.delete(meeting);
	}

	@Override
	@Transactional
	public void delete(final Long id) {
		Check.notNull(id, "Meeting ID cannot be null");
		final CMeeting meeting =
				repository.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Meeting not found: " + id));
		delete(meeting);
	}

	@Override
	public Class<CMeeting> getEntityClass() { return CMeeting.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CMeetingInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceMeeting.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CMeeting entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new meeting entity");
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize meeting"));
		final CUser currentUser = sessionService.getActiveUser()
				.orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize meeting"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, meetingTypeService, projectItemStatusService);
		// Business event fields stay in CMeeting
		entity.setLocation("To be decided");
		entity.setStartDate(LocalDate.now(clock));
		entity.setStartTime(LocalTime.of(12, 00));
		entity.setEndDate(LocalDate.now(clock));
		entity.setEndTime(LocalTime.of(14, 00)); // 2 hour duration
		entity.setAssignedTo(currentUser);
		// Create sprint item for progress tracking (composition pattern)
		// Progress fields (storyPoint, dates, responsible, progress%) live in
		// CSprintItem
		// CRITICAL: This is the ONLY place where setSprintItem() should be called
		// Sprint items are created ONCE during entity initialization and NEVER replaced
		final CSprintItem sprintItem = new CSprintItem();
		sprintItem.setSprint(null); // null = backlog
		sprintItem.setProgressPercentage(0);
		sprintItem.setStartDate(LocalDate.now(clock));
		sprintItem.setDueDate(LocalDate.now(clock));
		sprintItem.setCompletionDate(null);
		sprintItem.setStoryPoint(0L);
		sprintItem.setItemOrder(1); // Default order
		entity.setSprintItem(sprintItem);
		LOGGER.debug("Meeting initialization complete with sprint item for progress tracking");
	}

	/** Lists meetings by project ordered by sprintOrder for sprint-aware components. Items with null sprintOrder will appear last.
	 * @param project the project
	 * @return list of meetings ordered by sprintOrder ASC, id DESC */
	public java.util.List<CMeeting> listForProjectBacklog(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		return ((IMeetingRepository) repository).listForProjectBacklog(project);
	}
}
