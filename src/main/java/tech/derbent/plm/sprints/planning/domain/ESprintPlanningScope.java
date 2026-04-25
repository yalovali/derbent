package tech.derbent.plm.sprints.planning.domain;

/**
 * ESprintPlanningScope - High-level filter for the sprint planning board.
 *
 * <p>This enum exists so the planning UI can offer an explicit "Backlog" scope
 * (items with {@code sprintItem.sprint == null}) without overloading the existing
 * Gnnt toolbar semantics.</p>
 */
public enum ESprintPlanningScope {

	ALL_ITEMS,
	BACKLOG,
	SPRINT
}
