package tech.derbent.api.domains;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.api.utils.Check;

/** Service for managing parent-child relationships between project items.
 * Provides methods for establishing, removing, and querying hierarchical relationships
 * with validation for circular dependencies and type compatibility. */
@Service
public class CParentChildRelationService extends CAbstractService<CParentChildRelation> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CParentChildRelationService.class);
	private final IParentChildRelationRepository repository;

	public CParentChildRelationService(final IParentChildRelationRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
		this.repository = repository;
	}
	
	@Override
	protected Class<CParentChildRelation> getEntityClass() {
		return CParentChildRelation.class;
	}

	/** Establish a parent-child relationship between two project items.
	 * This method validates that:
	 * - Both items are persisted (have IDs)
	 * - No circular dependency would be created
	 * - Parent type allows children (based on entity type configuration)
	 * - Items are not the same
	 * @param child  the child project item
	 * @param parent the parent project item
	 * @throws IllegalArgumentException if validation fails */
	@Transactional
	public void setParent(final CProjectItem<?> child, final CProjectItem<?> parent) {
		Check.notNull(child, "Child item cannot be null");
		Check.notNull(parent, "Parent item cannot be null");
		Check.notNull(child.getId(), "Child item must be persisted (ID cannot be null)");
		Check.notNull(parent.getId(), "Parent item must be persisted (ID cannot be null)");
		final String childType = child.getClass().getSimpleName();
		final String parentType = parent.getClass().getSimpleName();
		// Check that items are not the same
		if (child.getId().equals(parent.getId()) && childType.equals(parentType)) {
			throw new IllegalArgumentException("An item cannot be its own parent");
		}
		// Check for circular dependency
		if (wouldCreateCircularDependency(parent.getId(), parentType, child.getId(), childType)) {
			throw new IllegalArgumentException("Setting this parent would create a circular dependency");
		}
		// Remove any existing parent relationship for this child
		final Optional<CParentChildRelation> existingRelation = repository.findByChild(child.getId(), childType);
		existingRelation.ifPresent(relation -> repository.delete(relation));
		// Create new relationship
		final CParentChildRelation newRelation = new CParentChildRelation(child.getId(), childType, parent.getId(), parentType);
		repository.save(newRelation);
		// Update the child's parent fields
		child.setParentId(parent.getId());
		child.setParentType(parentType);
		LOGGER.info("Established parent-child relationship: {}#{} -> {}#{}", parentType, parent.getId(), childType, child.getId());
	}

	/** Remove the parent relationship for a child item.
	 * @param child the child project item */
	@Transactional
	public void clearParent(final CProjectItem<?> child) {
		Check.notNull(child, "Child item cannot be null");
		Check.notNull(child.getId(), "Child item must be persisted (ID cannot be null)");
		final String childType = child.getClass().getSimpleName();
		repository.deleteByChild(child.getId(), childType);
		child.clearParent();
		LOGGER.info("Cleared parent relationship for {}#{}", childType, child.getId());
	}

	/** Get the parent item for a child project item.
	 * @param child the child project item
	 * @return optional parent project item */
	@SuppressWarnings ("unchecked")
	@Transactional (readOnly = true)
	public <T extends CProjectItem<?>> Optional<T> getParent(final CProjectItem<?> child) {
		Check.notNull(child, "Child item cannot be null");
		Check.notNull(child.getId(), "Child item must be persisted (ID cannot be null)");
		final String childType = child.getClass().getSimpleName();
		final Optional<CParentChildRelation> relation = repository.findByChild(child.getId(), childType);
		if (relation.isEmpty()) {
			return Optional.empty();
		}
		final CParentChildRelation rel = relation.get();
		try {
			// Get the service for the parent entity type
			final Class<?> parentClass = CEntityRegistry.getEntityClassByTitle(rel.getParentType());
			if (parentClass == null) {
				LOGGER.warn("Could not find entity class for type: {}", rel.getParentType());
				return Optional.empty();
			}
			final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(parentClass);
			if (serviceClass == null) {
				LOGGER.warn("Could not find service class for entity: {}", parentClass.getSimpleName());
				return Optional.empty();
			}
			final CProjectItemService<?> service = (CProjectItemService<?>) CSpringContext.getBean(serviceClass);
			return (Optional<T>) service.getById(rel.getParentId());
		} catch (final Exception e) {
			LOGGER.error("Error retrieving parent for {}#{}: {}", childType, child.getId(), e.getMessage(), e);
			return Optional.empty();
		}
	}

	/** Get all child items for a parent project item.
	 * @param parent the parent project item
	 * @return list of child project items */
	@SuppressWarnings ("unchecked")
	@Transactional (readOnly = true)
	public <T extends CProjectItem<?>> List<T> getChildren(final CProjectItem<?> parent) {
		Check.notNull(parent, "Parent item cannot be null");
		Check.notNull(parent.getId(), "Parent item must be persisted (ID cannot be null)");
		final String parentType = parent.getClass().getSimpleName();
		final List<CParentChildRelation> relations = repository.findByParent(parent.getId(), parentType);
		final List<T> children = new ArrayList<>();
		for (final CParentChildRelation rel : relations) {
			try {
				final Class<?> childClass = CEntityRegistry.getEntityClassByTitle(rel.getChildType());
				if (childClass == null) {
					LOGGER.warn("Could not find entity class for type: {}", rel.getChildType());
					continue;
				}
				final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(childClass);
				if (serviceClass == null) {
					LOGGER.warn("Could not find service class for entity: {}", childClass.getSimpleName());
					continue;
				}
				final CProjectItemService<?> service = (CProjectItemService<?>) CSpringContext.getBean(serviceClass);
				final Optional<?> child = service.getById(rel.getChildId());
				child.ifPresent(c -> children.add((T) c));
			} catch (final Exception e) {
				LOGGER.error("Error retrieving child {}#{}: {}", rel.getChildType(), rel.getChildId(), e.getMessage(), e);
			}
		}
		return children;
	}

	/** Get all child items for a parent project item filtered by child entity class.
	 * This is useful when you want only children of a specific type (e.g., only CActivity children).
	 * @param parent          the parent project item
	 * @param childEntityClass the class name of children to retrieve (e.g., "CActivity")
	 * @return list of child project items matching the specified type */
	@SuppressWarnings ("unchecked")
	@Transactional (readOnly = true)
	public <T extends CProjectItem<?>> List<T> getChildrenByType(final CProjectItem<?> parent, final String childEntityClass) {
		Check.notNull(parent, "Parent item cannot be null");
		Check.notNull(parent.getId(), "Parent item must be persisted (ID cannot be null)");
		Check.notBlank(childEntityClass, "Child entity class cannot be blank");
		final String parentType = parent.getClass().getSimpleName();
		final List<CParentChildRelation> relations = repository.findByParentAndChildType(parent.getId(), parentType, childEntityClass);
		final List<T> children = new ArrayList<>();
		for (final CParentChildRelation rel : relations) {
			try {
				final Class<?> childClass = CEntityRegistry.getEntityClassByTitle(rel.getChildType());
				if (childClass == null) {
					LOGGER.warn("Could not find entity class for type: {}", rel.getChildType());
					continue;
				}
				final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(childClass);
				if (serviceClass == null) {
					LOGGER.warn("Could not find service class for entity: {}", childClass.getSimpleName());
					continue;
				}
				final CProjectItemService<?> service = (CProjectItemService<?>) CSpringContext.getBean(serviceClass);
				final Optional<?> child = service.getById(rel.getChildId());
				child.ifPresent(c -> children.add((T) c));
			} catch (final Exception e) {
				LOGGER.error("Error retrieving child {}#{}: {}", rel.getChildType(), rel.getChildId(), e.getMessage(), e);
			}
		}
		return children;
	}

	/** Check if setting a parent would create a circular dependency.
	 * This checks if the proposed parent is already a descendant of the child.
	 * @param parentId   ID of the proposed parent
	 * @param parentType class name of the proposed parent
	 * @param childId    ID of the child
	 * @param childType  class name of the child
	 * @return true if circular dependency would be created */
	@Transactional (readOnly = true)
	public boolean wouldCreateCircularDependency(final Long parentId, final String parentType, final Long childId, final String childType) {
		Check.notNull(parentId, "Parent ID cannot be null");
		Check.notBlank(parentType, "Parent type cannot be blank");
		Check.notNull(childId, "Child ID cannot be null");
		Check.notBlank(childType, "Child type cannot be blank");
		// Check if parent is a descendant of child
		final List<Object[]> descendants = repository.findAllDescendants(childId, childType);
		for (final Object[] desc : descendants) {
			final Long descId = ((Number) desc[0]).longValue();
			final String descType = (String) desc[1];
			if (descId.equals(parentId) && descType.equals(parentType)) {
				return true;
			}
		}
		return false;
	}

	/** Check if a project item can have children based on its entity type configuration.
	 * Note: Not all CProjectItem subclasses have getEntityType() method.
	 * This method uses reflection to check if the type allows children.
	 * @param item the project item to check
	 * @return true if the item's type allows children, or true by default if type cannot be determined */
	@Transactional (readOnly = true)
	public boolean canHaveChildren(final CProjectItem<?> item) {
		if (item == null) {
			return false;
		}
		try {
			// Use reflection to get entityType (not all CProjectItem subclasses have this method)
			final java.lang.reflect.Method getEntityTypeMethod = item.getClass().getMethod("getEntityType");
			final Object entityType = getEntityTypeMethod.invoke(item);
			if (entityType instanceof CTypeEntity) {
				return ((CTypeEntity<?>) entityType).getCanHaveChildren();
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not determine entity type for {}, defaulting to allowing children", item.getClass().getSimpleName());
		}
		// If no entity type is set or method doesn't exist, default to allowing children
		return true;
	}
}
