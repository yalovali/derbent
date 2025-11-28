package tech.derbent.api.screens.service;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
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
