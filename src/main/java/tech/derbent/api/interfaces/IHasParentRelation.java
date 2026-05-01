package tech.derbent.api.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.parentrelation.domain.CParentRelation;
import tech.derbent.api.utils.Check;

/** IHasParentRelation - Canonical interface for entities that support hierarchical parent relationships.
 * <p>
 * This interface is implemented by domain entities (e.g., CActivity, CMeeting, CIssue, CEpic, CFeature, CUserStory) that can participate in
 * hierarchical relationships. It provides a contract for entities that can have parent-child relationships in a flexible, level-based hierarchy.
 * </p>
 * <p>
 * Entities implementing this interface can:
 * <ul>
 * <li>Be organized in hierarchical structures using level-based rules</li>
 * <li>Have a parent entity at a higher level in the hierarchy</li>
 * <li>Have child entities at a lower level in the hierarchy</li>
 * <li>Navigate the hierarchy using getParentItem() and setParentItem()</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Level-Based Hierarchy Rules:</strong>
 * <ul>
 * <li>Level 0 (e.g. Epic): Cannot have a parent — must be root level</li>
 * <li>Level N &gt; 0 (e.g. Feature=1, UserStory=2): Parent must have level N-1</li>
 * <li>Level -1 (leaf entities like Activity, Meeting, Risk): Can have any non-leaf entity as parent</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Implementation Requirements:</strong>
 * <ul>
 * <li>Entity must have a @OneToOne relationship with CParentRelation</li>
 * <li>Entity must cascade ALL operations to the parent relation (CASCADE.ALL, orphanRemoval=true)</li>
 * <li>Entity must initialize parent relation in constructor</li>
 * <li>Entity must set back-reference in parent relation after loading (@PostLoad)</li>
 * </ul>
 * </p>
 * @see CParentRelation */
public interface IHasParentRelation {

	Logger LOGGER = LoggerFactory.getLogger(IHasParentRelation.class);

	/** Clear the parent relationship, making this a root-level item. Sets the parentItem to null but does NOT delete the parent relation entity. */
	@Transactional
	default void clearParentItem() {
		final CParentRelation parentRelation = getParentRelation();
		Check.notNull(parentRelation, "Parent relation cannot be null");
		final CProjectItem<?, ?> previousParent = parentRelation.getParentItem();
		parentRelation.setParentItem(null);
		if (previousParent != null) {
			LOGGER.info("Cleared parent item '{}' from item '{}'", previousParent.getName(), getName());
		}
	}

	/** Get the entity ID.
	 * @return the entity ID */
	Long getId();

	/** Get the entity name.
	 * @return the entity name */
	String getName();

	/** Get the parent item in the hierarchy.
	 * @return the parent project item, or null if this is a root item */
	default CProjectItem<?, ?> getParentItem() {
		final CParentRelation parentRelation = getParentRelation();
		Check.notNull(parentRelation, "Parent relation must not be null");
		return parentRelation.getParentItem();
	}

	/** Get the parent relation entity.
	 * @return the parent relation entity */
	CParentRelation getParentRelation();

	/** Check if this item has a parent in the hierarchy.
	 * @return true if a parent is set, false otherwise */
	default boolean hasParentActivity() {
		final CParentRelation parentRelation = getParentRelation();
		return parentRelation != null && parentRelation.hasParent();
	}

	/** Set the parent item in the hierarchy. This method delegates to the parent relation entity.
	 * @param parentItem the parent project item, or null to make this a root item */
	@Transactional
	default void setParentItem(final CProjectItem<?, ?> parentItem) {
		final CParentRelation parentRelation = getParentRelation();
		Check.notNull(parentRelation, "Parent relation cannot be null");
		// Prevent self-reference
		if (parentItem != null && parentItem.getId() != null && parentItem.getId().equals(getId())) {
			throw new IllegalArgumentException("An entity cannot be its own parent");
		}
		parentRelation.setParentItem(parentItem);
	}

	/** Set the parent relation entity.
	 * @param parentRelation the parent relation entity */
	void setParentRelation(CParentRelation parentRelation);
}
