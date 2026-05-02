package tech.derbent.plm.sprints.planning.view.components;

/**
 * Lightweight sprint header metrics for planning views.
 *
 * <p>This mirrors Jira's sprint header summary, including done/total rollups, without coupling grids to services.
 * The board computes these from the loaded sprintable items and injects them into the sprint tree grid.
 * </p>
 */
public record CSprintPlanningSprintMetrics(int itemDoneCount, int itemTotalCount, long storyPointsDone,
		long storyPointsTotal, int inSprintCount) {

	/** Backward-compatible constructor for callers that don't track in-sprint counts. */
	public CSprintPlanningSprintMetrics(final int itemDoneCount, final int itemTotalCount, final long storyPointsDone,
			final long storyPointsTotal) {
		this(itemDoneCount, itemTotalCount, storyPointsDone, storyPointsTotal, 0);
	}

	/** Full sprint-header format: "X/Y tasks, Z/W SP". Used in sprint planning board header. */
	public String formatRollup() {
		return "%d/%d tasks, %d/%d SP".formatted(itemDoneCount, itemTotalCount, storyPointsDone, storyPointsTotal);
	}

	/** Compact kanban parent rollup: "[open/pending Done, active Active]".
	 * open = tasks in backlog (not done, not in sprint); pending = total - done; active = sprint-assigned.
	 * Invariant: open + active = pending (i.e. open + active = total - done). */
	public String formatTaskRollup() {
		final int pending = Math.max(0, itemTotalCount - itemDoneCount);
		final int open = Math.max(0, pending - inSprintCount);
		return "[%d/%d Done, %d Active]".formatted(open, pending, inSprintCount);
	}
}
