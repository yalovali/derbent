package tech.derbent.app.gannt.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.interfaces.IGanttDisplayable;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.base.users.domain.CUser;

/** CGanttItem - Data transfer object for Gantt chart representation of project entities. This class wraps project entities to provide a unified
 * interface for Gantt chart display. Follows coding standards with C prefix and provides standardized access to entity properties. */
public class CGanttItem extends CEntityDB<CGanttItem> {

	private final LocalDate endDate;
	private final CEntityOfProject<?> entity;
	private final String entityType;
	private final int hierarchyLevel;
	private final Long parentId;
	private final String parentType;
	private final LocalDate startDate;

	/** Constructor for CGanttItem.
	 * @param entity The project entity to wrap
	 * @throws Exception */
	public CGanttItem(final CEntityOfProject<?> entity) throws Exception {
		this.entity = entity;
		entityType = entity.getClass().getSimpleName();
		startDate = extractStartDate(entity);
		endDate = extractEndDate(entity);
		parentId = extractParentId(entity);
		parentType = extractParentType(entity);
		hierarchyLevel = 0; // Will be calculated by hierarchy service
	}

	/** Constructor with hierarchy level.
	 * @param entity         The project entity to wrap
	 * @param hierarchyLevel The level in the hierarchy (0 = top level)
	 * @throws Exception */
	public CGanttItem(final CEntityOfProject<?> entity, final int hierarchyLevel) throws Exception {
		this.entity = entity;
		entityType = entity.getClass().getSimpleName();
		startDate = extractStartDate(entity);
		endDate = extractEndDate(entity);
		parentId = extractParentId(entity);
		parentType = extractParentType(entity);
		this.hierarchyLevel = hierarchyLevel;
	}

	/** Extract end date from entity using interface or fallback to reflection.
	 * @param entity The entity to extract from
	 * @return The end date or null */
	private LocalDate extractEndDate(final CEntityOfProject<?> entity) {
		// First try the interface approach
		if (entity instanceof tech.derbent.api.interfaces.IGanttDisplayable) {
			return ((tech.derbent.api.interfaces.IGanttDisplayable) entity).getGanttEndDate();
		}
		// Fallback to reflection for backward compatibility
		try {
			// Try dueDate first (for activities)
			Object result = tech.derbent.api.utils.CAuxillaries.invokeMethod(entity, "getDueDate");
			if (result instanceof LocalDate) {
				return (LocalDate) result;
			}
			// Try endDate (for meetings)
			result = tech.derbent.api.utils.CAuxillaries.invokeMethod(entity, "getEndDate");
			if (result instanceof LocalDate) {
				return (LocalDate) result;
			} else if (result instanceof LocalDateTime) {
				return ((LocalDateTime) result).toLocalDate();
			}
		} catch (final Exception e) {
			// Ignore reflection errors
		}
		return null;
	}

	/** Extract parent ID from entity using interface or fallback to reflection.
	 * @param entity The entity to extract from
	 * @return The parent ID or null
	 * @throws Exception */
	private Long extractParentId(final CEntityOfProject<?> entity) throws Exception {
		// First try the interface approach
		if (entity instanceof IGanttDisplayable) {
			return ((IGanttDisplayable) entity).getGanttParentId();
		}
		// Fallback to reflection for backward compatibility
		Object result = CAuxillaries.invokeMethod(entity, "getParentId");
		return result instanceof Long ? (Long) result : null;
	}

	/** Extract parent type from entity using interface or fallback to reflection.
	 * @param entity The entity to extract from
	 * @return The parent type or null
	 * @throws Exception */
	private String extractParentType(final CEntityOfProject<?> entity) throws Exception {
		// First try the interface approach
		if (entity instanceof IGanttDisplayable) {
			return ((IGanttDisplayable) entity).getGanttParentType();
		}
		// Fallback to reflection for backward compatibility
		Object result = CAuxillaries.invokeMethod(entity, "getParentType");
		return result instanceof String ? (String) result : null;
	}

	/** Extract start date from entity using interface or fallback to reflection.
	 * @param entity The entity to extract from
	 * @return The start date or null */
	private LocalDate extractStartDate(final CEntityOfProject<?> entity) {
		// First try the interface approach
		if (entity instanceof IGanttDisplayable) {
			return ((IGanttDisplayable) entity).getGanttStartDate();
		}
		// Fallback to reflection for backward compatibility
		try {
			// Try startDate first (for activities)
			Object result = tech.derbent.api.utils.CAuxillaries.invokeMethod(entity, "getStartDate");
			if (result instanceof LocalDate) {
				return (LocalDate) result;
			}
			// Try meetingDate (for meetings)
			result = tech.derbent.api.utils.CAuxillaries.invokeMethod(entity, "getMeetingDate");
			if (result instanceof LocalDate) {
				return (LocalDate) result;
			} else if (result instanceof LocalDateTime) {
				return ((LocalDateTime) result).toLocalDate();
			}
		} catch (final Exception e) {
			// Ignore reflection errors
		}
		return null;
	}

	/** Get the entity color code for visual representation using interface or fallback to reflection.
	 * @return The color code string */
	public String getColorCode() {
		// First try the interface approach
		if (entity instanceof tech.derbent.api.interfaces.IGanttDisplayable) {
			return ((tech.derbent.api.interfaces.IGanttDisplayable) entity).getGanttColorCode();
		}
		// Fallback to reflection for backward compatibility
		try {
			Object result = tech.derbent.api.utils.CAuxillaries.invokeMethod(entity.getClass(), "getEntityColorCode");
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
	 * @return The project entity */
	public CEntityOfProject<?> getEntity() { return entity; }

	/** Get the entity ID.
	 * @return The entity ID */
	public Long getEntityId() { return entity.getId(); }

	/** Get the entity type name.
	 * @return The entity type */
	public String getEntityType() { return entityType; }

	/** Get the hierarchy level.
	 * @return The hierarchy level */
	public int getHierarchyLevel() { return hierarchyLevel; }

	/** Get the icon filename for the entity.
	 * @return The icon filename */
	public String getIconFilename() {
		try {
			final java.lang.reflect.Method method = entity.getClass().getMethod("getIconFilename");
			return (String) method.invoke(null);
		} catch (final Exception e) {
			return "vaadin:question"; // Default icon
		}
	}

	/** Get the parent entity ID.
	 * @return The parent ID or null */
	public Long getParentId() { return parentId; }

	/** Get the parent entity type.
	 * @return The parent type or null */
	public String getParentType() { return parentType; }

	/** Get the responsible user (assigned to).
	 * @return The responsible user or null */
	public CUser getResponsible() {
		if (entity.getAssignedTo() != null) {
			return entity.getAssignedTo();
		}
		return null;
	}

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
