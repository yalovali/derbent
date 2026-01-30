package tech.derbent.api.agileparentrelation.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.agileparentrelation.domain.CAgileParentRelation;
import tech.derbent.api.domains.COneToOneRelationServiceBase;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasAgileParentRelation;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.ui.component.CComponentAgileParentSelector;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;

/** Service for managing agile parent relations in hierarchies. Provides methods for establishing, removing, and querying hierarchical relationships
 * with validation for circular dependencies and proper hierarchy management.
 * <p>
 * This service works with any entity that implements IHasAgileParentRelation (Activities, Meetings, Issues, etc.).
 * </p>
 */
@Service
@Profile ("derbent")
public class CAgileParentRelationService extends COneToOneRelationServiceBase<CAgileParentRelation> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAgileParentRelationService.class);

	/** Get the depth level of an entity in the hierarchy. Root items have depth 0, their children have depth 1, etc.
	 * @param entity the entity (must implement IHasAgileParentRelation)
	 * @return the depth level (0 for root items) */
	@Transactional (readOnly = true)
	public static int getDepth(final CProjectItem<?> entity) {
		Check.notNull(entity, "Entity cannot be null");
		if (!(entity instanceof IHasAgileParentRelation)) {
			return 0;
		}
		final IHasAgileParentRelation hasRelation = (IHasAgileParentRelation) entity;
		int depth = 0;
		CProjectItem<?> current = hasRelation.getParentItem();
		final Set<Long> visited = new HashSet<>();
		while (current != null && current.getId() != null) {
			// Prevent infinite loops from circular references
			if (visited.contains(current.getId())) {
				LOGGER.warn("Circular reference detected in hierarchy for entity '{}'", entity.getName());
				break;
			}
			visited.add(current.getId());
			depth++;
			// Get parent of current item if it implements IHasAgileParentRelation
			if (current instanceof IHasAgileParentRelation) {
				final IHasAgileParentRelation currentRelation = (IHasAgileParentRelation) current;
				current = currentRelation.getParentItem();
			} else {
				break;
			}
		}
		return depth;
	}

	private final CActivityService activityService;

	public CAgileParentRelationService(final IAgileParentRelationRepository repository, final Clock clock, final ISessionService sessionService,
			final CActivityService activityService) {
		super(repository, clock, sessionService);
		this.activityService = activityService;
	}

	/** Clear the parent item for an entity, making it a root item.
	 * @param entity the entity (must implement IHasAgileParentRelation) */
	@Transactional
	public void clearParent(final CProjectItem<?> entity) {
		validateOwnership(entity, IHasAgileParentRelation.class);
		final IHasAgileParentRelation hasRelation = (IHasAgileParentRelation) entity;
		Check.notNull(hasRelation.getAgileParentRelation(), "Entity must have an agile parent relation");
		hasRelation.getAgileParentRelation().setParentItem(null);
	}

	/** Helper method to recursively collect all descendants.
	 * @param item        the current item
	 * @param descendants accumulator for descendants
	 * @param visited     set of visited IDs to prevent infinite loops */
	private void collectDescendants(final CProjectItem<?> item, final List<CProjectItem<?>> descendants, final Set<Long> visited) {
		final List<CAgileParentRelation> childRelations = ((IAgileParentRelationRepository) repository).findChildrenByParentId(item.getId());
		childRelations.forEach((final CAgileParentRelation relation) -> {
			final CProjectItem<?> child = relation.getOwnerItem();
			if (child != null && !visited.contains(child.getId())) {
				visited.add(child.getId());
				descendants.add(child);
				// Recurse for any child that implements IHasAgileParentRelation
				if (child instanceof IHasAgileParentRelation) {
					collectDescendants(child, descendants, visited);
				}
			}
		});
	}

	/** Create an agile parent selector component for selecting parent activities. Called by component factory via @AMetaData createComponentMethod.
	 * This component is used in entity detail forms to allow users to select a parent activity for establishing agile hierarchy relationships (Epic →
	 * User Story → Task, etc.).
	 * <p>
	 * The component provides:
	 * </p>
	 * <ul>
	 * <li>Filtering by project (only activities in same project)</li>
	 * <li>Excluding the current entity (prevent self-parenting)</li>
	 * <li>Hierarchical display with activity type indication</li>
	 * <li>Circular dependency prevention</li>
	 * </ul>
	 * @return the agile parent selector component */
	public Component createComponent() {
		try {
			final CComponentAgileParentSelector component = new CComponentAgileParentSelector(activityService, this);
			LOGGER.debug("Created agile parent selector component");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create agile parent selector component.", e);
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading agile parent selector component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	/** Get all descendants (children, grandchildren, etc.) for an item. Uses recursive traversal to find all items in the subtree.
	 * @param item the root item
	 * @return list of all descendant entities */
	@Transactional (readOnly = true)
	public List<CProjectItem<?>> getAllDescendants(final CProjectItem<?> item) {
		Check.notNull(item, "Item cannot be null");
		Check.notNull(item.getId(), "Item must be persisted");
		final List<CProjectItem<?>> descendants = new ArrayList<>();
		final Set<Long> visited = new HashSet<>();
		collectDescendants(item, descendants, visited);
		return descendants;
	}

	/** Get all child entities for a parent item. Returns the owner entities (Activities, Epics, Features, etc.) that have this item as parent.
	 * @param parent the parent item
	 * @return list of child entities */
	@Transactional (readOnly = true)
	public List<CProjectItem<?>> getChildren(final CProjectItem<?> parent) {
		Check.notNull(parent, "Parent item cannot be null");
		Check.notNull(parent.getId(), "Parent item must be persisted");
		final List<CAgileParentRelation> relations = ((IAgileParentRelationRepository) repository).findChildrenByParentId(parent.getId());
		final List<CProjectItem<?>> children = new ArrayList<>();
		relations.forEach((final CAgileParentRelation relation) -> {
			final CProjectItem<?> child = relation.getOwnerItem();
			if (child != null) {
				children.add(child);
			} else {
				LOGGER.warn("Agile parent relation {} has no owner item", relation.getId());
			}
		});
		return children;
	}

	@Override
	public Class<CAgileParentRelation> getEntityClass() { return CAgileParentRelation.class; }

	@Override
	public Class<?> getPageServiceClass() { return null; }

	/** Get the parent item for an entity.
	 * @param entity the entity (must implement IHasAgileParentRelation)
	 * @return the parent item, or null if none */
	@Transactional (readOnly = true)
	public CProjectItem<?> getParent(final CProjectItem<?> entity) {
		Check.notNull(entity, "Entity cannot be null");
		if (!(entity instanceof IHasAgileParentRelation)) {
			return null;
		}
		final IHasAgileParentRelation hasRelation = (IHasAgileParentRelation) entity;
		Check.notNull(hasRelation.getAgileParentRelation(), "Entity must have an agile parent relation");
		return hasRelation.getAgileParentRelation().getParentItem();
	}

	/** Get all root entities (those without a parent).
	 * @return list of root entities */
	@Transactional (readOnly = true)
	public List<CProjectItem<?>> getRootItems() {
		final List<CAgileParentRelation> relations = ((IAgileParentRelationRepository) repository).findRootItems();
		final List<CProjectItem<?>> roots = new ArrayList<>();
		relations.forEach((final CAgileParentRelation relation) -> {
			final CProjectItem<?> entity = relation.getOwnerItem();
			if (entity != null) {
				roots.add(entity);
			}
		});
		return roots;
	}

	@Override
	public Class<?> getServiceClass() { return CAgileParentRelationService.class; }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	public CAgileParentRelation newEntity() throws Exception {
		// CAgileParentRelation requires ownerItem in constructor
		// For new entity creation without owner, pass null
		// (ownerItem will be set later by the parent entity)
		return new CAgileParentRelation(null);
	}

	/** Set the parent item for an entity. Validates that this won't create a circular dependency.
	 * @param entity the entity (must implement IHasAgileParentRelation)
	 * @param parent the parent item (must be Epic, Feature, or UserStory), or null to make root item */
	@Transactional
	public void setParent(final CProjectItem<?> entity, final CProjectItem<?> parent) {
		validateOwnership(entity, IHasAgileParentRelation.class);
		final IHasAgileParentRelation hasRelation = (IHasAgileParentRelation) entity;
		Check.notNull(hasRelation.getAgileParentRelation(), "Entity must have an agile parent relation");
		// Allow null parent (makes it a root item)
		if (parent != null) {
			Check.notNull(parent.getId(), "Parent item must be persisted");
			// Prevent self-reference
			validateNotSelfReference(entity.getId(), parent.getId(), "An entity cannot be its own parent");
			// Validate parent type based on agile hierarchy rules
			validateParentType(entity, parent);
			// Check for circular dependency
			if (wouldCreateCircularDependency(parent, entity)) {
				throw new IllegalArgumentException("Setting this parent would create a circular dependency");
			}
			// Validate same project
			validateSameProject(entity, parent);
		} else {
			// Validate that Epic is the only entity allowed to have null parent
			if (!(entity instanceof CEpic)) {
				LOGGER.warn("Entity {} with ID {} has null parent but is not an Epic. This may be intentional for root items.",
						entity.getClass().getSimpleName(), entity.getId());
			}
		}
		final CProjectItem<?> previousParent = hasRelation.getAgileParentRelation().getParentItem();
		hasRelation.getAgileParentRelation().setParentItem(parent);
		// Entity will be saved by caller
		if (parent != null) {
			LOGGER.info("Established parent-child relationship: '{}' -> '{}'", parent.getName(), entity.getName());
		} else if (previousParent != null) {
			LOGGER.info("Cleared parent '{}' from child '{}'", previousParent.getName(), entity.getName());
		}
	}

	/** Validate that the parent type is allowed for the given entity type based on agile hierarchy rules.
	 * <p>
	 * Hierarchy rules:
	 * </p>
	 * <ul>
	 * <li>Epic: Cannot have a parent (must be root level)</li>
	 * <li>Feature: Can only have an Epic as parent</li>
	 * <li>UserStory: Can only have a Feature as parent</li>
	 * <li>Other entities (Activity, Meeting, Risk, etc.): Can only have a UserStory as parent</li>
	 * </ul>
	 * @param entity the entity
	 * @param parent the proposed parent (null allowed)
	 * @throws IllegalArgumentException if parent type violates hierarchy rules */
	private void validateParentType(final CProjectItem<?> entity, final CProjectItem<?> parent) {
		// Null parent is always allowed (makes entity a root item)
		if (parent == null) {
			return;
		}
		// Epic cannot have a parent
		if (entity instanceof CEpic) {
			throw new IllegalArgumentException("Epic cannot have a parent - it must be at the top level of the hierarchy");
		}
		// Feature can only have Epic as parent
		if (entity instanceof CFeature) {
			if (!(parent instanceof CEpic)) {
				throw new IllegalArgumentException("Feature can only have an Epic as parent");
			}
			return;
		}
		// UserStory can only have Feature as parent
		if (entity instanceof CUserStory) {
			if (!(parent instanceof CFeature)) {
				throw new IllegalArgumentException("User Story can only have a Feature as parent");
			}
			return;
		}
		// Other entities (Activity, Meeting, Risk, etc.) can only have UserStory as parent
		// This is the IAgileMember level
		if (!(parent instanceof CUserStory)) {
			throw new IllegalArgumentException(entity.getClass().getSimpleName() + " can only have a User Story as parent. "
					+ "Agile hierarchy is: Epic → Feature → User Story → " + entity.getClass().getSimpleName());
		}
	}

	/** Check if setting a parent would create a circular dependency. This checks if the proposed parent is already a descendant of the child.
	 * @param parent the proposed parent item
	 * @param child  the child entity
	 * @return true if circular dependency would be created */
	@Transactional (readOnly = true)
	public boolean wouldCreateCircularDependency(final CProjectItem<?> parent, final CProjectItem<?> child) {
		Check.notNull(parent, "Parent item cannot be null");
		Check.notNull(child, "Child entity cannot be null");
		Check.notNull(parent.getId(), "Parent ID cannot be null");
		Check.notNull(child.getId(), "Child ID cannot be null");
		// Check if parent is a descendant of child (any entity can be a parent now)
		final List<Long> descendantIds = ((IAgileParentRelationRepository) repository).findAllDescendantIds(child.getId());
		return descendantIds.contains(parent.getId());
	}
}
