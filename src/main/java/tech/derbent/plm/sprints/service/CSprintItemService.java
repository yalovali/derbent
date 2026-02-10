package tech.derbent.plm.sprints.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.screens.service.IOrderedEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.service.IActivityRepository;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.meetings.service.IMeetingRepository;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintItem;

/** CSprintItemService - Service class for managing sprint items. Sprint items are progress tracking components owned by CActivity/CMeeting. They
 * store progress data (story points, dates, progress %). Implements IOrderedEntityService for reordering within sprints/backlog. */
@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
public class CSprintItemService extends CAbstractService<CSprintItem> implements IEntityRegistrable, IOrderedEntityService<CSprintItem> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintItemService.class);

	public CSprintItemService(final ISprintItemRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CSprintItem item) {
		return super.checkDeleteAllowed(item);
	}

	/** Find all sprint items by sprint ID.
	 * @param masterId the sprint ID
	 * @return list of sprint items */
	public List<CSprintItem> findByMasterId(final Long masterId) {
		Check.notNull(masterId, "Master ID cannot be null");
		return getTypedRepository().findByMasterId(masterId);
	}

	/** Find all sprint items by sprint ID with their parent items loaded. This eagerly loads the parent items (Activity/Meeting) for display.
	 * @param masterId the sprint ID
	 * @return list of sprint items with parent items loaded */
	public List<CSprintItem> findByMasterIdWithItems(final Long masterId) {
		final List<CSprintItem> items = findByMasterId(masterId);
		// CRITICAL: Load parent items and set the transient back-reference
		// Sprint items have a transient @Transient parentItem field that must be populated
		// after loading from database for the composition pattern to work correctly
		final IActivityRepository activityRepo = CSpringContext.getBean(IActivityRepository.class);
		final IMeetingRepository meetingRepo = CSpringContext.getBean(IMeetingRepository.class);
		for (final CSprintItem sprintItem : items) {
			if (sprintItem.getId() != null) {
				try {
					// Try to find activity first
					final Optional<CActivity> activity = activityRepo.findBySprintItemId(sprintItem.getId());
					if (activity.isPresent()) {
						sprintItem.setParentItem(activity.get());
						continue;
					}
					// If not an activity, try meeting
					final Optional<CMeeting> meeting = meetingRepo.findBySprintItemId(sprintItem.getId());
					if (meeting.isPresent()) {
						sprintItem.setParentItem(meeting.get());
					}
				} catch (final Exception e) {
					LOGGER.error("[DragDrop] Failed to load parent item for sprint item {}", sprintItem.getId(), e);
				}
			}
		}
		return items;
	}

	@Override
	public Class<CSprintItem> getEntityClass() { return CSprintItem.class; }

	/** Get the next item order for a new item in a sprint.
	 * @param sprint the sprint
	 * @return the next available order number */
	public Integer getNextItemOrder(final CSprint sprint) {
		if (sprint == null || sprint.getId() == null) {
			return 1; // Default for backlog or new sprint
		}
		final List<CSprintItem> items = findByMasterId(sprint.getId());
		if (items.isEmpty()) {
			return 1;
		}
		// Find max order and add 1
		final Integer maxOrder = items.stream().map(CSprintItem::getItemOrder).filter(order -> order != null).max(Integer::compareTo).orElse(0);
		return maxOrder + 1;
	}
	// IOrderedEntityService implementation

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceSprintItem.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

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
			if (a.getItemOrder() == null && b.getItemOrder() == null) {
				return 0;
			}
			if (a.getItemOrder() == null) {
				return 1;
			}
			if (b.getItemOrder() == null) {
				return -1;
			}
			return a.getItemOrder().compareTo(b.getItemOrder());
		});
		return siblings;
	}

	protected ISprintItemRepository getTypedRepository() { return (ISprintItemRepository) repository; }

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
	protected void validateEntity(final CSprintItem entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		// Note: SprintItem usually doesn't have direct required fields that aren't managed by code (like parentItem)
		// But let's check basics
		// 2. Numeric Checks
		if (entity.getProgressPercentage() != null && (entity.getProgressPercentage() < 0 || entity.getProgressPercentage() > 100)) {
			throw new IllegalArgumentException("Progress Percentage must be between 0 and 100");
		}
		if (entity.getItemOrder() != null && entity.getItemOrder() < 1) {
			throw new IllegalArgumentException("Item Order must be at least 1");
		}
		// 3. Date Logic
		if (entity.getStartDate() != null && entity.getDueDate() != null && entity.getDueDate().isBefore(entity.getStartDate())) {
			throw new IllegalArgumentException("Due Date cannot be before Start Date");
		}
	}
}
