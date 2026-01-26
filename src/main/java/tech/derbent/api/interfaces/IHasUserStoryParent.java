package tech.derbent.api.interfaces;

import org.springframework.transaction.annotation.Transactional;
import tech.derbent.plm.agile.domain.CUserStory;

/** IHasUserStoryParent - Marker interface for entities that can have a UserStory as parent.
 * <p>
 * This interface is implemented by leaf-level entities (Activity, Meeting, Risk, Decision) 
 * that can ONLY have a UserStory as their parent in the agile hierarchy.
 * </p>
 * <p>
 * <strong>Hierarchy Position:</strong> Leaf Level (cannot be a parent to other entities)
 * </p>
 * <p>
 * <strong>Allowed Parent:</strong> CUserStory only
 * </p>
 * @author Derbent Framework
 * @see IHasAgileParentRelation
 * @see CUserStory */
public interface IHasUserStoryParent extends IHasAgileParentRelation {

	/** Get the parent UserStory for this entity.
	 * @return the parent UserStory, or null if this entity has no parent */
	default CUserStory getParentUserStory() {
		final var parent = getParentItem();
		return parent instanceof CUserStory ? (CUserStory) parent : null;
	}

	/** Set the parent UserStory for this entity.
	 * @param userStory the parent UserStory, or null to clear the parent */
	@Transactional
	default void setParentUserStory(final CUserStory userStory) {
		setParentItem(userStory);
	}

	/** Check if this entity has a UserStory parent.
	 * @return true if this entity has a UserStory parent, false otherwise */
	default boolean hasUserStoryParent() {
		return getParentUserStory() != null;
	}
}
