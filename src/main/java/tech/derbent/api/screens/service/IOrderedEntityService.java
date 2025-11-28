package tech.derbent.api.screens.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service interface for managing ordered child entities within a parent/master entity context.
 * Provides operations for moving items up/down in an ordered list.
 * @param <ChildClass> The child entity type that implements IOrderedEntity */
public interface IOrderedEntityService<ChildClass extends IOrderedEntity> {

	Logger LOGGER = LoggerFactory.getLogger(IOrderedEntityService.class);

	/** Delete the entity.
	 * @param item the item to delete */
	void delete(final ChildClass item);

	/** Move the item down in the ordering (increase order number).
	 * @param item the item to move down */
	void moveItemDown(final ChildClass item);

	/** Move the item up in the ordering (decrease order number).
	 * @param item the item to move up */
	void moveItemUp(final ChildClass item);

	/** Save the entity.
	 * @param item the item to save
	 * @return the saved item */
	ChildClass save(final ChildClass item);
}
