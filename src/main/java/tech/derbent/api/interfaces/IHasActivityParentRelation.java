package tech.derbent.api.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.domain.CActivityParentRelation;

/**
 * IHasActivityParentRelation - Marker interface for entities that support agile hierarchy relationships.
 * <p>
 * This interface is implemented by domain entities (e.g., CActivity, CMeeting) that can participate
 * in agile hierarchies (Epic → User Story → Task, etc.). It provides a contract for entities
 * that can have parent-child relationships within the agile methodology.
 * </p>
 * <p>
 * Entities implementing this interface can:
 * <ul>
 * <li>Be organized in hierarchical structures (Epic, User Story, Feature, Task)</li>
 * <li>Have parent activities of higher levels in the hierarchy</li>
 * <li>Have child activities of lower levels in the hierarchy</li>
 * <li>Navigate up and down the hierarchy tree</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Implementation Requirements:</strong>
 * <ul>
 * <li>Entity must have a @OneToOne relationship with CActivityParentRelation</li>
 * <li>Entity must cascade ALL operations to the parent relation (CASCADE.ALL, orphanRemoval=true)</li>
 * <li>Entity must initialize parent relation in constructor</li>
 * <li>Entity must set back-reference in parent relation after loading</li>
 * </ul>
 * </p>
 * 
 * @author Derbent Framework
 * @see CActivityParentRelation
 * @see CActivity
 */
public interface IHasActivityParentRelation {

    Logger LOGGER = LoggerFactory.getLogger(IHasActivityParentRelation.class);

    /**
     * Clear the parent relationship, making this a root-level item.
     * Sets the parentActivity to null but does NOT delete the parent relation entity.
     */
    @Transactional
    default void clearParentActivity() {
        final CActivityParentRelation parentRelation = getParentRelation();
        Check.notNull(parentRelation, "Parent relation cannot be null");
        
        final CActivity previousParent = parentRelation.getParentActivity();
        parentRelation.setParentActivity(null);
        
        if (previousParent != null) {
            LOGGER.info("Cleared parent activity '{}' from item '{}'", previousParent.getName(), getName());
        }
    }

    /**
     * Get the entity ID.
     * 
     * @return the entity ID
     */
    Long getId();

    /**
     * Get the entity name.
     * 
     * @return the entity name
     */
    String getName();

    /**
     * Get the parent activity in the agile hierarchy.
     * 
     * @return the parent activity, or null if this is a root item
     */
    default CActivity getParentActivity() {
        final CActivityParentRelation parentRelation = getParentRelation();
        Check.notNull(parentRelation, "Parent relation must not be null");
        return parentRelation.getParentActivity();
    }

    /**
     * Get the parent relation entity.
     * 
     * @return the parent relation entity
     */
    CActivityParentRelation getParentRelation();

    /**
     * Check if this item has a parent in the agile hierarchy.
     * 
     * @return true if a parent is set, false otherwise
     */
    default boolean hasParentActivity() {
        final CActivityParentRelation parentRelation = getParentRelation();
        return parentRelation != null && parentRelation.hasParent();
    }

    /**
     * Set the parent activity in the agile hierarchy.
     * This method delegates to the parent relation entity.
     * 
     * @param parentActivity the parent activity, or null to make this a root item
     */
    @Transactional
    default void setParentActivity(final CActivity parentActivity) {
        final CActivityParentRelation parentRelation = getParentRelation();
        Check.notNull(parentRelation, "Parent relation cannot be null");
        
        // Prevent self-reference
        if (parentActivity != null && parentActivity.getId() != null && parentActivity.getId().equals(getId())) {
            throw new IllegalArgumentException("An activity cannot be its own parent");
        }
        
        final CActivity previousParent = parentRelation.getParentActivity();
        parentRelation.setParentActivity(parentActivity);
        
        if (parentActivity != null) {
            LOGGER.info("Set parent activity '{}' for item '{}'", parentActivity.getName(), getName());
        } else if (previousParent != null) {
            LOGGER.info("Cleared parent activity '{}' from item '{}'", previousParent.getName(), getName());
        }
    }

    /**
     * Set the parent relation entity.
     * 
     * @param parentRelation the parent relation entity
     */
    void setParentRelation(CActivityParentRelation parentRelation);
}
