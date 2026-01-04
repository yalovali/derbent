package tech.derbent.app.sprints.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.utils.Check;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.base.session.service.ISessionService;

/** CSprintItemService - Service class for managing sprint items. 
 * Sprint items are progress tracking components owned by CActivity/CMeeting.
 * They store progress data (story points, dates, responsible person, progress %).
 */
@Service
@PreAuthorize ("isAuthenticated()")
public class CSprintItemService extends CAbstractService<CSprintItem> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintItemService.class);

	public CSprintItemService(final ISprintItemRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}
	
	@Override
	protected ISprintItemRepository getTypedRepository() {
		return (ISprintItemRepository) repository;
	}

	/** Create a default sprint item for backlog (sprint = null).
	 * This is used when creating new sprintable items without a specific sprint.
	 * @return a new CSprintItem with default values for backlog items */
	public static CSprintItem createDefaultSprintItem() {
		final CSprintItem sprintItem = new CSprintItem();
		sprintItem.setSprint(null); // null = backlog
		sprintItem.setProgressPercentage(0);
		sprintItem.setStoryPoint(0L);
		sprintItem.setStartDate(null); // Will be set when initialized
		sprintItem.setDueDate(null); // Will be set when initialized
		sprintItem.setCompletionDate(null); // Not completed yet
		sprintItem.setResponsible(null); // Will be set when initialized
		return sprintItem;
	}
	
	/** Find all sprint items by sprint ID.
	 * @param masterId the sprint ID
	 * @return list of sprint items */
	public List<CSprintItem> findByMasterId(final Long masterId) {
		Check.notNull(masterId, "Master ID cannot be null");
		return getTypedRepository().findByMasterId(masterId);
	}

	@Override
	public Class<CSprintItem> getEntityClass() { return CSprintItem.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceSprintItem.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }
}
