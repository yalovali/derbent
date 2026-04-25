package tech.derbent.plm.sprints.planning.view.components;

import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;

/**
 * Shared drag state for the sprint planning board.
 *
 * <p>Vaadin Grid drag/drop listeners are scoped to each grid instance, so to enable
 * cross-grid moves (Backlog → Sprint and Sprint → Backlog) we keep the currently
 * dragged {@link CGnntItem} in a board-level context that both grids can consult.</p>
 */
public class CSprintPlanningDragContext {

	private CGnntItem draggedItem;
	private String sourceGridId;

	public void clear() {
		draggedItem = null;
		sourceGridId = null;
	}

	public CGnntItem getDraggedItem() {
		return draggedItem;
	}

	public String getSourceGridId() {
		return sourceGridId;
	}

	public void setDraggedItem(final CGnntItem draggedItem, final String sourceGridId) {
		this.draggedItem = draggedItem;
		this.sourceGridId = sourceGridId;
	}
}
