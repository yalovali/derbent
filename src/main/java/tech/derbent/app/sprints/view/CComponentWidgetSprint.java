package tech.derbent.app.sprints.view;

import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.app.sprints.domain.CSprint;

/** CComponentWidgetSprint - Widget component for displaying Sprint entities in grids.
 * <p>
 * This widget displays sprint information in a three-row layout:
 * <ul>
 * <li><b>Row 1:</b> Sprint name (12pt font)</li>
 * <li><b>Row 2:</b> Short description and status badge (10pt font)</li>
 * <li><b>Row 3:</b> Assigned user, start date, end date (10pt font)</li>
 * </ul>
 * </p>
 * @author Derbent Framework
 * @since 1.0
 * @see CComponentWidgetEntityOfProject */
public class CComponentWidgetSprint extends CComponentWidgetEntityOfProject<CSprint> {

	private static final long serialVersionUID = 1L;

	/** Creates a new sprint widget for the specified sprint.
	 * @param item the sprint to display in the widget */
	public CComponentWidgetSprint(final CSprint item) {
		super(item);
		addEditAction();
		addDeleteAction();
	}
}
