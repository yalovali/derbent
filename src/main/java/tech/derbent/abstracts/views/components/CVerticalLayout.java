package tech.derbent.abstracts.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import tech.derbent.abstracts.utils.CAuxillaries;

/**
 * CVerticalLayout - Enhanced base class for vertical layouts in the application. Layer:
 * View (MVC) Provides common initialization patterns, utility methods for spacing,
 * padding, and sizing operations. Extends Vaadin VerticalLayout with application-specific
 * enhancements.
 */
public class CVerticalLayout extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a vertical layout for button groups with spacing.
	 * @param buttons the buttons to add
	 * @return new CVerticalLayout configured for buttons
	 */
	public static CVerticalLayout forButtons(final Component... buttons) {
		final CVerticalLayout layout = new CVerticalLayout(false, true, false);
		layout.setWidthFull();
		layout.setDefaultHorizontalComponentAlignment(Alignment.START);
		layout.add(buttons);
		return layout;
	}

	/**
	 * Default constructor with no padding, no spacing, and full size.
	 */
	public CVerticalLayout() {
		super();
		setSizeFull();
		setPadding(false);
		setSpacing(false);
		setMargin(false);
		initializeLayout();
	}

	/**
	 * Constructor with explicit padding, spacing, and margin settings.
	 */
	public CVerticalLayout(final boolean padding, final boolean spacing,
		final boolean margin) {
		super();
		setPadding(padding);
		setSpacing(spacing);
		setMargin(margin);
		setSizeFull();
		initializeLayout();
	}

	/**
	 * Constructor with components.
	 */
	public CVerticalLayout(final Component... components) {
		this();
		add(components);
	}

	/**
	 * Constructor with CSS class name.
	 */
	public CVerticalLayout(final String style) {
		this();
		addClassName(style);
	}

	/**
	 * Constructor with CSS class name and width setting.
	 */
	public CVerticalLayout(final String style, final boolean isFullWidth) {
		this(style);

		if (isFullWidth) {
			setWidthFull();
		}
	}

	/**
	 * Common initialization for all CVerticalLayout instances.
	 */
	protected void initializeLayout() {
		setSizeFull();
		CAuxillaries.setId(this);
	}

	/**
	 * Fluent API for setting default horizontal component alignment.
	 * @param alignment the alignment to set
	 * @return this layout for method chaining
	 */
	public CVerticalLayout withDefaultAlignment(final Alignment alignment) {
		setDefaultHorizontalComponentAlignment(alignment);
		return this;
	}
}
