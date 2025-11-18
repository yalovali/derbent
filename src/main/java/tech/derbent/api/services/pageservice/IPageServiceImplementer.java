package tech.derbent.api.services.pageservice;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IEntityUpdateListener;
import tech.derbent.api.views.CDetailsBuilder;
import tech.derbent.base.session.service.ISessionService;

/** Interface for page service implementers that manage entity CRUD operations. Extends IContentOwner for basic entity management and
 * IEntityUpdateListener for entity lifecycle events.
 * @param <EntityClass> the entity type managed by this page */
public interface IPageServiceImplementer<EntityClass extends CEntityDB<EntityClass>> extends IContentOwner, IEntityUpdateListener<EntityClass> {

	CEnhancedBinder<EntityClass> getBinder();
	@Override
	EntityClass getCurrentEntity();
	CDetailsBuilder getDetailsBuilder();
	Class<?> getEntityClass();
	@Override
	CAbstractService<EntityClass> getEntityService();
	CPageService<EntityClass> getPageService();
	ISessionService getSessionService();

	@Override
	default void onEntityRefreshed(final EntityClass entity) throws Exception {
		setCurrentEntity(entity);
		populateForm();
	}

	/** Selects the first item in the grid. Default implementation does nothing; subclasses with grids should override. */
	void selectFirstInGrid();
}
