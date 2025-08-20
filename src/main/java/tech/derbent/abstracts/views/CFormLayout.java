package tech.derbent.abstracts.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;

import tech.derbent.abstracts.utils.CAuxillaries;

/**
 * CFormLayout - Enhanced base class for form layouts in the application. Layer: View (MVC) Provides common
 * initialization patterns, utility methods for responsive design, field organization, and styling. Extends Vaadin
 * FormLayout with application-specific enhancements.
 */
public class CFormLayout extends FormLayout {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public CFormLayout() {
        super();
        initializeFormLayout();
    }

    /**
     * Constructor with initial components.
     */
    public CFormLayout(final Component... components) {
        super(components);
        initializeFormLayout();
    }

    /**
     * Creates a single-column form layout.
     * 
     * @return new CFormLayout with single column
     */
    public static CFormLayout singleColumn() {
        final CFormLayout form = new CFormLayout();
        form.setResponsiveSteps(new ResponsiveStep("0", 1));
        return form;
    }

    /**
     * Creates a two-column form layout for larger screens.
     * 
     * @return new CFormLayout with responsive columns
     */
    public static CFormLayout twoColumn() {
        final CFormLayout form = new CFormLayout();
        form.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));
        return form;
    }

    /**
     * Creates a responsive form layout with multiple breakpoints.
     * 
     * @return new CFormLayout with responsive design
     */
    public static CFormLayout responsive() {
        final CFormLayout form = new CFormLayout();
        form.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2),
                new ResponsiveStep("1000px", 3));
        return form;
    }

    /**
     * Creates a compact form layout with reduced spacing.
     * 
     * @return new CFormLayout with compact styling
     */
    public static CFormLayout compact() {
        final CFormLayout form = new CFormLayout();
        form.addClassName("compact-form");
        form.getStyle().set("gap", "var(--lumo-space-s)");
        return form;
    }

    /**
     * Creates a form layout for dialog usage.
     * 
     * @return new CFormLayout optimized for dialogs
     */
    public static CFormLayout forDialog() {
        final CFormLayout form = singleColumn();
        form.setMaxWidth("400px");
        form.getStyle().set("margin", "0 auto");
        return form;
    }

    /**
     * Creates a form layout for card/panel usage.
     * 
     * @return new CFormLayout optimized for cards
     */
    public static CFormLayout forCard() {
        final CFormLayout form = twoColumn();
        form.getStyle().set("padding", "var(--lumo-space-m)");
        form.getStyle().set("background", "var(--lumo-base-color)");
        form.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        return form;
    }

    /**
     * Fluent API for setting responsive steps.
     * 
     * @param steps
     *            the responsive steps to set
     * @return this form for method chaining
     */
    public CFormLayout withResponsiveSteps(final ResponsiveStep... steps) {
        setResponsiveSteps(steps);
        return this;
    }

    /**
     * Fluent API for setting maximum width.
     * 
     * @param maxWidth
     *            the maximum width (e.g., "400px", "50%")
     * @return this form for method chaining
     */
    public CFormLayout withMaxWidth(final String maxWidth) {
        setMaxWidth(maxWidth);
        return this;
    }

    /**
     * Fluent API for setting width to full.
     * 
     * @return this form for method chaining
     */
    public CFormLayout withWidthFull() {
        setWidthFull();
        return this;
    }

    /**
     * Fluent API for adding CSS class.
     * 
     * @param className
     *            the CSS class name to add
     * @return this form for method chaining
     */
    public CFormLayout withClassName(final String className) {
        addClassName(className);
        return this;
    }

    /**
     * Fluent API for setting CSS style property.
     * 
     * @param property
     *            the CSS property name
     * @param value
     *            the CSS property value
     * @return this form for method chaining
     */
    public CFormLayout withStyle(final String property, final String value) {
        getStyle().set(property, value);
        return this;
    }

    /**
     * Fluent API for setting padding.
     * 
     * @param padding
     *            the padding value (e.g., "var(--lumo-space-m)")
     * @return this form for method chaining
     */
    public CFormLayout withPadding(final String padding) {
        getStyle().set("padding", padding);
        return this;
    }

    /**
     * Fluent API for setting gap between form items.
     * 
     * @param gap
     *            the gap value (e.g., "var(--lumo-space-s)")
     * @return this form for method chaining
     */
    public CFormLayout withGap(final String gap) {
        getStyle().set("gap", gap);
        return this;
    }

    /**
     * Adds a component that spans across all columns.
     * 
     * @param component
     *            the component to add
     * @return this form for method chaining
     */
    public CFormLayout withFullWidthComponent(final Component component) {
        add(component);
        // Set a large colspan to span across all columns
        setColspan(component, 3);
        return this;
    }

    /**
     * Adds a component with specified column span.
     * 
     * @param component
     *            the component to add
     * @param colspan
     *            the number of columns to span
     * @return this form for method chaining
     */
    public CFormLayout withComponent(final Component component, final int colspan) {
        add(component);
        setColspan(component, colspan);
        return this;
    }

    /**
     * Common initialization for all CFormLayout instances.
     */
    protected void initializeFormLayout() {
        CAuxillaries.setId(this);
        // Set default responsive behavior
        setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));
    }
}