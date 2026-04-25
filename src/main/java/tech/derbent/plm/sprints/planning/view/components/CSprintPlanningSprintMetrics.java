package tech.derbent.plm.sprints.planning.view.components;

/**
 * Lightweight sprint header metrics for planning views.
 *
 * <p>This mirrors Jira's sprint header summary (items + story points) without coupling grids to services.
 * The board computes these from the loaded sprintable items and injects them into the sprint tree grid.
 * </p>
 */
public record CSprintPlanningSprintMetrics(int itemCount, long storyPoints) {}
