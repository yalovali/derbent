package tech.derbent.plm.kanban.kanbanline.domain;

/** EKanbanViewMode — controls how the kanban board loads and displays items.
 * <p>
 * <strong>Sprint Board</strong> (default): shows items that belong to the currently-selected sprint.
 * The backlog column on the left lists non-sprint items; dragging from the backlog to a column
 * adds the item to the sprint and transitions its status.
 * </p>
 * <p>
 * <strong>Status Board</strong>: shows <em>all</em> project items regardless of sprint membership,
 * grouped by workflow status into the kanban columns. No backlog column is shown. Dragging between
 * columns changes only the item's status — sprint assignment is never modified. This lets teams
 * track work-in-progress without formal sprint planning.
 * </p> */
public enum EKanbanViewMode {

	/** Items from the selected sprint only. Backlog column visible. Drag from backlog adds to sprint. */
	SPRINT_BOARD("Sprint Board"),
	/** All project items by status. No backlog column. Drag only changes status, not sprint membership. */
	STATUS_BOARD("Status Board");

	public static final String FILTER_KEY = "kanbanViewMode";

	private final String label;

	EKanbanViewMode(final String label) { this.label = label; }

	public String getLabel() { return label; }
}
