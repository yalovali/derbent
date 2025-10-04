package tech.derbent.api.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import tech.derbent.api.utils.CAuxillaries;

/** CDiv - Enhanced base class for div containers in the application. Layer: View (MVC) Provides common initialization patterns, utility methods for
 * styling, sizing, and content management. Extends Vaadin Div with application-specific enhancements. */
public class CDiv extends Div {

	private static final long serialVersionUID = 1L;

	public CDiv() {
		super();
		initializeDiv();
	}

	/** Constructor with initial components. */
	public CDiv(final Component... components) {
		super(components);
		initializeDiv();
	}

	public CDiv(final String text) {
		super(text);
		initializeDiv();
	}

	/** Common initialization for all CDiv instances. */
	protected void initializeDiv() {
		CAuxillaries.setId(this);
		addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
		setWidthFull();
	}
}
