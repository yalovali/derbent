package tech.derbent.app.activities.view;

import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.app.activities.domain.CActivity;

/** CComponentWidgetActivity - Widget component for displaying Activity entities in grids.
 * <p>
 * This widget displays activity information in a three-row layout:
 * <ul>
 * <li><b>Row 1:</b> Activity name with icon and color</li>
 * <li><b>Row 2:</b> Description (truncated with ellipsis)</li>
 * <li><b>Row 3:</b> Status badge, responsible user, and date range</li>
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
}
