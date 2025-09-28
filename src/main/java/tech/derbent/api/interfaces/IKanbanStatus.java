package tech.derbent.api.interfaces;
/** CKanbanStatus - Interface for status entities used in Kanban boards. Layer: Interface (Abstraction) Provides the contract for status objects that
 * can be used to organize entities in kanban columns. */
public interface IKanbanStatus {

	/** Gets the unique identifier for this status.
	 * @return the status ID */
	Long getId();
	/** Gets the display name for this status.
	 * @return the status name */
	String getName();
	/** Gets the description of this status.
	 * @return the status description */
	String getDescription();
	/** Gets the sort order for this status. Used to determine column ordering in the kanban board.
	 * @return the sort order, lower values appear first */
	Integer getSortOrder();
}
