package tech.derbent.app.sprints.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.screens.service.IOrderedEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.base.session.service.ISessionService;

/** CSprintItemService - Service class for managing sprint items. 
 * Sprint items are progress tracking components owned by CActivity/CMeeting.
 * They store progress data (story points, dates, responsible person, progress %).
 * Implements IOrderedEntityService for reordering within sprints/backlog.
 */
@Service
@PreAuthorize ("isAuthenticated()")
public class CSprintItemService extends CAbstractService<CSprintItem> 
		implements IEntityRegistrable, IOrderedEntityService<CSprintItem> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintItemService.class);

	public CSprintItemService(final ISprintItemRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}
	
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
		sprintItem.setItemOrder(1); // Default order
		return sprintItem;
	}
	
	/** Find all sprint items by sprint ID.
	 * @param masterId the sprint ID
	 * @return list of sprint items */
	public List<CSprintItem> findByMasterId(final Long masterId) {
		Check.notNull(masterId, "Master ID cannot be null");
		return getTypedRepository().findByMasterId(masterId);
	}
	
	/** Find all sprint items by sprint ID with their parent items loaded.
	 * This eagerly loads the parent items (Activity/Meeting) for display.
	 * @param masterId the sprint ID
	 * @return list of sprint items with parent items loaded */
	public List<CSprintItem> findByMasterIdWithItems(final Long masterId) {
		final List<CSprintItem> items = findByMasterId(masterId);
		// Parent items are loaded via @Transient parentItem field set by parent
		// They are loaded when the parent entity is fetched from database
		return items;
	}
	
	/** Get the next item order for a new item in a sprint.
	 * @param sprint the sprint
	 * @return the next available order number */
	public Integer getNextItemOrder(final tech.derbent.app.sprints.domain.CSprint sprint) {
		if (sprint == null || sprint.getId() == null) {
			return 1; // Default for backlog or new sprint
		}
		final List<CSprintItem> items = findByMasterId(sprint.getId());
		if (items.isEmpty()) {
			return 1;
		}
		// Find max order and add 1
		final Integer maxOrder = items.stream()
			.map(CSprintItem::getItemOrder)
			.filter(order -> order != null)
			.max(Integer::compareTo)
			.orElse(0);
		return maxOrder + 1;
	}
	
	// IOrderedEntityService implementation
	
	@Override
	public void moveItemUp(final CSprintItem item) {
		Check.notNull(item, "Sprint item cannot be null");
		final List<CSprintItem> items = getSiblingItems(item);
		
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(item.getId()) && i > 0) {
				final CSprintItem previousItem = items.get(i - 1);
				final Integer currentOrder = item.getItemOrder();
				final Integer previousOrder = previousItem.getItemOrder();
				item.setItemOrder(previousOrder);
				previousItem.setItemOrder(currentOrder);
				save(item);
				save(previousItem);
				break;
			}
		}
	}
	
	@Override
	public void moveItemDown(final CSprintItem item) {
		Check.notNull(item, "Sprint item cannot be null");
		final List<CSprintItem> items = getSiblingItems(item);
		
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(item.getId()) && i < items.size() - 1) {
				final CSprintItem nextItem = items.get(i + 1);
				final Integer currentOrder = item.getItemOrder();
				final Integer nextOrder = nextItem.getItemOrder();
				item.setItemOrder(nextOrder);
				nextItem.setItemOrder(currentOrder);
				save(item);
				save(nextItem);
				break;
			}
		}
	}
	
	/** Get sibling items (items in same sprint or backlog) sorted by itemOrder.
	 * @param item the reference item
	 * @return list of sibling items sorted by order */
	private List<CSprintItem> getSiblingItems(final CSprintItem item) {
		final List<CSprintItem> siblings;
		
		if (item.getSprint() == null) {
			// Backlog items - find all items with no sprint
			siblings = getTypedRepository().findBySprint(null);
		} else {
			// Sprint items - find all items in same sprint
			siblings = getTypedRepository().findByMasterId(item.getSprint().getId());
		}
		
		// Sort by itemOrder, nulls last
		siblings.sort((a, b) -> {
			if (a.getItemOrder() == null && b.getItemOrder() == null) return 0;
			if (a.getItemOrder() == null) return 1;
			if (b.getItemOrder() == null) return -1;
			return a.getItemOrder().compareTo(b.getItemOrder());
		});
		
		return siblings;
	}

	@Override
	public Class<CSprintItem> getEntityClass() { return CSprintItem.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceSprintItem.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }
}
