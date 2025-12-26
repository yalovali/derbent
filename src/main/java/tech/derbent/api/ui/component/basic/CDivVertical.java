package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import tech.derbent.api.utils.CAuxillaries;

/** CDiv - Enhanced base class for div containers in the application. Layer: View (MVC) Provides common initialization patterns, utility methods for
 * styling, sizing, and content management. Extends Vaadin Div with application-specific enhancements.
 * <p>
 * Note: This class already implements ClickNotifier through Vaadin's Div base class. */
public class CDivVertical extends Div {

	private static final long serialVersionUID = 1L;

	public CDivVertical() {
		super();
		initializeComponent();
	}

	/** Constructor with initial components. */
	public CDivVertical(final Component... components) {
		super(components);
		initializeComponent();
	}

	public CDivVertical(final String text) {
		super(text);
		initializeComponent();
	}

	/** Common initialization for all CDiv instances. */
	final private void initializeComponent() {
		CAuxillaries.setId(this);
		addClassNames(Display.FLEX, FlexDirection.COLUMN, // 🔥 stack items top to bottom
				AlignItems.START, // 🔥 align items to top
				JustifyContent.START, // 🔥 prevent vertical centering
				Gap.SMALL // optional spacing between items
		);
		setWidthFull();
	}
}
