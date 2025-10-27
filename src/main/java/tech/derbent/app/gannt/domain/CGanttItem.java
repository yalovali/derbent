package tech.derbent.app.gannt.domain;

import java.time.LocalDate;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CProjectItem;
import tech.derbent.base.users.domain.CUser;

/** CGanttItem - Data transfer object for Gantt chart representation of project items. This class wraps project items (CActivity, CMeeting, CDecision,
 * COrder) to provide a unified interface for Gantt chart display. Follows coding standards with C prefix and provides standardized access to entity
 * properties through the CProjectItem base class. */
public class CGanttItem extends CEntityDB<CGanttItem> {

	private final LocalDate endDate;
	private final CProjectItem<?> entity;
	private final String entityType;
	private final int hierarchyLevel;
	private final Long parentId;
	private final String parentType;
	private final LocalDate startDate;

	/** Constructor for CGanttItem.
	 * @param entity The project item to wrap */
	public CGanttItem(final CProjectItem<?> entity) {
		this.entity = entity;
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
	public CGanttItem(final CProjectItem<?> entity, final int hierarchyLevel) {
		this.entity = entity;
		entityType = entity.getClass().getSimpleName();
		startDate = entity.getStartDate();
		endDate = entity.getEndDate();
		parentId = entity.getParentId();
		parentType = entity.getParentType();
		this.hierarchyLevel = hierarchyLevel;
	}

	/** Get the entity color code for visual representation using interface or fallback to reflection.
	 * @return The color code string */
	public String getColorCode() {
		// Fallback to reflection for backward compatibility
		try {
			final Object result = tech.derbent.api.utils.CAuxillaries.invokeMethod(entity.getClass(), "getEntityColorCode");
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
