package tech.derbent.abstracts.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;

import tech.derbent.abstracts.utils.CAuxillaries;

/**
 * CDiv - Enhanced base class for div containers in the application. Layer: View (MVC) Provides common initialization
 * patterns, utility methods for styling, sizing, and content management. Extends Vaadin Div with application-specific
 * enhancements.
 */
public class CDiv extends Div {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a flex spacer div that grows to fill available space.
     * 
     * @return new CDiv configured as flex spacer
     */
    public static CDiv createSpacer() {
        final CDiv spacer = new CDiv();
        spacer.getStyle().set("flex-grow", "1");
        return spacer;
    }

    /**
     * Creates a div with flex display and center alignment.
     * 
     * @return new CDiv with flex display and center alignment
     */
    public static CDiv withFlexCentered() {
        final CDiv div = withFlexDisplay();
        div.getStyle().set("align-items", "center");
        div.getStyle().set("justify-content", "center");
        return div;
    }

    /**
     * Creates a div with flex display.
     * 
     * @return new CDiv with flex display
     */
    public static CDiv withFlexDisplay() {
        final CDiv div = new CDiv();
        div.getStyle().set("display", "flex");
        return div;
    }

    /**
     * Creates a div with full height.
     * 
     * @return new CDiv with full height
     */
    public static CDiv withFullHeight() {
        final CDiv div = new CDiv();
        div.setHeightFull();
        return div;
    }

    /**
     * Creates a div with full size.
     * 
     * @return new CDiv with full size
     */
    public static CDiv withFullSize() {
        final CDiv div = new CDiv();
        div.setSizeFull();
        return div;
    }

    /**
     * Creates a div with full width.
     * 
     * @return new CDiv with full width
     */
    public static CDiv withFullWidth() {
        final CDiv div = new CDiv();
        div.setWidthFull();
        return div;
    }

    /**
     * Default constructor.
     */
    public CDiv() {
        super();
        initializeDiv();
    }

    /**
     * Constructor with initial components.
     */
    public CDiv(final Component... components) {
        super(components);
        initializeDiv();
    }

    public CDiv(final String text) {
        super(text);
    }

    /**
     * Common initialization for all CDiv instances.
     */
    protected void initializeDiv() {
        CAuxillaries.setId(this);
    }

    /**
     * Fluent API for setting background color.
     * 
     * @param backgroundColor
     *            the background color value
     * @return this div for method chaining
     */
    public CDiv withBackgroundColor(final String backgroundColor) {
        getStyle().set("background-color", backgroundColor);
        return this;
    }

    /**
     * Fluent API for setting border radius.
     * 
     * @param borderRadius
     *            the border radius value (e.g., "4px", "0.5rem")
     * @return this div for method chaining
     */
    public CDiv withBorderRadius(final String borderRadius) {
        getStyle().set("border-radius", borderRadius);
        return this;
    }

    /**
     * Fluent API for adding CSS class.
     * 
     * @param className
     *            the CSS class name to add
     * @return this div for method chaining
     */
    public CDiv withClassName(final String className) {
        addClassName(className);
        return this;
    }

    /**
     * Fluent API for setting height to full.
     * 
     * @return this div for method chaining
     */
    public CDiv withHeightFull() {
        setHeightFull();
        return this;
    }

    /**
     * Fluent API for setting margin.
     * 
     * @param margin
     *            the margin value (e.g., "10px", "1rem")
     * @return this div for method chaining
     */
    public CDiv withMargin(final String margin) {
        getStyle().set("margin", margin);
        return this;
    }

    /**
     * Fluent API for setting padding.
     * 
     * @param padding
     *            the padding value (e.g., "10px", "1rem")
     * @return this div for method chaining
     */
    public CDiv withPadding(final String padding) {
        getStyle().set("padding", padding);
        return this;
    }

    /**
     * Fluent API for setting size to full.
     * 
     * @return this div for method chaining
     */
    public CDiv withSizeFull() {
        setSizeFull();
        return this;
    }

    /**
     * Fluent API for setting CSS style property.
     * 
     * @param property
     *            the CSS property name
     * @param value
     *            the CSS property value
     * @return this div for method chaining
     */
    public CDiv withStyle(final String property, final String value) {
        getStyle().set(property, value);
        return this;
    }

    /**
     * Fluent API for setting width to full.
     * 
     * @return this div for method chaining
     */
    public CDiv withWidthFull() {
        setWidthFull();
        return this;
    }
}