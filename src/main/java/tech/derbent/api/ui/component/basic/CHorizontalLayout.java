package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.utils.CAuxillaries;

/** CHorizontalLayout - Enhanced base class for horizontal layouts in the application. Layer: View (MVC) Provides common initialization patterns,
 * utility methods for spacing, padding, and sizing operations. Extends Vaadin HorizontalLayout with application-specific enhancements. */
public class CHorizontalLayout extends HorizontalLayout {

	private static final long serialVersionUID = 1L;

	public static CHorizontalLayout forForm() {
		final CHorizontalLayout layout = new CHorizontalLayout(false, true, false);
		layout.setClassName("form-field-layout");
		layout.setAlignItems(FlexComponent.Alignment.BASELINE);
		return layout;
	}

	/** Default constructor with no padding, no spacing, and full size. */
	public CHorizontalLayout() {
		super();
		initializeComponent();
	}

	/** Constructor with explicit padding, spacing, and margin settings. */
	public CHorizontalLayout(final boolean padding, final boolean spacing, final boolean margin) {
		this();
		setPadding(padding);
		setSpacing(spacing);
		setMargin(margin);
	}

	/** Constructor with components. */
	public CHorizontalLayout(final Component... components) {
		this();
		add(components);
	}

	/** Constructor with CSS class name. */
	public CHorizontalLayout(final String style) {
		this();
		addClassName(style);
	}

	/** Common initialization for all CHorizontalLayout instances. */
	final private void initializeComponent() {
		CAuxillaries.setId(this);
		setWidthFull();
		setDefaultVerticalComponentAlignment(Alignment.CENTER);
		setJustifyContentMode(JustifyContentMode.START);
		setPadding(false);
		setSpacing(false);
		setMargin(false);
	}
}
