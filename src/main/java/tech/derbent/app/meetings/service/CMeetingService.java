package tech.derbent.app.meetings.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.domains.IHasStatusAndWorkflowService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.services.CProjectItemService;
import tech.derbent.api.services.pageservice.implementations.CPageServiceMeeting;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMeetingService extends CProjectItemService<CMeeting> implements IEntityRegistrable {
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
}
