package tech.derbent.api.grid.widget;

import tech.derbent.api.entityOfProject.domain.CProjectItem;

public abstract class CComponentWidgetEntityOfProject<T extends CProjectItem<?>> extends CComponentWidgetEntity<T> {

	/** Max description length to display in the widget. */
	protected static final int MAX_DESCRIPTION_LENGTH = 100;
	private static final long serialVersionUID = 1L;

	/** Creates a new project item widget for the specified entity.
	 * @param item the project item to display in the widget */
	public CComponentWidgetEntityOfProject(final T item) {
		super(item);
	}
}
