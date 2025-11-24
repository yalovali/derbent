package tech.derbent.app.sprints.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.base.session.service.ISessionService;

/**
 * CSprintItemService - Service class for managing sprint items.
 * Provides business logic for sprint item operations and dynamic item loading.
 */
@Service
@PreAuthorize("isAuthenticated()")
public class CSprintItemService extends CAbstractService<CSprintItem> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintItemService.class);

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private CActivityService activityService;

	@Autowired
	private CMeetingService meetingService;

	public CSprintItemService(final ISprintItemRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CSprintItem> getEntityClass() {
		return CSprintItem.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return null; // No initializer service needed
	}

	@Override
	public Class<?> getPageServiceClass() {
		return null; // Uses default page service
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	/**
	 * Load the project item for a sprint item based on itemId and itemType.
	 * This method dynamically loads the item at runtime.
	 * @param sprintItem the sprint item to load the item for
	 */
	public void loadItem(final CSprintItem sprintItem) {
		if (sprintItem == null || sprintItem.getItemId() == null || sprintItem.getItemType() == null) {
			LOGGER.warn("Cannot load item - sprint item, itemId, or itemType is null");
			return;
		}

		try {
			final CProjectItem<?> item = loadItemByIdAndType(sprintItem.getItemId(), sprintItem.getItemType());
			sprintItem.setItem(item);
		} catch (final Exception e) {
			LOGGER.error("Failed to load item for sprint item {}: {}", sprintItem.getId(), e.getMessage(), e);
		}
	}

	/**
	 * Load items for a list of sprint items.
	 * @param sprintItems the list of sprint items
	 */
	public void loadItems(final List<CSprintItem> sprintItems) {
		if (sprintItems == null || sprintItems.isEmpty()) {
			return;
		}

		for (final CSprintItem sprintItem : sprintItems) {
			loadItem(sprintItem);
		}
	}

	/**
	 * Load a project item by ID and type.
	 * @param itemId the item ID
	 * @param itemType the item type (e.g., "CActivity", "CMeeting")
	 * @return the project item, or null if not found
	 */
	private CProjectItem<?> loadItemByIdAndType(final Long itemId, final String itemType) {
		try {
			// Map item type to service and load the item
			switch (itemType) {
				case "CActivity":
					return activityService.getById(itemId).orElse(null);
				case "CMeeting":
					return meetingService.getById(itemId).orElse(null);
				default:
					LOGGER.warn("Unknown item type: {}", itemType);
					return null;
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to load item {} of type {}: {}", itemId, itemType, e.getMessage());
			return null;
		}
	}

	/**
	 * Find all sprint items for a specific sprint.
	 * @param sprintId the sprint ID
	 * @return list of sprint items
	 */
	public List<CSprintItem> findBySprintId(final Long sprintId) {
		return ((ISprintItemRepository) repository).findBySprintIdOrderByItemOrderAsc(sprintId);
	}

	/**
	 * Find all sprint items for a specific sprint and load their items.
	 * @param sprintId the sprint ID
	 * @return list of sprint items with loaded items
	 */
	public List<CSprintItem> findBySprintIdWithItems(final Long sprintId) {
		final List<CSprintItem> sprintItems = findBySprintId(sprintId);
		loadItems(sprintItems);
		return sprintItems;
	}

	/**
	 * Find all sprint items of a specific type.
	 * @param itemType the item type (e.g., "CActivity", "CMeeting")
	 * @return list of sprint items
	 */
	public List<CSprintItem> findByItemType(final String itemType) {
		return ((ISprintItemRepository) repository).findByItemType(itemType);
	}

	/**
	 * Find a sprint item by sprint ID and item ID.
	 * @param sprintId the sprint ID
	 * @param itemId the item ID
	 * @return the sprint item, or null if not found
	 */
	public CSprintItem findBySprintIdAndItemId(final Long sprintId, final Long itemId) {
		return ((ISprintItemRepository) repository).findBySprintIdAndItemId(sprintId, itemId);
	}

	/**
	 * Move a sprint item up in the order (decrease order number).
	 * @param sprintItem the sprint item to move up
	 */
	public void moveItemUp(final CSprintItem sprintItem) {
		if (sprintItem == null || sprintItem.getSprint() == null) {
			LOGGER.warn("Cannot move up - sprint item or sprint is null");
			return;
		}

		final List<CSprintItem> items = findBySprintId(sprintItem.getSprint().getId());
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(sprintItem.getId()) && i > 0) {
				// Swap orders with previous item
				final CSprintItem previousItem = items.get(i - 1);
				final Integer currentOrder = sprintItem.getItemOrder();
				final Integer previousOrder = previousItem.getItemOrder();
				sprintItem.setItemOrder(previousOrder);
				previousItem.setItemOrder(currentOrder);
				save(sprintItem);
				save(previousItem);
				LOGGER.debug("Moved sprint item {} up from order {} to {}", sprintItem.getId(), currentOrder, previousOrder);
				break;
			}
		}
	}

	/**
	 * Move a sprint item down in the order (increase order number).
	 * @param sprintItem the sprint item to move down
	 */
	public void moveItemDown(final CSprintItem sprintItem) {
		if (sprintItem == null || sprintItem.getSprint() == null) {
			LOGGER.warn("Cannot move down - sprint item or sprint is null");
			return;
		}

		final List<CSprintItem> items = findBySprintId(sprintItem.getSprint().getId());
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(sprintItem.getId()) && i < (items.size() - 1)) {
				// Swap orders with next item
				final CSprintItem nextItem = items.get(i + 1);
				final Integer currentOrder = sprintItem.getItemOrder();
				final Integer nextOrder = nextItem.getItemOrder();
				sprintItem.setItemOrder(nextOrder);
				nextItem.setItemOrder(currentOrder);
				save(sprintItem);
				save(nextItem);
				LOGGER.debug("Moved sprint item {} down from order {} to {}", sprintItem.getId(), currentOrder, nextOrder);
				break;
			}
		}
	}

	/**
	 * Create a new sprint item for the given sprint.
	 * @param sprint the sprint
	 * @param item the project item to add
	 * @return the new sprint item
	 */
	public CSprintItem newSprintItem(final CSprint sprint, final CProjectItem<?> item) {
		if (sprint == null || item == null) {
			LOGGER.warn("Cannot create sprint item - sprint or item is null");
			return null;
		}

		final List<CSprintItem> existingItems = findBySprintId(sprint.getId());
		final int nextOrder = existingItems.size() + 1;

		final CSprintItem sprintItem = new CSprintItem(sprint, item, nextOrder);
		LOGGER.debug("Created new sprint item for sprint {} with order {}", sprint.getId(), nextOrder);
		return sprintItem;
	}
}
