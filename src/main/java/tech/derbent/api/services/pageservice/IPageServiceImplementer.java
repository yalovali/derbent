package tech.derbent.api.services.pageservice;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IEntityUpdateListener;
import tech.derbent.base.session.service.ISessionService;

/**
 * Interface for page service implementers that manage entity CRUD operations.
 * Extends IContentOwner for basic entity management and IEntityUpdateListener for entity lifecycle events.
 * 
 * @param <EntityClass> the entity type managed by this page
 */
public interface IPageServiceImplementer<EntityClass extends CEntityDB<EntityClass>> 
		extends IContentOwner, IEntityUpdateListener<EntityClass> {

	/**
	 * Gets the binder used for form validation and binding.
	 * @return the enhanced binder for the entity
	 */
	CEnhancedBinder<EntityClass> getBinder();

	/**
	 * Gets the current entity being edited/viewed.
	 * Overrides IContentOwner to return the specific entity type.
	 * @return the current entity
	 */
	@Override
	EntityClass getCurrentEntity();

	/**
	 * Gets the entity class managed by this page.
	 * @return the entity class
	 */
	Class<?> getEntityClass();

	/**
	 * Gets the entity service for this page.
	 * Overrides IContentOwner to return the specific service type.
	 * @return the entity service
	 */
	@Override
	CAbstractService<EntityClass> getEntityService();

	/**
	 * Gets the session service for user and project context.
	 * @return the session service
	 */
	ISessionService getSessionService();

	@Override
	default void onEntityRefreshed(EntityClass entity) throws Exception {
		setCurrentEntity(entity);
		populateForm();
	}

	/**
	 * Selects the first item in the grid.
	 * Default implementation does nothing; subclasses with grids should override.
	 */
	void selectFirstInGrid();
}
