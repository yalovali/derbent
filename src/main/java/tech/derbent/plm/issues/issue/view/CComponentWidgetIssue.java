package tech.derbent.plm.issues.issue.view;

import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.plm.issues.issue.domain.CIssue;

/**
 * CComponentWidgetIssue - Widget component for displaying Issue entities in grids.
 * <p>
 * This widget displays issue information in a three-row layout:
 * <ul>
 * <li><b>Row 1:</b> Issue name with icon and color</li>
 * <li><b>Row 2:</b> Description (truncated with ellipsis)</li>
 * <li><b>Row 3:</b> Status badge, severity, priority, and assigned user</li>
 * </ul>
 * </p>
 * <p>
 * Inherits common functionality from CComponentWidgetEntityOfProject.
 * </p>
 * 
 * @author Derbent Framework
 * @since 1.0
 * @see CComponentWidgetEntityOfProject
 */
public class CComponentWidgetIssue extends CComponentWidgetEntityOfProject<CIssue> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new issue widget for the specified issue.
	 * 
	 * @param issue the issue to display in the widget
	 */
	public CComponentWidgetIssue(final CIssue issue) {
		super(issue);
		// addEditAction();
		// addDeleteAction();
	}
}
