package tech.derbent.api.services.pageservice;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.base.session.service.ISessionService;

public interface IPageServiceImplementer<EntityClass extends CEntityDB<EntityClass>> {

	CEnhancedBinder<EntityClass> getBinder();
	EntityClass getCurrentEntity();
	Class<?> getEntityClass();
	CAbstractService<EntityClass> getEntityService();
	ISessionService getSessionService();
	void onEntityCreated(EntityClass newEntity);
	void onEntityDeleted(EntityClass entity);
	void onEntityRefreshed(CEntityDB<?> reloaded);
	void onEntitySaved(EntityClass savedEntity);
	void populateForm();
	void selectFirstInGrid();
	void setCurrentEntity(EntityClass entity);
}
