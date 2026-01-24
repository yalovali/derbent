package tech.derbent.plm.activities.view;

import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.plm.activities.domain.CActivity;

/** CComponentWidgetActivity - Widget component for displaying Activity entities in grids.
 * <p>
 * This widget displays activity information in a three-row layout:
 * <ul>
 * <li><b>Row 1:</b> Activity name with icon and color</li>
 * <li><b>Row 2:</b> Description (truncated with ellipsis)</li>
 * <li><b>Row 3:</b> Status badge, responsible user, date range, and parent activity (if present)</li>
 * </ul>
 * </p>
 * <p>
 * Inherits common functionality from CComponentWidgetEntityOfProject.
 * </p>
 * @author Derbent Framework
 * @since 1.0
 * @see CComponentWidgetEntityOfProject */
public class CComponentWidgetActivity extends CComponentWidgetEntityOfProject<CActivity> {

	private static final long serialVersionUID = 1L;

	/** Creates a new activity widget for the specified activity.
	 * @param activity the activity to display in the widget */
	public CComponentWidgetActivity(final CActivity activity) {
		super(activity);
		// addEditAction();
		// addDeleteAction();
	}

	/** Override third line to include parent activity information if present. */
	@Override
	protected void createThirdLine() throws Exception {
		super.createThirdLine();
		// Add parent activity information if this activity has a parent
		final CActivity activity = getEntity();
		if (activity.hasParentActivity()) {
			try {
				final CActivity parentActivity = activity.getParentActivity();
				if (parentActivity != null) {
					final CLabelEntity parentLabel = new CLabelEntity(parentActivity);
					parentLabel.getStyle().set("font-style", "italic");
					parentLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
					layoutLineThree.add(parentLabel);
				}
			} catch (@SuppressWarnings ("unused") final Exception e) {
				// Silently ignore if parent cannot be loaded - it may have been deleted
			}
		}
	}
}
