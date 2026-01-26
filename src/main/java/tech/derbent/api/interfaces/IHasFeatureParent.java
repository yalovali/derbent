package tech.derbent.api.interfaces;

import org.springframework.transaction.annotation.Transactional;
import tech.derbent.plm.agile.domain.CFeature;

/** IHasFeatureParent - Marker interface for entities that can have a Feature as parent.
 * <p>
 * This interface is implemented by UserStory entities that can have a Feature as their parent.
 * </p>
 * <p>
 * <strong>Hierarchy Position:</strong> Mid-level (can have Features as children below)
 * </p>
 * <p>
 * <strong>Allowed Parent:</strong> CFeature only
 * </p>
 * @author Derbent Framework
 * @see IHasAgileParentRelation
 * @see CFeature */
public interface IHasFeatureParent extends IHasAgileParentRelation {

	/** Get the parent Feature for this entity.
	 * @return the parent Feature, or null if this entity has no parent */
	default CFeature getParentFeature() {
		final var parent = getParentItem();
		return parent instanceof CFeature ? (CFeature) parent : null;
	}

	/** Set the parent Feature for this entity.
	 * @param feature the parent Feature, or null to clear the parent */
	@Transactional
	default void setParentFeature(final CFeature feature) {
		setParentItem(feature);
	}

	/** Check if this entity has a Feature parent.
	 * @return true if this entity has a Feature parent, false otherwise */
	default boolean hasFeatureParent() {
		return getParentFeature() != null;
	}
}
