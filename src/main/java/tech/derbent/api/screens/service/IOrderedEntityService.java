package tech.derbent.api.screens.service;

import tech.derbent.api.entity.domain.CEntityDB;

public interface IOrderedEntityService<EntityClass extends CEntityDB<EntityClass>> {

	public void moveItemDown(final EntityClass item);
	public void moveItemUp(final EntityClass item);
}
