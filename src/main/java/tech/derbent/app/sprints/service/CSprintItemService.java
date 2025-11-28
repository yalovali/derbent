package tech.derbent.app.sprints.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.screens.service.IOrderedEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.base.session.service.ISessionService;

/** CSprintItemService - Service class for managing sprint items. Provides business logic for sprint item operations and dynamic item loading.
 * <p>
 * Follows the common naming conventions for child entity services:
 * <ul>
 * <li>{@code findByMaster(M master)} - Find all items by master entity</li>
 * <li>{@code findByMasterId(Long id)} - Find all items by master ID</li>
 * <li>{@code findByMasterIdWithItems(Long id)} - Find with eager loaded transient data</li>
 * </ul>
 */
@Service
@PreAuthorize ("isAuthenticated()")
public class CSprintItemService extends CAbstractService<CSprintItem> implements IEntityRegistrable, IOrderedEntityService<CSprintItem> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintItemService.class);
	@Autowired
	private CActivityService activityService;
	@Autowired
	private CMeetingService meetingService;

	public CSprintItemService(final ISprintItemRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Find all sprint items of a specific type.
	 * @param itemType the item type (e.g., "CActivity", "CMeeting")
	 * @return list of sprint items */
	public List<CSprintItem> findByItemType(final String itemType) {
		return getTypedRepository().findByItemType(itemType);
	}

	/** Find all sprint items for a specific sprint (master entity).
	 * @param master the sprint entity
	 * @return list of sprint items ordered by itemOrder */
	@Transactional (readOnly = true)
	public List<CSprintItem> findByMaster(final CSprint master) {
		Check.notNull(master, "Master cannot be null");
		if (master.getId() == null) {
			// new instance, no items yet
			return List.of();
		}
		return getTypedRepository().findByMaster(master);
	}

	/** Find all sprint items by sprint (master) ID.
	 * @param masterId the sprint ID
	 * @return list of sprint items ordered by itemOrder */
	@Transactional (readOnly = true)
	public List<CSprintItem> findByMasterId(final Long masterId) {
		Check.notNull(masterId, "Master ID cannot be null");
		return getTypedRepository().findByMasterId(masterId);
	}

	/** Find all sprint items for a specific sprint and load their project items.
	 * @param masterId the sprint ID
	 * @return list of sprint items with loaded project items */
	public List<CSprintItem> findByMasterIdWithItems(final Long masterId) {
		final List<CSprintItem> sprintItems = findByMasterId(masterId);
		loadItems(sprintItems);
		return sprintItems;
	}

	@Override
	public Class<CSprintItem> getEntityClass() { return CSprintItem.class; }

	@Override
	public Class<?> getInitializerServiceClass() {
		return null; // No initializer service needed
	}

	@Override
	public Class<?> getPageServiceClass() {
		return null; // Uses default page service
	}

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Get the typed repository for this service.
	 * @return the ISprintItemRepository */
	private ISprintItemRepository getTypedRepository() { return (ISprintItemRepository) repository; }

	/** Load the project item for a sprint item based on itemId and itemType. This method dynamically loads the item at runtime.
	 * @param sprintItem the sprint item to load the item for */
	public void loadItem(final CSprintItem sprintItem) {
		if ((sprintItem == null) || (sprintItem.getItemId() == null) || (sprintItem.getItemType() == null)) {
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

	/** Load a project item by ID and type.
	 * @param itemId   the item ID
	 * @param itemType the item type (e.g., "CActivity", "CMeeting")
	 * @return the project item, or null if not found */
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

	/** Load items for a list of sprint items.
	 * @param sprintItems the list of sprint items */
	public void loadItems(final List<CSprintItem> sprintItems) {
		if ((sprintItems == null) || sprintItems.isEmpty()) {
			return;
		}
		for (final CSprintItem sprintItem : sprintItems) {
			loadItem(sprintItem);
		}
	}

	/** Move a sprint item down in the order (increase order number).
	 * @param childItem the sprint item to move down */
	@Override
	public void moveItemDown(final CSprintItem childItem) {
		if ((childItem == null)) {
			LOGGER.warn("Cannot move down - sprint item or sprint is null");
			return;
		}
		final List<CSprintItem> items = findByMaster(childItem.getSprint());
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(childItem.getId()) && (i < (items.size() - 1))) {
				// Swap orders with next item
				final CSprintItem nextItem = items.get(i + 1);
				final Integer currentOrder = childItem.getItemOrder();
				final Integer nextOrder = nextItem.getItemOrder();
				childItem.setItemOrder(nextOrder);
				nextItem.setItemOrder(currentOrder);
				save(childItem);
				save(nextItem);
				break;
			}
		}
	}

	/** Move a sprint item up in the order (decrease order number).
	 * @param childItem the sprint item to move up */
	@Override
	public void moveItemUp(final CSprintItem childItem) {
		if (childItem == null) {
			LOGGER.warn("Cannot move up - sprint item is null");
			return;
		}
		final List<CSprintItem> items = findByMaster(childItem.getSprint());
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(childItem.getId()) && (i > 0)) {
				// Swap orders with previous item
				final CSprintItem previousItem = items.get(i - 1);
				final Integer currentOrder = childItem.getItemOrder();
				final Integer previousOrder = previousItem.getItemOrder();
				childItem.setItemOrder(previousOrder);
				previousItem.setItemOrder(currentOrder);
				save(childItem);
				save(previousItem);
				break;
			}
		}
	}

	/** Create a new sprint item for the given sprint (master).
	 * @param master the sprint
	 * @param item   the project item to add
	 * @return the new sprint item */
	public CSprintItem newSprintItem(final CSprint master, final CProjectItem<?> item) {
		if ((master == null) || (item == null)) {
			LOGGER.warn("Cannot create sprint item - master or item is null");
			return null;
		}
		final List<CSprintItem> existingItems = findByMasterId(master.getId());
		final int nextOrder = existingItems.size() + 1;
		final CSprintItem sprintItem = new CSprintItem(master, item, nextOrder);
		LOGGER.debug("Created new sprint item for master {} with order {}", master.getId(), nextOrder);
		return sprintItem;
	}
}
