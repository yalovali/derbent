package tech.derbent.plm.gnnt.gnntitem.domain;

import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.plm.gannt.ganntitem.domain.CGanntItem;

/**
 * Gnnt-specific wrapper around project items for the new board.
 *
 * <p>The class intentionally reuses the mature generic Gantt item DTO behavior,
 * while exposing Gnnt naming and title helpers in the new package tree.</p>
 */
public class CGnntItem extends CGanntItem {

	public static final String DEFAULT_COLOR = "#3B5FA7";
	public static final String DEFAULT_ICON = "vaadin:timeline";
	public static final String ENTITY_TITLE_PLURAL = "Gnnt Items";
	public static final String ENTITY_TITLE_SINGULAR = "Gnnt Item";
	public static final String VIEW_NAME = "Gnnt Items View";

	public CGnntItem(final CProjectItem<?> entity, final long uniqueId, final int hierarchyLevel) {
		super(entity, uniqueId);
		this.hierarchyLevel = hierarchyLevel;
	}

	private final int hierarchyLevel;

	@Override
	public int getHierarchyLevel() {
		return hierarchyLevel;
	}

	public String getEntityTypeTitle() {
		final String title = CEntityRegistry.getEntityTitleSingular(getEntity().getClass());
		return title != null ? title : getEntityType();
	}

	public String getIndentedName() {
		return getEntity().getName();
	}
}
