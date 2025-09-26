package tech.derbent.api.interfaces;

import java.time.LocalDate;

/** CGanttDisplayable - Interface for entities that can be displayed in Gantt charts. Provides standardized access to date properties without using
 * reflection. Layer: Interface (MVC) */
public interface CGanttDisplayable {

	/** Gets the start date for Gantt display.
	 * @return the start date or null if not available */
	default LocalDate getGanttStartDate() { return null; }

	/** Gets the end date for Gantt display.
	 * @return the end date or null if not available */
	default LocalDate getGanttEndDate() { return null; }

	/** Gets the parent ID for hierarchy display.
	 * @return the parent ID or null if top-level */
	default Long getGanttParentId() { return null; }

	/** Gets the parent type for hierarchy display.
	 * @return the parent type or null if top-level */
	default String getGanttParentType() { return null; }

	/** Gets the color code for visual representation.
	 * @return the color code string */
	default String getGanttColorCode() {
		return "#6c757d"; // Default gray color
	}
}
