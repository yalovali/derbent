package tech.derbent.api.screens.service;

/** Interface for entities that can be ordered within a parent entity.
 * Implementations should extend CEntityDB to provide the getId() method. */
public interface IOrderedEntity {

	/** Get the unique identifier of this entity.
	 * @return the entity ID */
	Long getId();

	/** Get the item order for this entity.
	 * @return the item order */
	Integer getItemOrder();

	/** Set the item order for this entity.
	 * @param itemOrder the new item order */
	void setItemOrder(Integer itemOrder);
}
