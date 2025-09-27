package tech.derbent.api.interfaces;
/** CKanbanType - Interface for type entities used in Kanban boards. Layer: Interface (Abstraction) Provides the contract for type objects that can be
 * used to group entities within kanban columns. */
public interface IKanbanType {

	/** Gets the unique identifier for this type.
	 * @return the type ID */
	Long getId();
	/** Gets the display name for this type.
	 * @return the type name */
	String getName();
	/** Gets the description of this type.
	 * @return the type description */
	String getDescription();
}
