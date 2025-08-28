package tech.derbent.meetings.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.abstracts.interfaces.CKanbanService;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMeetingService extends CEntityOfProjectService<CMeeting> implements CKanbanService<CMeeting, CMeetingStatus> {
	CMeetingService(final CMeetingRepository repository, final Clock clock) {
		super(repository, clock);
	}

	@Override
	public List<CMeetingStatus> getAllStatuses() { return List.of(); }

	@Override
	public Map<CMeetingStatus, List<CMeeting>> getEntitiesGroupedByStatus(final Long projectId) {
		return Map.of();
	}

	@Override
	protected Class<CMeeting> getEntityClass() { return CMeeting.class; }

	@Override
	public CMeeting updateEntityStatus(final CMeeting entity, final CMeetingStatus newStatus) {
		Check.notNull(entity, "Entity cannot be null");
		Check.notNull(newStatus, "New status cannot be null");
		entity.setStatus(newStatus);
		return save(entity);
	}
}
