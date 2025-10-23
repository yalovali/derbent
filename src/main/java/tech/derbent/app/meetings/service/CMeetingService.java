package tech.derbent.app.meetings.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.services.CProjectItemService;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.domain.CMeetingType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMeetingService extends CProjectItemService<CMeeting> {

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
	protected Class<CMeeting> getEntityClass() { return CMeeting.class; }

	@Override
	public void initializeNewEntity(final CMeeting entity) {
		super.initializeNewEntity(entity);
		// Get current user and project from session
		final CUser currentUser = sessionService.getActiveUser()
				.orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize meeting"));
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize meeting"));
		// Initialize date fields - default meeting starts now, ends in 1 hour
		entity.setMeetingDate(LocalDateTime.now(clock));
		entity.setEndDate(LocalDateTime.now(clock).plusHours(1));
		// Initialize responsible with current user
		entity.setResponsible(currentUser);
		// Initialize meeting type - get first available meeting type for the project (optional field, don't throw if missing)
		final List<CMeetingType> availableTypes = meetingTypeService.listByProject(currentProject);
		if (!availableTypes.isEmpty()) {
			entity.setMeetingType(availableTypes.get(0));
		}
	}
}
