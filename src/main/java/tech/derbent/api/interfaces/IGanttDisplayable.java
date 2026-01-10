package tech.derbent.api.interfaces;

import java.time.LocalDate;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.base.users.domain.CUser;

/** IGanttDisplayable - Interface for entities that can be displayed in Gantt charts. Provides standardized access to properties for Gantt chart
 * visualization without using reflection. Implementing classes should provide consistent date handling, visual representation, user assignments, and
 * hierarchical relationships for optimal Gantt chart display. Layer: Interface (MVC) */
public interface IGanttDisplayable {

	/** Clear the parent relationship for this item. */
	void clearParent();
	/** Get the user responsible for this item. This is typically the assigned user.
	 * @return the responsible user, or null if not assigned */
	CUser getAssignedTo();
	/** Get the end date of this item. For activities, this is the due date. For meetings, this is the end date/time converted to LocalDate.
	 * @return the end date, or null if not set */
	LocalDate getEndDate();
	/** Get the icon identifier for this item type. Should return a Vaadin icon identifier (e.g., "vaadin:tasks", "vaadin:calendar").
	 * @return the icon identifier */
	String getIconString();
	/** Get the parent item ID for hierarchy display.
	 * @return the parent item ID, or null if this is a top-level item */
	Long getParentId();
	/** Get the parent item type (class name) for hierarchy display.
	 * @return the parent type name (e.g., "CActivity", "CMeeting"), or null if no parent */
	String getParentType();
	/** Get the start date of this item. For activities, this is the start date. For meetings, this is the meeting date/time converted to LocalDate.
	 * @return the start date, or null if not set */
	LocalDate getStartDate();
	/** Set the parent item for hierarchy display. The parent must be persisted (have a non-null ID).
	 * @param parent the parent item, or null to clear the parent relationship
	 * @throws IllegalArgumentException if parent is not persisted or if attempting to set self as parent */
	void setParent(CProjectItem<?> parent);
}
