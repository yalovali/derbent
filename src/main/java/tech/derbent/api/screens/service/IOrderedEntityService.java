package tech.derbent.api.screens.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entity.domain.CEntityDB;

public interface IOrderedEntityService<MasterClass extends CEntityDB<?>, ChildClass extends IOrderedEntity> {

	static final Logger LOGGER = LoggerFactory.getLogger(IOrderedEntityService.class);

	public List<ChildClass> findByCurrentMaster();
	public void moveItemDown(final ChildClass item);

	default void moveItemUp(final ChildClass childItem) {
		if (childItem == null) {
			LOGGER.warn("Cannot move up - item is null");
			return;
		}
		final List<ChildClass> items = findByCurrentMaster();
		for (int i = 0; i < items.size(); i++) {
			if (((CEntityDB<?>) items.get(i)).getId().equals(((CEntityDB<?>) childItem).getId()) && (i > 0)) {
				// Swap orders with previous item
				final ChildClass previousItem = items.get(i - 1);
				final Integer currentOrder = childItem.getItemOrder();
				final Integer previousOrder = previousItem.getItemOrder();
				childItem.setItemOrder(previousOrder);
				previousItem.setItemOrder(currentOrder);
				save(childItem);
				save(previousItem);
				break;
			}
		}
	}

	public void save(final ChildClass item);
}
