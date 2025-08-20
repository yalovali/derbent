package tech.derbent.abstracts.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import tech.derbent.abstracts.utils.CAuxillaries;

/**
 * CHorizontalLayout - Enhanced base class for horizontal layouts in the application.
 * Layer: View (MVC) Provides common initialization patterns, utility methods for spacing,
 * padding, and sizing operations. Extends Vaadin HorizontalLayout with
 * application-specific enhancements.
 */
public class CHorizontalLayout extends HorizontalLayout {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a horizontal layout for button groups with spacing and center alignment.
	 * @param buttons the buttons to add
	 * @return new CHorizontalLayout configured for buttons
	 */
	public static CHorizontalLayout forButtons(final Component... buttons) {
		final CHorizontalLayout layout = withSpacing();
		layout.setWidthFull();
		layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		layout.add(buttons);
		return layout;
	}

	public static CHorizontalLayout forForm() {
		final CHorizontalLayout layout = new CHorizontalLayout(false, true, false);
		layout.setClassName("form-field-layout");
		layout.setJustifyContentMode(JustifyContentMode.START);
		layout.setAlignItems(FlexComponent.Alignment.BASELINE);
		return layout;
	}

	/**
	 * Creates a horizontal layout for toolbar components with spacing.
	 * @param components the toolbar components to add
	 * @return new CHorizontalLayout configured for toolbars
	 */
	public static CHorizontalLayout forToolbar(final Component... components) {
		final CHorizontalLayout layout = withSpacing();
		layout.setWidthFull();
		layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		layout.setJustifyContentMode(JustifyContentMode.START);
		layout.add(components);
		return layout;
	}

	/**
	 * Creates a horizontal layout with all spacing, padding, and margin enabled.
	 * @return new CHorizontalLayout with full spacing configuration
	 */
	public static CHorizontalLayout withFullSpacing() {
		return new CHorizontalLayout(true, true, true);
	}

	/**
	 * Creates a horizontal layout with padding enabled.
	 * @return new CHorizontalLayout with padding
	 */
	public static CHorizontalLayout withPadding() {
		return new CHorizontalLayout(true, false, false);
	}

	/**
	 * Creates a horizontal layout with spacing enabled.
	 * @return new CHorizontalLayout with spacing
	 */
	public static CHorizontalLayout withSpacing() {
		return new CHorizontalLayout(false, true, false);
	}

	/**
	 * Creates a horizontal layout with both spacing and padding enabled.
	 * @return new CHorizontalLayout with spacing and padding
	 */
	public static CHorizontalLayout withSpacingAndPadding() {
		return new CHorizontalLayout(true, true, false);
	}

	/**
	 * Default constructor with no padding, no spacing, and full size.
	 */
	public CHorizontalLayout() {
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
	public CHorizontalLayout(final boolean padding, final boolean spacing,
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
	public CHorizontalLayout(final Component... components) {
		this();
		add(components);
	}

	/**
	 * Constructor with CSS class name.
	 */
	public CHorizontalLayout(final String style) {
		this();
		addClassName(style);
	}

	/**
	 * Constructor with CSS class name and width setting.
	 */
	public CHorizontalLayout(final String style, final boolean isFullWidth) {
		this(style);

		if (isFullWidth) {
			setWidthFull();
		}
	}

	/**
	 * Common initialization for all CHorizontalLayout instances.
	 */
	protected void initializeLayout() {
		CAuxillaries.setId(this);
	}

	/**
	 * Fluent API for setting default vertical component alignment.
	 * @param alignment the alignment to set
	 * @return this layout for method chaining
	 */
	public CHorizontalLayout withDefaultAlignment(final Alignment alignment) {
		setDefaultVerticalComponentAlignment(alignment);
		return this;
	}

	/**
	 * Fluent API for setting height to full.
	 * @return this layout for method chaining
	 */
	public CHorizontalLayout withHeightFull() {
		setHeightFull();
		return this;
	}

	/**
	 * Fluent API for setting justify content mode.
	 * @param mode the justify content mode to set
	 * @return this layout for method chaining
	 */
	public CHorizontalLayout withJustifyContentMode(final JustifyContentMode mode) {
		setJustifyContentMode(mode);
		return this;
	}

	/**
	 * Fluent API for setting margin.
	 * @param margin whether to enable margin
	 * @return this layout for method chaining
	 */
	public CHorizontalLayout withMargin(final boolean margin) {
		setMargin(margin);
		return this;
	}

	/**
	 * Fluent API for setting padding.
	 * @param padding whether to enable padding
	 * @return this layout for method chaining
	 */
	public CHorizontalLayout withPadding(final boolean padding) {
		setPadding(padding);
		return this;
	}

	/**
	 * Fluent API for setting size to full.
	 * @return this layout for method chaining
	 */
	public CHorizontalLayout withSizeFull() {
		setSizeFull();
		return this;
	}

	/**
	 * Fluent API for setting spacing.
	 * @param spacing whether to enable spacing
	 * @return this layout for method chaining
	 */
	public CHorizontalLayout withSpacing(final boolean spacing) {
		setSpacing(spacing);
		return this;
	}

	/**
	 * Fluent API for setting width to full.
	 * @return this layout for method chaining
	 */
	public CHorizontalLayout withWidthFull() {
		setWidthFull();
		return this;
	}
}
