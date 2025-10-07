package tech.derbent.meetings.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.interfaces.IKanbanService;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.utils.CKanbanUtils;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMeetingService extends CEntityOfProjectService<CMeeting> implements IKanbanService<CMeeting, CMeetingStatus> {

	CMeetingService(final IMeetingRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
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
	public CMeeting updateEntityStatus(final CMeeting entity, final CMeetingStatus newStatus) {
		CKanbanUtils.updateEntityStatusSimple(entity, newStatus, CMeeting::setStatus);
		return save(entity);
	}
}
