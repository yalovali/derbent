package tech.derbent.app.activities.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.domain.CActivityParentRelation;
import tech.derbent.base.session.service.ISessionService;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for managing activity parent relations in agile hierarchies.
 * Provides methods for establishing, removing, and querying hierarchical relationships
 * with validation for circular dependencies and proper hierarchy management.
 */
@Service
public class CActivityParentRelationService extends CAbstractService<CActivityParentRelation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityParentRelationService.class);
    private final IActivityParentRelationRepository repository;
    private final CActivityService activityService;

    public CActivityParentRelationService(
            final IActivityParentRelationRepository repository,
            final Clock clock,
            final ISessionService sessionService,
            final CActivityService activityService) {
        super(repository, clock, sessionService);
        this.repository = repository;
        this.activityService = activityService;
    }

    /**
     * Clear the parent activity for a child, making it a root item.
     * 
     * @param child the child activity
     */
    @Transactional
    public void clearParent(final CActivity child) {
        Check.notNull(child, "Child activity cannot be null");
        Check.notNull(child.getId(), "Child activity must be persisted");
        Check.notNull(child.getParentRelation(), "Child must have a parent relation");

        child.getParentRelation().setParentActivity(null);
        activityService.save(child);
        
        LOGGER.info("Cleared parent relationship for activity '{}' (ID: {})", child.getName(), child.getId());
    }

    /**
     * Create a default parent relation for new activities.
     * This is called during activity initialization.
     * 
     * @return a new CActivityParentRelation with default values
     */
    public static CActivityParentRelation createDefaultParentRelation() {
        return new CActivityParentRelation();
    }

    /**
     * Get all child activities for a parent activity.
     * 
     * @param parent the parent activity
     * @return list of child activities
     */
    @Transactional(readOnly = true)
    public List<CActivity> getChildren(final CActivity parent) {
        Check.notNull(parent, "Parent activity cannot be null");
        Check.notNull(parent.getId(), "Parent activity must be persisted");

        final List<CActivityParentRelation> relations = repository.findChildrenByParentId(parent.getId());
        final List<CActivity> children = new ArrayList<>();

        for (final CActivityParentRelation relation : relations) {
            final CActivity child = (CActivity) relation.getOwnerItem();
            if (child != null) {
                children.add(child);
            } else {
                LOGGER.warn("Parent relation {} has no owner item", relation.getId());
            }
        }

        return children;
    }

    /**
     * Get all descendants (children, grandchildren, etc.) for an activity.
     * Uses recursive traversal to find all items in the subtree.
     * 
     * @param activity the root activity
     * @return list of all descendant activities
     */
    @Transactional(readOnly = true)
    public List<CActivity> getAllDescendants(final CActivity activity) {
        Check.notNull(activity, "Activity cannot be null");
        Check.notNull(activity.getId(), "Activity must be persisted");

        final List<CActivity> descendants = new ArrayList<>();
        final Set<Long> visited = new HashSet<>();
        collectDescendants(activity, descendants, visited);
        
        return descendants;
    }

    /**
     * Get the depth level of an activity in the hierarchy.
     * Root items have depth 0, their children have depth 1, etc.
     * 
     * @param activity the activity
     * @return the depth level (0 for root items)
     */
    @Transactional(readOnly = true)
    public int getDepth(final CActivity activity) {
        Check.notNull(activity, "Activity cannot be null");
        
        int depth = 0;
        CActivity current = activity;
        final Set<Long> visited = new HashSet<>();
        
        while (current.hasParentActivity()) {
            final CActivity parent = current.getParentActivity();
            if (parent == null || parent.getId() == null) {
                break;
            }
            
            // Prevent infinite loops from circular references
            if (visited.contains(parent.getId())) {
                LOGGER.warn("Circular reference detected in hierarchy for activity '{}'", activity.getName());
                break;
            }
            
            visited.add(parent.getId());
            depth++;
            current = parent;
        }
        
        return depth;
    }

    @Override
    protected Class<CActivityParentRelation> getEntityClass() {
        return CActivityParentRelation.class;
    }

    /**
     * Get the parent activity for a child activity.
     * 
     * @param child the child activity
     * @return the parent activity, or null if none
     */
    @Transactional(readOnly = true)
    public CActivity getParent(final CActivity child) {
        Check.notNull(child, "Child activity cannot be null");
        Check.notNull(child.getParentRelation(), "Child must have a parent relation");

        return child.getParentRelation().getParentActivity();
    }

    /**
     * Get all root activities (those without a parent).
     * 
     * @return list of root activities
     */
    @Transactional(readOnly = true)
    public List<CActivity> getRootActivities() {
        final List<CActivityParentRelation> relations = repository.findRootItems();
        final List<CActivity> roots = new ArrayList<>();

        for (final CActivityParentRelation relation : relations) {
            final CActivity activity = (CActivity) relation.getOwnerItem();
            if (activity != null) {
                roots.add(activity);
            }
        }

        return roots;
    }

    /**
     * Set the parent activity for a child activity.
     * Validates that this won't create a circular dependency.
     * 
     * @param child the child activity
     * @param parent the parent activity (null to make root item)
     */
    @Transactional
    public void setParent(final CActivity child, final CActivity parent) {
        Check.notNull(child, "Child activity cannot be null");
        Check.notNull(child.getId(), "Child activity must be persisted");
        Check.notNull(child.getParentRelation(), "Child must have a parent relation");

        // Allow null parent (makes it a root item)
        if (parent != null) {
            Check.notNull(parent.getId(), "Parent activity must be persisted");

            // Prevent self-reference
            if (child.getId().equals(parent.getId())) {
                throw new IllegalArgumentException("An activity cannot be its own parent");
            }

            // Check for circular dependency
            if (wouldCreateCircularDependency(parent, child)) {
                throw new IllegalArgumentException("Setting this parent would create a circular dependency");
            }

            // Validate same project
            if (!child.getProject().getId().equals(parent.getProject().getId())) {
                throw new IllegalArgumentException("Parent and child must belong to the same project");
            }
        }

        final CActivity previousParent = child.getParentRelation().getParentActivity();
        child.getParentRelation().setParentActivity(parent);
        activityService.save(child);

        if (parent != null) {
            LOGGER.info("Established parent-child relationship: '{}' -> '{}'", parent.getName(), child.getName());
        } else if (previousParent != null) {
            LOGGER.info("Cleared parent '{}' from child '{}'", previousParent.getName(), child.getName());
        }
    }

    /**
     * Check if setting a parent would create a circular dependency.
     * This checks if the proposed parent is already a descendant of the child.
     * 
     * @param parent the proposed parent activity
     * @param child the child activity
     * @return true if circular dependency would be created
     */
    @Transactional(readOnly = true)
    public boolean wouldCreateCircularDependency(final CActivity parent, final CActivity child) {
        Check.notNull(parent, "Parent activity cannot be null");
        Check.notNull(child, "Child activity cannot be null");
        Check.notNull(parent.getId(), "Parent ID cannot be null");
        Check.notNull(child.getId(), "Child ID cannot be null");

        // Check if parent is a descendant of child
        final List<Long> descendantIds = repository.findAllDescendantIds(child.getId());
        return descendantIds.contains(parent.getId());
    }

    /**
     * Helper method to recursively collect all descendants.
     * 
     * @param activity the current activity
     * @param descendants accumulator for descendants
     * @param visited set of visited IDs to prevent infinite loops
     */
    private void collectDescendants(final CActivity activity, final List<CActivity> descendants, final Set<Long> visited) {
        final List<CActivityParentRelation> childRelations = repository.findChildrenByParentId(activity.getId());
        
        for (final CActivityParentRelation relation : childRelations) {
            final CActivity child = (CActivity) relation.getOwnerItem();
            if (child != null && !visited.contains(child.getId())) {
                visited.add(child.getId());
                descendants.add(child);
                collectDescendants(child, descendants, visited);
            }
        }
    }
}
