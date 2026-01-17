package tech.derbent.app.activities.view;

import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.app.activities.domain.CActivity;

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
		if (activity.hasParent()) {
			try {
				// TODO: Fix getBean call - incorrect parameters
				// Fetch parent entity to display its name
				// final CAbstractService<?> service = CSpringContext.getBean(CAbstractService.class, activity.getParentType());
				// final Object parent = service.getById(activity.getParentId());
				// if (parent instanceof CActivity) {
				// 	final CActivity parentActivity = (CActivity) parent;
				// 	final CLabelEntity parentLabel = new CLabelEntity(parentActivity);
				// 	parentLabel.getStyle().set("font-style", "italic");
				// 	parentLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
				// 	layoutLineThree.add(parentLabel);
				// }
			} catch (final Exception e) {
				// Silently ignore if parent cannot be loaded - it may have been deleted
			}
		}
	}
}
