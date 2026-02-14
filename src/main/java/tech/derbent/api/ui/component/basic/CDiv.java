package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import tech.derbent.api.ui.constants.CUIConstants;
import tech.derbent.api.utils.CAuxillaries;

/** CDiv - Enhanced base class for div containers in the application. Layer: View (MVC) Provides common initialization patterns, utility methods for
 * styling, sizing, and content management. Extends Vaadin Div with application-specific enhancements.
 * <p>
 * Note: This class already implements ClickNotifier through Vaadin's Div base class. */
public class CDiv extends Div {
	private static final long serialVersionUID = 1L;

	public static Component errorDiv(final String string) {
		final CDiv div = new CDiv();
		div.addClassName("error-div");
		div.setText(string);
		return div;
	}

	public CDiv() {
		super();
		initializeComponent();
	}

	/** Constructor with initial components. */
	public CDiv(final Component... components) {
		super(components);
		initializeComponent();
	}

	public CDiv(final String text) {
		super(text);
		initializeComponent();
	}

	/** Common initialization for all CDiv instances. */
	final private void initializeComponent() {
		CAuxillaries.setId(this);
		addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
		setWidthFull();
		getStyle().set("box-sizing", "border-box");
		getStyle().set("min-width", "0");
	}

	/** Apply a standard card style with border, radius, background and padding. */
	public CDiv styleAsCard(final String padding) {
		getStyle().set("box-sizing", "border-box").set("min-width", "0")
				.set("padding", padding != null ? padding : CUIConstants.PADDING_STANDARD).set("border-radius", CUIConstants.BORDER_RADIUS_MEDIUM)
				.set("border", CUIConstants.BORDER_WIDTH_STANDARD + " " + CUIConstants.BORDER_STYLE_SOLID + " var(--lumo-contrast-10pct)")
				.set("background", "var(--lumo-base-color)");
		return this;
	}

	/** Apply a lightweight surface style for grouped sections. */
	public CDiv styleAsSurface(final String padding) {
		getStyle().set("box-sizing", "border-box").set("min-width", "0")
				.set("padding", padding != null ? padding : CUIConstants.PADDING_SMALL)
				.set("border-radius", CUIConstants.BORDER_RADIUS_MEDIUM).set("background", "var(--lumo-contrast-5pct)");
		return this;
	}

	/** Apply a warning banner style used for graceful degradation notices. */
	public CDiv styleAsWarningBanner() {
		getStyle().set("display", "flex").set("align-items", "center").set("gap", CUIConstants.GAP_SMALL).set("padding", "8px 6px")
				.set("margin-bottom", "12px").set("background", "var(--lumo-warning-color-10pct)")
				.set("border", "1px solid var(--lumo-warning-color-50pct)").set("border-radius", CUIConstants.BORDER_RADIUS_STANDARD)
				.set("color", "var(--lumo-warning-text-color)");
		return this;
	}
}
