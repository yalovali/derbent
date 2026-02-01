package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.html.H1;

/** CH1 - Header level 1 component wrapper.
 * <p>
 * Follows the project convention of using C-prefixed component wrappers.
 * Provides consistent styling for header 1 elements, matching CH3 styling.
 * </p>
 * @author Derbent Framework
 * @since 1.0 */
public class CH1 extends H1 {

	private static final long serialVersionUID = 1L;

	/** Creates a new CH1 with the specified text.
	 * @param text the header text */
	public CH1(final String text) {
		super(text);
		initializeComponent();
	}

	/** Initializes the component with default styling matching CH3 pattern. */
	private void initializeComponent() {
		getStyle().set("display", "flex").set("justify-content", "space-evenly");
		setWidthFull();
		// Height set to null for proper sizing (no setHeightFull - makes headers look ugly)
		setHeight(null);
		getStyle().set("margin", "0");
		getStyle().set("padding", "0");
	}
}
