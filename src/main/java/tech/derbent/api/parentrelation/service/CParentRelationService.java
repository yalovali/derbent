package tech.derbent.api.parentrelation.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.COneToOneRelationServiceBase;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasParentRelation;
import tech.derbent.api.parentrelation.domain.CParentRelation;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;

/**
 * Service for managing parent relations in hierarchies. Provides methods for establishing,
 * removing, and querying hierarchical relationships with validation for circular dependencies and
 * proper hierarchy management using level-based rules.
 *
 * <p>
 * KEYWORDS: HierarchyRelation, CParentRelation, setParent, clearParent, listChildren,
 * validateParentType, level-based-validation
 * </p>
 * <p>
 * Level semantics:
 * <ul>
 * <li>Level 0 = top (e.g. Epic) - cannot have a parent</li>
 * <li>Level N &gt; 0 = intermediate (e.g. Feature=1, UserStory=2) - parent must have level N-1</li>
 * <li>Level -1 = leaf (Activity, Meeting, Risk, etc.) - parent can be any non-leaf entity</li>
 * </ul>
 * </p>
 */
@Service
@Primary
@Profile({"derbent", "default", "test"})
public class CParentRelationService extends COneToOneRelationServiceBase<CParentRelation>
        implements IEntityRegistrable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CParentRelationService.class);
    private final CHierarchyNavigationService hierarchyNavigationService;

    public CParentRelationService(final IParentRelationRepository repository, final Clock clock,
            final ISessionService sessionService, final CHierarchyNavigationService hierarchyNavigationService) {
        super(repository, clock, sessionService);
        this.hierarchyNavigationService = hierarchyNavigationService;
    }

    /**
     * Get the level of an entity based on its type. Returns -1 for leaf entities or entities
     * without a typed level.
     *
     * @param  entity the entity
     * @return        the level from entity type, or -1 if not determinable
     */
    public static int getEntityLevel(final CProjectItem<?> entity) {
        return CHierarchyNavigationService.getEntityLevel(entity);
    }

    /**
     * Get the depth level of an entity in the hierarchy (how deep it is from root).
     *
     * @param  entity the entity (must implement IHasParentRelation)
     * @return        the depth level (0 for root items)
     */
    @Transactional(readOnly = true)
    public static int getDepth(final CProjectItem<?> entity) {
        Check.notNull(entity, "Entity cannot be null");
        if (!(entity instanceof IHasParentRelation hasRelation)) {
            return 0;
        }
        int depth = 0;
        CProjectItem<?> current = hasRelation.getParentItem();
        final Set<Long> visited = new HashSet<>();
        while (current != null && current.getId() != null) {
            if (visited.contains(current.getId())) {
                LOGGER.warn("Circular reference detected in hierarchy for entity '{}'",
                        entity.getName());
                break;
            }
            visited.add(current.getId());
            depth++;
            if (current instanceof IHasParentRelation currentRelation) {
                current = currentRelation.getParentItem();
            } else {
                break;
            }
        }
        return depth;
    }

    /**
     * Clear the parent item for an entity, making it a root item.
     *
     * @param entity the entity (must implement IHasParentRelation)
     */
    @Transactional
    public void clearParent(final CProjectItem<?> entity) {
        validateOwnership(entity, IHasParentRelation.class);
        final IHasParentRelation hasRelation = (IHasParentRelation) entity;
        Check.notNull(hasRelation.getParentRelation(), "Entity must have a parent relation");
        hasRelation.getParentRelation().setParentItem(null);
        LOGGER.info("Cleared parent for entity '{}'", entity.getName());
    }

    /**
     * Get all descendants (children, grandchildren, etc.) for an item.
     *
     * @param  item the root item
     * @return      list of all descendant entities
     */
    @Transactional(readOnly = true)
    public List<CProjectItem<?>> getAllDescendants(final CProjectItem<?> item) {
        Check.notNull(item, "Item cannot be null");
        Check.notNull(item.getId(), "Item must be persisted");
        return hierarchyNavigationService.getAllDescendants(item);
    }

    /**
     * Get all child entities for a parent item.
     *
     * @param  parent the parent item
     * @return        list of child entities
     */
    @Transactional(readOnly = true)
    public List<CProjectItem<?>> getChildren(final CProjectItem<?> parent) {
        Check.notNull(parent, "Parent item cannot be null");
        Check.notNull(parent.getId(), "Parent item must be persisted");
        return hierarchyNavigationService.listChildren(parent);
    }

    @Override
    public Class<CParentRelation> getEntityClass() {
        return CParentRelation.class;
    }

    @Override
    public Class<?> getPageServiceClass() {
        return null;
    }

    /**
     * Get the parent item for an entity.
     *
     * @param  entity the entity (must implement IHasParentRelation)
     * @return        the parent item, or null if none
     */
    @Transactional(readOnly = true)
    public CProjectItem<?> getParent(final CProjectItem<?> entity) {
        Check.notNull(entity, "Entity cannot be null");
        if (!(entity instanceof IHasParentRelation)) {
            return null;
        }
        final IHasParentRelation hasRelation = (IHasParentRelation) entity;
        if (hasRelation.getParentRelation() == null) {
            return null;
        }
        return hasRelation.getParentRelation().getParentItem();
    }

    /**
     * Get all root entities (those without a parent).
     *
     * @return list of root entities
     */
    @Transactional(readOnly = true)
    public List<CProjectItem<?>> getRootItems() {
        final List<CParentRelation> relations =
                ((IParentRelationRepository) repository).findRootItems();
        final List<CProjectItem<?>> roots = new ArrayList<>();
        relations.forEach((final CParentRelation relation) -> {
            final CProjectItem<?> entity = relation.getOwnerItem();
            if (entity != null) {
                roots.add(entity);
            }
        });
        return roots;
    }

    @Override
    public Class<?> getServiceClass() {
        return CParentRelationService.class;
    }

    @Override
    public void initializeNewEntity(final Object entity) {
        super.initializeNewEntity(entity);
    }

    @Override
    public CParentRelation newEntity() throws Exception {
        return new CParentRelation(null);
    }

    /**
     * Set the parent item for an entity. Validates hierarchy levels and circular dependencies.
     *
     * @param entity the entity (must implement IHasParentRelation)
     * @param parent the parent item, or null to make root item
     */
    @Transactional
    public void setParent(final CProjectItem<?> entity, final CProjectItem<?> parent) {
        validateOwnership(entity, IHasParentRelation.class);
        final IHasParentRelation hasRelation = (IHasParentRelation) entity;
        Check.notNull(hasRelation.getParentRelation(), "Entity must have a parent relation");
        if (parent != null) {
            Check.notNull(parent.getId(), "Parent item must be persisted");
            Check.notNull(entity.getProject(), "Child project must be set before assigning parent");
            Check.notNull(parent.getProject(), "Parent project must be set before assigning parent");
            Check.isTrue(entity.getProject().equals(parent.getProject()),
                    "Parent and child must belong to the same project");
            validateNotSelfReference(entity.getId(), parent.getId(),
                    "An entity cannot be its own parent");
            validateParentType(entity, parent);
            if (wouldCreateCircularDependency(parent, entity)) {
                throw new IllegalArgumentException(
                        "Setting this parent would create a circular dependency");
            }
        } else {
            final int entityLevel = getEntityLevel(entity);
            if (entityLevel != 0 && entityLevel != -1) {
                LOGGER.debug(
                        "Entity {} with ID {} has null parent but is not at top level (level={}). This may be intentional.",
                        entity.getClass().getSimpleName(), entity.getId(), entityLevel);
            }
        }
        final CProjectItem<?> previousParent = hasRelation.getParentRelation().getParentItem();
        hasRelation.getParentRelation().setParentItem(parent);
        if (parent != null) {
            LOGGER.info("Established parent-child relationship: '{}' -> '{}'", parent.getName(),
                    entity.getName());
        } else if (previousParent != null) {
            LOGGER.info("Cleared parent '{}' from child '{}'", previousParent.getName(),
                    entity.getName());
        }
    }

    /**
     * Validate that the parent type is allowed for the given entity based on level-based hierarchy
     * rules.
     *
     * @param  entity                    the entity
     * @param  parent                    the proposed parent
     * @throws IllegalArgumentException if parent type violates hierarchy rules
     */
    protected void validateParentType(final CProjectItem<?> entity, final CProjectItem<?> parent) {
        if (parent == null) {
            return;
        }
        final int childLevel = getEntityLevel(entity);
        final int parentLevel = getEntityLevel(parent);
        if (!CHierarchyNavigationService.canHaveChildren(parent)) {
            throw new IllegalArgumentException(
                    "Parent type '%s' does not allow child items for the current hierarchy configuration."
                            .formatted(parent.getClass().getSimpleName()));
        }
        if (childLevel == 0) {
            throw new IllegalArgumentException(
                    "Top-level entities (level=0) cannot have a parent. '"
                            + entity.getClass().getSimpleName() + "' is at the top of the hierarchy.");
        }
        if (childLevel > 0 && parentLevel != childLevel - 1) {
            throw new IllegalArgumentException(
                    entity.getClass().getSimpleName() + " (level=" + childLevel
                            + ") requires a parent at level " + (childLevel - 1) + ", but '"
                            + parent.getClass().getSimpleName() + "' has level " + parentLevel + ".");
        }
    }

    /**
     * Check whether setting a parent would create a circular dependency.
     *
     * @param  parent the proposed parent item
     * @param  child  the child entity
     * @return        true if circular dependency would be created
     */
    @Transactional(readOnly = true)
    public boolean wouldCreateCircularDependency(final CProjectItem<?> parent,
            final CProjectItem<?> child) {
        Check.notNull(parent, "Parent item cannot be null");
        Check.notNull(child, "Child entity cannot be null");
        Check.notNull(parent.getId(), "Parent ID cannot be null");
        Check.notNull(child.getId(), "Child ID cannot be null");
        final String childKey = child.getClass().getSimpleName() + ":" + child.getId();
        CProjectItem<?> current = parent;
        final Set<String> visited = new HashSet<>();
        while (current != null && current.getId() != null) {
            final String currentKey = current.getClass().getSimpleName() + ":" + current.getId();
            if (visited.contains(currentKey)) {
                LOGGER.warn("Circular reference detected while checking hierarchy (already visited {})",
                        currentKey);
                return true;
            }
            if (childKey.equals(currentKey)) {
                return true;
            }
            if (!(current instanceof IHasParentRelation currentRelation)) {
                return false;
            }
            visited.add(currentKey);
            current = currentRelation.getParentItem();
        }
        return false;
    }
}
