package tech.derbent.api.interfaces;
/** CKanbanEntity - Interface for entities that can be displayed in a Kanban board. Layer: Interface (Abstraction) Any entity that implements this
 * interface can be used in the generic kanban system. Provides the basic contract for entities with status-based organization. */
public interface CKanbanEntity {

	/** Gets the unique identifier for this entity.
	 * @return the entity ID */
	Long getId();
	/** Gets the display name for this entity.
	 * @return the entity name */
	String getName();
	/** Gets the description of this entity.
	 * @return the entity description */
	String getDescription();
	/** Gets the current status of this entity.
	 * @return the entity status */
	CKanbanStatus getStatus();
	/** Sets the status of this entity.
	 * @param status the new status */
	void setStatus(CKanbanStatus status);
	/** Gets the type information for this entity.
	 * @return the entity type, may be null */
	CKanbanType getType();
}
