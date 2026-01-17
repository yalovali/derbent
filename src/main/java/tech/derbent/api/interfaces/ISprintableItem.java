package tech.derbent.api.interfaces;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.utils.Check;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.service.CSprintItemService;
import tech.derbent.base.users.domain.CUser;

/** ISprintableItem - Marker interface for entities that can be included in sprints.
 * <p>
 * This interface is implemented by domain entities (e.g., CActivity, CMeeting) that can be added to sprint items. It provides a contract for entities
 * that can be part of sprint planning and tracking.
 * </p>
 * <p>
 * Entities implementing this interface can be:
 * <ul>
 * <li>Added to CSprintItem entities</li>
 * <li>Displayed in sprint item widgets</li>
 * <li>Tracked and managed within sprint contexts</li>
 * <li>Have their services provide sprint display widgets</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Implementation Requirements:</strong>
 * <ul>
 * <li>Entity must extend CProjectItem (provides getId(), getName(), getDescription())</li>
 * <li>Entity's service should implement IEntityRegistrable</li>
 * <li>Entity's page service should implement ISprintItemPageService for sprint widget support</li>
 * </ul>
 * </p>
 * @author Derbent Framework
 * @since 1.0
 * @see CProjectItem
 * @see ISprintItemPageService */
public interface ISprintableItem {

	Logger LOGGER = LoggerFactory.getLogger(ISprintableItem.class);

	/** Adds a backlog item (sprintable entity) to a sprint.
	 * <p>
	 * <strong>CRITICAL:</strong> This method uses the existing sprint item owned by the parent entity (Activity/Meeting). It does NOT create a new
	 * sprint item.
	 * </p>
	 * @param item         The sprintable item to add (must not be null and must have a sprint item)
	 * @param targetSprint The target sprint (must not be null and must be persisted)
	 * @throws IllegalArgumentException if parameters are null or invalid */
	@Transactional
	default void addBacklogItemToSprint(final CSprint targetSprint) {
		moveSprintItemToSprint(targetSprint);
	}

	CUser getAssignedTo();
	String getDescription();
	String getDescriptionShort();
	LocalDate getEndDate();
	Long getId();
	String getName();
	CSprintItem getSprintItem();
	Integer getSprintOrder();
	LocalDate getStartDate();
	CProjectItemStatus getStatus();
	Long getStoryPoint();

	@Transactional
	default void moveSprintItemToBacklog() {
		final CSprintItem sprintItem = getSprintItem();
		Check.notNull(sprintItem, "Sprint item cannot be null");
		Check.notNull(sprintItem.getId(), "Sprint item must be persisted (ID cannot be null)");
		final Long sourceSprintId = sprintItem.getSprint() != null ? sprintItem.getSprint().getId() : null;
		LOGGER.info("[BacklogMove] Moving sprint item {} from sprint {} to backlog", sprintItem.getId(), sourceSprintId);
		// CRITICAL: Set sprint to NULL (backlog semantics), do NOT delete
		sprintItem.setSprint(null);
		// Clear kanban column assignment (backlog items don't have kanban columns)
		sprintItem.setKanbanColumnId(null);
		final CSprintItemService sprintItemService = CSpringContext.getBean(CSprintItemService.class);
		sprintItemService.save(sprintItem);
	}

	@Transactional
	default void moveSprintItemToSprint(final CSprint targetSprint) {
		final CSprintItem sprintItem = getSprintItem();
		Check.notNull(sprintItem, "Sprint item must exist for sprintable item");
		Check.notNull(sprintItem.getId(), "Sprint item must be persisted (ID cannot be null)");
		Check.notNull(targetSprint, "Target sprint cannot be null");
		Check.notNull(targetSprint.getId(), "Target sprint must be persisted (ID cannot be null)");
		final Long sourceSprintId = sprintItem.getSprint() != null ? sprintItem.getSprint().getId() : null;
		final CSprintItemService sprintItemService = CSpringContext.getBean(CSprintItemService.class);
		LOGGER.info("[SprintMove] Moving sprint item {} from sprint {} to sprint {}", sprintItem.getId(), sourceSprintId, targetSprint.getId());
		// Set sprint reference to target sprint
		sprintItem.setSprint(targetSprint);
		// Assign order at end of target sprint
		final Integer nextOrder = sprintItemService.getNextItemOrder(targetSprint);
		sprintItem.setItemOrder(nextOrder);
		// Save the modified sprint item
		sprintItemService.save(sprintItem);
	}

	/** Removes a sprintable item from its current sprint and moves it to backlog.
	 * <p>
	 * This is a convenience method that wraps moveSprintItemToBacklog with the sprintable item's existing sprint item.
	 * </p>
	 * @param item The sprintable item to move to backlog (must not be null)
	 * @throws IllegalArgumentException if item is null or has no sprint item */
	@Transactional
	default void removeItemFromSprint() {
		moveSprintItemToBacklog();
	}

	/** Saves the underlying project item (Activity or Meeting) using its specific service.
	 * <p>
	 * This is a utility method for saving parent entities after modifying their sprint item relationships or other properties.
	 * </p>
	 * @param item The sprintable item to save (must not be null)
	 * @throws IllegalArgumentException if item is null */
	@Transactional
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	default void saveProjectItem() {
		Check.instanceOf(this, CProjectItem.class, "Item must be a CProjectItem");
		final CProjectItem projectItem = (CProjectItem) this;
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(projectItem.getClass());
		Check.notNull(serviceClass, "Service class not found for entity: " + projectItem.getClass().getName());
		final CProjectItemService service = (CProjectItemService) CSpringContext.getBean(serviceClass);
		service.save(projectItem);
		// LOGGER.debug("[Save] Saved project item {} using service {}", projectItem.getId(), serviceClass.getSimpleName());
	}

	void setSprintItem(CSprintItem cSprintItem);
	void setSprintOrder(Integer sprintOrder);
	void setStatus(CProjectItemStatus newStatus);
	void setStoryPoint(Long storyPoint);
}
