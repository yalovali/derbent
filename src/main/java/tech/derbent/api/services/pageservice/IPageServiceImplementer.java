package tech.derbent.api.services.pageservice;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IEntityUpdateListener;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.base.session.service.ISessionService;

public interface IPageServiceImplementer<EntityClass extends CEntityDB<EntityClass>> extends IEntityUpdateListener<EntityClass> {

	CEnhancedBinder<EntityClass> getBinder();
	EntityClass getCurrentEntity();
	Class<?> getEntityClass();
	CAbstractService<EntityClass> getEntityService();
	ISessionService getSessionService();

	@Override
	default void onEntityRefreshed(EntityClass entity) throws Exception {
		setCurrentEntity(entity);
		populateForm();
	}

	void populateForm() throws Exception;
	void selectFirstInGrid();
	void setCurrentEntity(EntityClass entity);
}
