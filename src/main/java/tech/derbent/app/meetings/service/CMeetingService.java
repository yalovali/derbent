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
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.service.CSprintItemService;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMeetingService extends CProjectItemService<CMeeting> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingService.class);
	private final CMeetingTypeService meetingTypeService;
	private final CSprintItemService sprintItemService;

	CMeetingService(final IMeetingRepository repository, final Clock clock, final ISessionService sessionService,
			final CMeetingTypeService meetingTypeService, final CProjectItemStatusService projectItemStatusService,
			final CSprintItemService sprintItemService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.meetingTypeService = meetingTypeService;
		this.sprintItemService = sprintItemService;
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
		detachSprintItemIfPresent(meeting);
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

	private void detachSprintItemIfPresent(final CMeeting meeting) {
		final CSprintItem sprintItem = meeting.getSprintItem();
		if (sprintItem == null || sprintItem.getId() == null) {
			return;
		}
		meeting.setSprintItem(null);
		repository.saveAndFlush(meeting);
		sprintItemService.delete(sprintItem.getId());
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
		entity.setLocation("To be decided");
		entity.setStartDate(LocalDate.now(clock)); // Default: now
		entity.setStartTime(LocalTime.of(12, 00)); // Default: 10 AM
		entity.setEndDate(LocalDate.now(clock)); // Default: 1 hour duration
		entity.setStartTime(entity.getStartTime().plusHours(2));
		entity.setResponsible(currentUser);
		LOGGER.debug("Meeting initialization complete with default start time and responsible user: {}", currentUser.getName());
	}

	/** Lists meetings by project ordered by sprintOrder for sprint-aware components. Items with null sprintOrder will appear last.
	 * @param project the project
	 * @return list of meetings ordered by sprintOrder ASC, id DESC */
	public java.util.List<CMeeting> listForProjectBacklog(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		return ((IMeetingRepository) repository).listForProjectBacklog(project);
	}
}
