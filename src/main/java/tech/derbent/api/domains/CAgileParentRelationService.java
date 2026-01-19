package tech.derbent.api.domains;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasAgileParentRelation;
import tech.derbent.api.ui.component.CComponentAgileParentSelector;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.base.session.service.ISessionService;

/** Service for managing agile parent relations in hierarchies. Provides methods for establishing, removing, and querying hierarchical relationships
 * with validation for circular dependencies and proper hierarchy management.
 * <p>
 * This service works with any entity that implements IHasAgileParentRelation (Activities, Meetings, Issues, etc.).
 * </p>
 */
@Service
public class CAgileParentRelationService extends COneToOneRelationServiceBase<CAgileParentRelation> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAgileParentRelationService.class);
	private final CActivityService activityService;

	/** Create a default agile parent relation for new entities. This is called during entity initialization.
	 * @return a new CAgileParentRelation with default values */
	public static CAgileParentRelation createDefaultAgileParentRelation() {
		return new CAgileParentRelation();
	}

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
		CActivity current = hasRelation.getParentActivity();
		final Set<Long> visited = new HashSet<>();
		while (current != null && current.getId() != null) {
			// Prevent infinite loops from circular references
			if (visited.contains(current.getId())) {
				LOGGER.warn("Circular reference detected in hierarchy for entity '{}'", entity.getName());
				break;
			}
			visited.add(current.getId());
			depth++;
			// Get parent of current activity
			// CActivity always implements IHasAgileParentRelation, so cast is safe
			final IHasAgileParentRelation currentRelation = current;
			current = currentRelation.getParentActivity();
		}
		return depth;
	}

	public CAgileParentRelationService(final IAgileParentRelationRepository repository, final Clock clock, 
			final ISessionService sessionService, final CActivityService activityService) {
		super(repository, clock, sessionService);
		this.activityService = activityService;
	}

	/** Clear the parent activity for an entity, making it a root item.
	 * @param entity the entity (must implement IHasAgileParentRelation) */
	@Transactional
	public void clearParent(final CProjectItem<?> entity) {
		validateOwnership(entity, IHasAgileParentRelation.class);
		final IHasAgileParentRelation hasRelation = (IHasAgileParentRelation) entity;
		Check.notNull(hasRelation.getAgileParentRelation(), "Entity must have an agile parent relation");
		hasRelation.getAgileParentRelation().setParentActivity(null);
	}

	/** Helper method to recursively collect all descendants.
	 * @param activity    the current activity
	 * @param descendants accumulator for descendants
	 * @param visited     set of visited IDs to prevent infinite loops */
	private void collectDescendants(final CActivity activity, final List<CProjectItem<?>> descendants, final Set<Long> visited) {
		final List<CAgileParentRelation> childRelations = ((IAgileParentRelationRepository) repository).findChildrenByParentId(activity.getId());
		for (final CAgileParentRelation relation : childRelations) {
			final CProjectItem<?> child = relation.getOwnerItem();
			if (child != null && !visited.contains(child.getId())) {
				visited.add(child.getId());
				descendants.add(child);
				// If child is also an activity, recurse
				if (child instanceof CActivity) {
					collectDescendants((CActivity) child, descendants, visited);
				}
			}
		}
	}

	/** Get all descendants (children, grandchildren, etc.) for an activity. Uses recursive traversal to find all items in the subtree.
	 * @param activity the root activity
	 * @return list of all descendant entities */
	@Transactional (readOnly = true)
	public List<CProjectItem<?>> getAllDescendants(final CActivity activity) {
		Check.notNull(activity, "Activity cannot be null");
		Check.notNull(activity.getId(), "Activity must be persisted");
		final List<CProjectItem<?>> descendants = new ArrayList<>();
		final Set<Long> visited = new HashSet<>();
		collectDescendants(activity, descendants, visited);
		return descendants;
	}

	/** Get all child entities for a parent activity. Returns the owner entities (Activities, Meetings, etc.) that have this activity as parent.
	 * @param parent the parent activity
	 * @return list of child entities */
	@Transactional (readOnly = true)
	public List<CProjectItem<?>> getChildren(final CActivity parent) {
		Check.notNull(parent, "Parent activity cannot be null");
		Check.notNull(parent.getId(), "Parent activity must be persisted");
		final List<CAgileParentRelation> relations = ((IAgileParentRelationRepository) repository).findChildrenByParentId(parent.getId());
		final List<CProjectItem<?>> children = new ArrayList<>();
		for (final CAgileParentRelation relation : relations) {
			final CProjectItem<?> child = relation.getOwnerItem();
			if (child != null) {
				children.add(child);
			} else {
				LOGGER.warn("Agile parent relation {} has no owner item", relation.getId());
			}
		}
		return children;
	}

	@Override
	protected Class<CAgileParentRelation> getEntityClass() { return CAgileParentRelation.class; }

	/** Get the parent activity for an entity.
	 * @param entity the entity (must implement IHasAgileParentRelation)
	 * @return the parent activity, or null if none */
	@Transactional (readOnly = true)
	public CActivity getParent(final CProjectItem<?> entity) {
		Check.notNull(entity, "Entity cannot be null");
		if (entity instanceof IHasAgileParentRelation) {
			final IHasAgileParentRelation hasRelation = (IHasAgileParentRelation) entity;
			Check.notNull(hasRelation.getAgileParentRelation(), "Entity must have an agile parent relation");
			return hasRelation.getAgileParentRelation().getParentActivity();
		}
		return null;
	}

	/** Get all root entities (those without a parent).
	 * @return list of root entities */
	@Transactional (readOnly = true)
	public List<CProjectItem<?>> getRootItems() {
		final List<CAgileParentRelation> relations = ((IAgileParentRelationRepository) repository).findRootItems();
		final List<CProjectItem<?>> roots = new ArrayList<>();
		for (final CAgileParentRelation relation : relations) {
			final CProjectItem<?> entity = relation.getOwnerItem();
			if (entity != null) {
				roots.add(entity);
			}
		}
		return roots;
	}

	/** Set the parent activity for an entity. Validates that this won't create a circular dependency.
	 * @param entity the entity (must implement IHasAgileParentRelation)
	 * @param parent the parent activity (null to make root item) */
	@Transactional
	public void setParent(final CProjectItem<?> entity, final CActivity parent) {
		validateOwnership(entity, IHasAgileParentRelation.class);
		final IHasAgileParentRelation hasRelation = (IHasAgileParentRelation) entity;
		Check.notNull(hasRelation.getAgileParentRelation(), "Entity must have an agile parent relation");
		// Allow null parent (makes it a root item)
		if (parent != null) {
			Check.notNull(parent.getId(), "Parent activity must be persisted");
			// Prevent self-reference (for activities that can parent themselves)
			if (entity instanceof CActivity) {
				validateNotSelfReference(entity.getId(), parent.getId(), "An activity cannot be its own parent");
			}
			// Check for circular dependency
			if (wouldCreateCircularDependency(parent, entity)) {
				throw new IllegalArgumentException("Setting this parent would create a circular dependency");
			}
			// Validate same project
			validateSameProject(entity, parent);
		}
		final CActivity previousParent = hasRelation.getAgileParentRelation().getParentActivity();
		hasRelation.getAgileParentRelation().setParentActivity(parent);
		// Entity will be saved by caller
		if (parent != null) {
			LOGGER.info("Established parent-child relationship: '{}' -> '{}'", parent.getName(), entity.getName());
		} else if (previousParent != null) {
			LOGGER.info("Cleared parent '{}' from child '{}'", previousParent.getName(), entity.getName());
		}
	}

	/** Check if setting a parent would create a circular dependency. This checks if the proposed parent is already a descendant of the child.
	 * @param parent the proposed parent activity
	 * @param child  the child entity
	 * @return true if circular dependency would be created */
	@Transactional (readOnly = true)
	public boolean wouldCreateCircularDependency(final CActivity parent, final CProjectItem<?> child) {
		Check.notNull(parent, "Parent activity cannot be null");
		Check.notNull(child, "Child entity cannot be null");
		Check.notNull(parent.getId(), "Parent ID cannot be null");
		Check.notNull(child.getId(), "Child ID cannot be null");
		// Only activities can be parents, so only check if child is an activity
		if (!(child instanceof CActivity)) {
			return false; // Non-activities can't have descendants that are activities
		}
		// Check if parent is a descendant of child
		final List<Long> descendantIds = ((IAgileParentRelationRepository) repository).findAllDescendantIds(child.getId());
		return descendantIds.contains(parent.getId());
	}

	/**
	 * Create an agile parent selector component for selecting parent activities.
	 * Called by component factory via @AMetaData createComponentMethod.
	 * This component is used in entity detail forms to allow users to select a parent
	 * activity for establishing agile hierarchy relationships (Epic → User Story → Task, etc.).
	 * 
	 * <p>The component provides:</p>
	 * <ul>
	 * <li>Filtering by project (only activities in same project)</li>
	 * <li>Excluding the current entity (prevent self-parenting)</li>
	 * <li>Hierarchical display with activity type indication</li>
	 * <li>Circular dependency prevention</li>
	 * </ul>
	 * 
	 * @return the agile parent selector component
	 */
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
}
