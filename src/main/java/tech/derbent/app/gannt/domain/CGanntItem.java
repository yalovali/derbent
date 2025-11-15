package tech.derbent.app.gannt.domain;

import java.time.LocalDate;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.base.users.domain.CUser;

/** CGanttItem - Data transfer object for Gantt chart representation of project items. This class wraps project items (CActivity, CMeeting, CDecision,
 * COrder) to provide a unified interface for Gantt chart display. Follows coding standards with C prefix and provides standardized access to entity
 * properties through the CProjectItem base class. */
// <T extends CEntityDB<T>>
public class CGanntItem extends CEntityOfProject<CGanntItem> {

	private final LocalDate endDate;
	private final CProjectItem<?> entity;
	private final String entityType;
	private final int hierarchyLevel;
	private final Long parentId;
	private final String parentType;
	private final LocalDate startDate;

	/** Constructor for CGanttItem.
	 * @param entity The project item to wrap */
	public CGanntItem(final CProjectItem<?> entity) {
		this.entity = entity;
		id = entity.getId();
		entityType = entity.getClass().getSimpleName();
		startDate = entity.getStartDate();
		endDate = entity.getEndDate();
		parentId = entity.getParentId();
		parentType = entity.getParentType();
		hierarchyLevel = 0; // Will be calculated by hierarchy service
	}

	/** Constructor with hierarchy level.
	 * @param entity         The project item to wrap
	 * @param hierarchyLevel The level in the hierarchy (0 = top level) */
	public CGanntItem(final CProjectItem<?> entity, final int hierarchyLevel) {
		this.entity = entity;
		id = entity.getId();
		entityType = entity.getClass().getSimpleName();
		startDate = entity.getStartDate();
		endDate = entity.getEndDate();
		parentId = entity.getParentId();
		parentType = entity.getParentType();
		this.hierarchyLevel = hierarchyLevel;
	}

	/** Constructor for CGanttItem with explicit ID to avoid collisions across different entity types.
	 * @param entity   The project item to wrap
	 * @param uniqueId Unique ID for this CGanttItem (sequential counter to prevent Activity ID=1 and Meeting ID=1 collision) */
	public CGanntItem(final CProjectItem<?> entity, final long uniqueId) {
		this.entity = entity;
		id = uniqueId;
		entityType = entity.getClass().getSimpleName();
		startDate = entity.getStartDate();
		endDate = entity.getEndDate();
		parentId = entity.getParentId();
		parentType = entity.getParentType();
		hierarchyLevel = 0; // Will be calculated by hierarchy service
	}

	/** Get the entity color code for visual representation using interface or fallback to reflection.
	 * @return The color code string */
	public String getColorCode() {
		// Fallback to reflection for backward compatibility
		try {
			final Object result = CColorUtils.getStaticIconColorCode(entity.getClass());
			if (result instanceof String) {
				return (String) result;
			}
		} catch (final Exception e) {
			// Ignore reflection errors
		}
		return "#6c757d"; // Default gray color
	}

	/** Get the entity description.
	 * @return The description or empty string */
	@Override
	public String getDescription() {
		if (entity.getDescription() != null) {
			return entity.getDescription();
		}
		return "";
	}

	/** Get the duration in days.
	 * @return The duration in days, or 1 if dates are not available */
	public long getDurationDays() {
		if ((startDate != null) && (endDate != null)) {
			return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
		}
		return 1; // Default duration
	}

	/** Get the end date.
	 * @return The end date */
	public LocalDate getEndDate() { return endDate; }

	/** Get the wrapped entity.
	 * @return The project item */
	public CProjectItem<?> getEntity() { return entity; }

	/** Get the entity ID.
	 * @return The entity ID */
	public Long getEntityId() { return entity.getId(); }

	/** Get the entity type name.
	 * @return The entity type */
	public String getEntityType() { return entityType; }

	public CProjectItem<?> getGanntItem(final CEntityOfProjectService<?> activityService, final CEntityOfProjectService<?> meetingService) {
		CEntityOfProjectService<?> service = null;
		final CProjectItem<?> selectedItem = getEntity();
		Check.notNull(selectedItem, "Selected Gantt item entity is null");
		if (selectedItem instanceof CActivity) {
			service = activityService;
		} else if (selectedItem instanceof CMeeting) {
			service = meetingService;
		} else {
			Check.fail("Unsupported entity type selected in Gantt item: " + selectedItem.getClass().getSimpleName());
		}
		// Add other entity type checks as needed
		final CProjectItem<?> entity = (CProjectItem<?>) service.getById(getEntityId()).orElse(null);
		Check.notNull(entity, "Entity not found for Gantt item selection");
		return entity;
	}

	/** Get the hierarchy level.
	 * @return The hierarchy level */
	public int getHierarchyLevel() { return hierarchyLevel; }

	/** Get the icon identifier for the entity.
	 * @return The icon identifier */
	public String getIcon() { return entity.getIcon(); }

	/** Get the parent entity ID.
	 * @return The parent ID or null */
	public Long getParentId() { return parentId; }

	/** Get the parent entity type.
	 * @return The parent type or null */
	public String getParentType() { return parentType; }

	/** Get the progress percentage of the task.
	 * @return Progress percentage (0-100), or 0 if not available */
	public int getProgressPercentage() {
		// Try to get progress from entity if it has a progress field
		try {
			final Object result = CAuxillaries.invokeMethod(entity, "getProgressPercentage");
			if (result instanceof Integer) {
				return (Integer) result;
			}
			if (result instanceof Double) {
				return ((Double) result).intValue();
			}
		} catch (final Exception e) {
			// Ignore - entity doesn't have progress field
		}
		// Default: calculate based on dates (if task is past due date, 100%, if past start, estimate 50%)
		if (hasDates()) {
			final LocalDate now = LocalDate.now();
			if (now.isAfter(endDate)) {
				return 100; // Task is past end date
			}
			if (now.isBefore(startDate)) {
				return 0; // Task hasn't started
			}
			// Task is in progress - estimate based on time elapsed
			final long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
			final long elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, now);
			if (totalDays > 0) {
				return (int) ((elapsedDays * 100) / totalDays);
			}
		}
		return 0;
	}

	/** Get the responsible user.
	 * @return The responsible user or null */
	public CUser getResponsible() { return entity.getResponsible(); }

	/** Get the responsible user name.
	 * @return The responsible user name or "Unassigned" */
	public String getResponsibleName() {
		final CUser responsible = getResponsible();
		if (responsible != null) {
			return responsible.getName();
		}
		return "Unassigned";
	}

	/** Get the start date.
	 * @return The start date */
	public LocalDate getStartDate() { return startDate; }

	/** Check if dates are available for timeline display.
	 * @return true if both start and end dates are available */
	public boolean hasDates() {
		return (startDate != null) && (endDate != null);
	}

	/** Check if this item has a parent.
	 * @return true if parent exists */
	public boolean hasParent() {
		return (parentId != null) && (parentType != null);
	}

	@Override
	public void initializeAllFields() {
		// No entity relationships to initialize in this entity
	}
}
