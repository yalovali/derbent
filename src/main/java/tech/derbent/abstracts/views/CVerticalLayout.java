package tech.derbent.abstracts.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import tech.derbent.abstracts.utils.CAuxillaries;

/**
 * CVerticalLayout - Enhanced base class for vertical layouts in the application.
 * Layer: View (MVC)
 * Provides common initialization patterns, utility methods for spacing, padding, 
 * and sizing operations. Extends Vaadin VerticalLayout with application-specific
 * enhancements.
 */
public class CVerticalLayout extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor with no padding, no spacing, and full size.
     */
    public CVerticalLayout() {
        super();
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        initializeLayout();
    }

    /**
     * Constructor with explicit padding, spacing, and margin settings.
     */
    public CVerticalLayout(final boolean padding, final boolean spacing, final boolean margin) {
        super();
        setPadding(padding);
        setSpacing(spacing);
        setMargin(margin);
        setSizeFull();
        initializeLayout();
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
     * Constructor with components.
     */
    public CVerticalLayout(final Component... components) {
        this();
        add(components);
    }

    /**
     * Creates a vertical layout with spacing enabled.
     * @return new CVerticalLayout with spacing
     */
    public static CVerticalLayout withSpacing() {
        return new CVerticalLayout(false, true, false);
    }

    /**
     * Creates a vertical layout with padding enabled.
     * @return new CVerticalLayout with padding
     */
    public static CVerticalLayout withPadding() {
        return new CVerticalLayout(true, false, false);
    }

    /**
     * Creates a vertical layout with both spacing and padding enabled.
     * @return new CVerticalLayout with spacing and padding
     */
    public static CVerticalLayout withSpacingAndPadding() {
        return new CVerticalLayout(true, true, false);
    }

    /**
     * Creates a vertical layout with all spacing, padding, and margin enabled.
     * @return new CVerticalLayout with full spacing configuration
     */
    public static CVerticalLayout withFullSpacing() {
        return new CVerticalLayout(true, true, true);
    }

    /**
     * Creates a vertical layout for button groups with spacing.
     * @param buttons the buttons to add
     * @return new CVerticalLayout configured for buttons
     */
    public static CVerticalLayout forButtons(final Component... buttons) {
        final CVerticalLayout layout = withSpacing();
        layout.setWidthFull();
        layout.setDefaultHorizontalComponentAlignment(Alignment.START);
        layout.add(buttons);
        return layout;
    }

    /**
     * Fluent API for setting spacing.
     * @param spacing whether to enable spacing
     * @return this layout for method chaining
     */
    public CVerticalLayout withSpacing(final boolean spacing) {
        setSpacing(spacing);
        return this;
    }

    /**
     * Fluent API for setting padding.
     * @param padding whether to enable padding
     * @return this layout for method chaining
     */
    public CVerticalLayout withPadding(final boolean padding) {
        setPadding(padding);
        return this;
    }

    /**
     * Fluent API for setting margin.
     * @param margin whether to enable margin
     * @return this layout for method chaining
     */
    public CVerticalLayout withMargin(final boolean margin) {
        setMargin(margin);
        return this;
    }

    /**
     * Fluent API for setting width to full.
     * @return this layout for method chaining
     */
    public CVerticalLayout withWidthFull() {
        setWidthFull();
        return this;
    }

    /**
     * Fluent API for setting height to full.
     * @return this layout for method chaining
     */
    public CVerticalLayout withHeightFull() {
        setHeightFull();
        return this;
    }

    /**
     * Fluent API for setting size to full.
     * @return this layout for method chaining
     */
    public CVerticalLayout withSizeFull() {
        setSizeFull();
        return this;
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

    /**
     * Common initialization for all CVerticalLayout instances.
     */
    protected void initializeLayout() {
        CAuxillaries.setId(this);
    }
}
