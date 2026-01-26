package tech.derbent.api.interfaces;

import org.springframework.transaction.annotation.Transactional;
import tech.derbent.plm.agile.domain.CEpic;

/** IHasEpicParent - Marker interface for entities that can have an Epic as parent.
 * <p>
 * This interface is implemented by Feature entities that can have an Epic as their parent.
 * </p>
 * <p>
 * <strong>Hierarchy Position:</strong> High-level (can have Epics as parents above)
 * </p>
 * <p>
 * <strong>Allowed Parent:</strong> CEpic only
 * </p>
 * @author Derbent Framework
 * @see IHasAgileParentRelation
 * @see CEpic */
public interface IHasEpicParent extends IHasAgileParentRelation {

	/** Get the parent Epic for this entity.
	 * @return the parent Epic, or null if this entity has no parent */
	default CEpic getParentEpic() {
		final var parent = getParentItem();
		return parent instanceof CEpic ? (CEpic) parent : null;
	}

	/** Set the parent Epic for this entity.
	 * @param epic the parent Epic, or null to clear the parent */
	@Transactional
	default void setParentEpic(final CEpic epic) {
		setParentItem(epic);
	}

	/** Check if this entity has an Epic parent.
	 * @return true if this entity has an Epic parent, false otherwise */
	default boolean hasEpicParent() {
		return getParentEpic() != null;
	}
}
