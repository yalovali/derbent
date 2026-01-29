package tech.derbent.api.domains;

import java.lang.reflect.Method;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;

/** Service for managing parent-child relationships between project items. Provides methods for establishing, removing, and querying hierarchical
 * relationships with validation for circular dependencies and type compatibility. */
@Service
public class CParentChildRelationService extends CAbstractService<CParentChildRelation> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CParentChildRelationService.class);

	/** Check if a project item can have children based on its entity type configuration. Note: Not all CProjectItem subclasses have getEntityType()
	 * method. This method uses reflection to check if the type allows children.
	 * @param item the project item to check
	 * @return true if the item's type allows children, or true by default if type cannot be determined */
	@Transactional (readOnly = true)
	public static boolean canHaveChildren(final CProjectItem<?> item) {
		if (item == null) {
			return false;
		}
		try {
			// Use reflection to get entityType (not all CProjectItem subclasses have this method)
			final Method getEntityTypeMethod = item.getClass().getMethod("getEntityType");
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

	/** Helper method to load child items from parent-child relations. Extracts common logic for loading entities from relation records.
	 * @param relations list of parent-child relation records
	 * @return list of loaded child items */
	@SuppressWarnings ("unchecked")
	private static <T extends CProjectItem<?>> List<T> loadChildrenFromRelations(final List<CParentChildRelation> relations) {
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

	public CParentChildRelationService(final IParentChildRelationRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Remove the parent relationship for a child item.
	 * @param child the child project item */
	@Transactional
	public void clearParent(final CProjectItem<?> child) {
		Objects.requireNonNull(child, "Child item cannot be null");
		Objects.requireNonNull(child.getId(), "Child item must be persisted");
		final String childType = child.getClass().getSimpleName();
		((IParentChildRelationRepository) repository).deleteByChild(child.getId(), childType);
		child.clearParent();
		LOGGER.info("Cleared parent relationship for {}#{}", childType, child.getId());
	}

	/** Get all child items for a parent project item.
	 * @param parent the parent project item
	 * @return list of child project items */
	@Transactional (readOnly = true)
	public <T extends CProjectItem<?>> List<T> getChildren(final CProjectItem<?> parent) {
		Objects.requireNonNull(parent, "Parent item cannot be null");
		Objects.requireNonNull(parent.getId(), "Parent item must be persisted");
		final String parentType = parent.getClass().getSimpleName();
		final List<CParentChildRelation> relations = ((IParentChildRelationRepository) repository).findByParent(parent.getId(), parentType);
		return loadChildrenFromRelations(relations);
	}

	/** Get all child items for a parent project item filtered by child entity class. This is useful when you want only children of a specific type
	 * (e.g., only CActivity children).
	 * @param parent               the parent project item
	 * @param childEntityClassName the class name of children to retrieve (e.g., "CActivity")
	 * @return list of child project items matching the specified type */
	@Transactional (readOnly = true)
	public <T extends CProjectItem<?>> List<T> getChildrenByType(final CProjectItem<?> parent, final String childEntityClassName) {
		Objects.requireNonNull(parent, "Parent item cannot be null");
		Objects.requireNonNull(parent.getId(), "Parent item must be persisted");
		Check.notBlank(childEntityClassName, "Child entity class name cannot be blank");
		final String parentType = parent.getClass().getSimpleName();
		final List<CParentChildRelation> relations =
				((IParentChildRelationRepository) repository).findByParentAndChildType(parent.getId(), parentType, childEntityClassName);
		return loadChildrenFromRelations(relations);
	}

	@Override
	protected Class<CParentChildRelation> getEntityClass() { return CParentChildRelation.class; }

	/** Get the parent item for a child project item.
	 * @param child the child project item
	 * @return optional parent project item */
	@SuppressWarnings ("unchecked")
	@Transactional (readOnly = true)
	public <T extends CProjectItem<?>> Optional<T> getParent(final CProjectItem<?> child) {
		Objects.requireNonNull(child, "Child item cannot be null");
		Objects.requireNonNull(child.getId(), "Child item must be persisted");
		final String childType = child.getClass().getSimpleName();
		final Optional<CParentChildRelation> relation = ((IParentChildRelationRepository) repository).findByChild(child.getId(), childType);
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

	/** CParentChildRelation constructor requires specific parent/child IDs and types. Use specific creation methods with required parameters instead
	 * of newEntity().
	 * @throws UnsupportedOperationException always - use createRelation methods */
	@Override
	public CParentChildRelation newEntity() throws Exception {
		throw new UnsupportedOperationException("CParentChildRelation requires specific parent/child IDs and types in constructor. "
				+ "Use createRelation(childId, childType, parentId, parentType) or similar methods instead.");
	}

	/** Establish a parent-child relationship between two project items. This method validates that: - Both items are persisted (have IDs) - No
	 * circular dependency would be created - Parent type allows children (based on entity type configuration) - Items are not the same
	 * @param child  the child project item
	 * @param parent the parent project item
	 * @throws IllegalArgumentException if validation fails */
	@Transactional
	public void setParent(final CProjectItem<?> child, final CProjectItem<?> parent) {
		Objects.requireNonNull(child, "Child item cannot be null");
		Objects.requireNonNull(parent, "Parent item cannot be null");
		Objects.requireNonNull(child.getId(), "Child item must be persisted");
		Objects.requireNonNull(parent.getId(), "Parent item must be persisted");
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
		final Optional<CParentChildRelation> existingRelation = ((IParentChildRelationRepository) repository).findByChild(child.getId(), childType);
		existingRelation.ifPresent(repository::delete);
		// Create new relationship
		final CParentChildRelation newRelation = new CParentChildRelation(child.getId(), childType, parent.getId(), parentType);
		repository.save(newRelation);
		// Update the child's parent fields
		child.setParentId(parent.getId());
		child.setParentType(parentType);
		LOGGER.info("Established parent-child relationship: {}#{} -> {}#{}", parentType, parent.getId(), childType, child.getId());
	}

	/** Check if setting a parent would create a circular dependency. This checks if the proposed parent is already a descendant of the child.
	 * @param parentId   ID of the proposed parent
	 * @param parentType class name of the proposed parent
	 * @param childId    ID of the child
	 * @param childType  class name of the child
	 * @return true if circular dependency would be created */
	@Transactional (readOnly = true)
	public boolean wouldCreateCircularDependency(final Long parentId, final String parentType, final Long childId, final String childType) {
		Objects.requireNonNull(parentId, "Parent ID cannot be null");
		Check.notBlank(parentType, "Parent type cannot be blank");
		Objects.requireNonNull(childId, "Child ID cannot be null");
		Check.notBlank(childType, "Child type cannot be blank");
		// Check if parent is a descendant of child
		final List<Object[]> descendants = ((IParentChildRelationRepository) repository).findAllDescendants(childId, childType);
		for (final Object[] desc : descendants) {
			final Long descId = ((Number) desc[0]).longValue();
			final String descType = (String) desc[1];
			if (descId.equals(parentId) && descType.equals(parentType)) {
				return true;
			}
		}
		return false;
	}
}
