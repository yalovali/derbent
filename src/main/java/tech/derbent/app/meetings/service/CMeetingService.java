package tech.derbent.app.meetings.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.interfaces.IKanbanService;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.utils.CKanbanUtils;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.domain.CMeetingStatus;
import tech.derbent.app.meetings.domain.CMeetingType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMeetingService extends CEntityOfProjectService<CMeeting> implements IKanbanService<CMeeting, CMeetingStatus> {

	private final CMeetingTypeService meetingTypeService;
	private final CMeetingStatusService meetingStatusService;

	CMeetingService(final IMeetingRepository repository, final Clock clock, final ISessionService sessionService,
			final CMeetingTypeService meetingTypeService, final CMeetingStatusService meetingStatusService) {
		super(repository, clock, sessionService);
		this.meetingTypeService = meetingTypeService;
		this.meetingStatusService = meetingStatusService;
	}

	@Override
	public String checkDeleteAllowed(final CMeeting entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public List<CMeetingStatus> getAllStatuses(Long projectId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<CMeetingStatus, List<CMeeting>> getEntitiesGroupedByStatus(final Long projectId) {
		return CKanbanUtils.getEmptyGroupedStatus(this.getClass());
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
		// Note: If no meeting type exists, the field will remain null (it's nullable)
		// Initialize status - get first available meeting status for the project (optional field, don't throw if missing)
		final List<CMeetingStatus> availableStatuses = meetingStatusService.listByProject(currentProject);
		if (!availableStatuses.isEmpty()) {
			entity.setStatus(availableStatuses.get(0));
		}
		// Note: If no status exists, the field will remain null (it's nullable)
	}

	@Override
	public CMeeting updateEntityStatus(final CMeeting entity, final CMeetingStatus newStatus) {
		CKanbanUtils.updateEntityStatusSimple(entity, newStatus, CMeeting::setStatus);
		return save(entity);
	}
}
