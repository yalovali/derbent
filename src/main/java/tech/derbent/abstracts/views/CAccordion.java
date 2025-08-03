package tech.derbent.abstracts.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * CAccordion - Enhanced base class for accordion components. Layer: View (MVC) Provides common accordion functionality
 * with standardized styling and behavior.
 */
public class CAccordion extends Accordion {

    private static final long serialVersionUID = 1L;

    private final VerticalLayout baseLayout = new VerticalLayout();

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final String accordionTitle;

    /**
     * Constructor for CAccordion.
     * 
     * @param title
     *            the title of the accordion panel
     */
    public CAccordion(final String title) {
        super();
        LOGGER.debug("Creating CAccordion with title: {}", title);
        this.accordionTitle = title;
        addClassName("c-accordion");
        // getStyle().("min-width", "300px"); setWidthFull(); setMin
        add(title, baseLayout);
        baseLayout.setWidthFull();
        baseLayout.setPadding(false);
        baseLayout.setMargin(false);
        baseLayout.setClassName("c-accordion-content-layout");
    }

    /**
     * Convenience method to add components to the base layout.
     * 
     * @param components
     *            the components to add
     */
    public void addToContent(final Component... components) {
        LOGGER.debug("Adding {} components to accordion content", components.length);
        baseLayout.add(components);
    }

    /**
     * Convenience method to remove all components from the base layout.
     */
    public void clearContent() {
        LOGGER.debug("Clearing all content from accordion");
        baseLayout.removeAll();
    }

    /**
     * Convenience method to close this accordion panel.
     */
    public void closePanel() {
        close();
    }

    /**
     * Gets the title of this accordion.
     * 
     * @return the accordion title
     */
    public String getAccordionTitle() {
        return accordionTitle;
    }

    /**
     * Gets the base layout for adding components.
     * 
     * @return the base VerticalLayout
     */
    public VerticalLayout getBaseLayout() {
        return baseLayout;
    }

    /**
     * Convenience method to open this accordion panel (index 0).
     */
    public void openPanel() {
        LOGGER.debug("Opening accordion panel: {}", accordionTitle);
        open(0);
    }

    /**
     * Convenience method to set spacing for the base layout.
     * 
     * @param spacing
     *            whether to enable spacing
     */
    public void setContentSpacing(final boolean spacing) {
        baseLayout.setSpacing(spacing);
    }
}
