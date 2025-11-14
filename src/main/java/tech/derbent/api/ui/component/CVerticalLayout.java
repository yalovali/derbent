package tech.derbent.api.ui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.utils.CAuxillaries;

/** CVerticalLayout - Enhanced base class for vertical layouts in the application. Layer: View (MVC) Provides common initialization patterns, utility
 * methods for spacing, padding, and sizing operations. Extends Vaadin VerticalLayout with application-specific enhancements. */
public class CVerticalLayout extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	/** Default constructor with no padding, no spacing, and full size. */
	public CVerticalLayout() {
		super();
		initLayout();
	}

	/** Constructor with explicit padding, spacing, and margin settings. */
	public CVerticalLayout(final boolean padding, final boolean spacing, final boolean margin) {
		this();
		setPadding(padding);
		setSpacing(spacing);
		setMargin(margin);
	}

	/** Constructor with components. */
	public CVerticalLayout(final Component... components) {
		this();
		add(components);
	}

	/** Constructor with CSS class name. */
	public CVerticalLayout(final String style) {
		this();
		addClassName(style);
	}

	protected void initLayout() {
		CAuxillaries.setId(this);
		setSizeFull();
		setPadding(false);
		setSpacing(false);
		setMargin(false);
		setDefaultHorizontalComponentAlignment(Alignment.START);
		setJustifyContentMode(JustifyContentMode.START);
	}
}
