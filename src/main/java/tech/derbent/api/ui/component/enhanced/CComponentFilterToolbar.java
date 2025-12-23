package tech.derbent.api.ui.component.enhanced;

import tech.derbent.api.utils.Check;

import com.vaadin.flow.component.Component;

/** CComponentFilterToolbar - Common filtering toolbar base class.
 * <p>
 * Extends {@link CComponentGridSearchToolbar} so filtering base components can reuse a shared toolbar while allowing custom filter controls to be
 * appended for specialized use cases.
 * </p>
 */
public class CComponentFilterToolbar extends CComponentGridSearchToolbar {

	private static final long serialVersionUID = 1L;

	public CComponentFilterToolbar() {
		super();
	}

	public CComponentFilterToolbar(final ToolbarConfig config) {
		super(config);
	}

	public void addFilterComponent(final Component component) {
		Check.notNull(component, "Filter component cannot be null");
		add(component);
	}

	public void addFilterComponents(final Component... components) {
		Check.notNull(components, "Filter components cannot be null");
		for (final Component component : components) {
			addFilterComponent(component);
		}
	}
}
