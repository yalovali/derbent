package tech.derbent.api.views.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/** CAccordion - Enhanced base class for accordion components. Layer: View (MVC) Provides common accordion functionality with standardized styling and
 * behavior. */
public class CTab extends Accordion implements IFormContainerComponent {

	private static final long serialVersionUID = 1L;
	private final String accordionTitle;
	private final VerticalLayout baseLayout = new VerticalLayout();
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	/** Constructor for CAccordion.
	 * @param title the title of the accordion panel */
	public CTab(final String title) {
		super();
		// LOGGER.debug("Creating CAccordion with title: {}", title);
		accordionTitle = title;
		addClassName("c-accordion");
		// getStyle().("min-width", "300px"); setWidthFull(); setMin
		add(title, baseLayout);
		baseLayout.setWidthFull();
		baseLayout.setPadding(false);
		baseLayout.setMargin(false);
		baseLayout.setClassName("c-accordion-content-layout");
	}

	/** Convenience method to add components to the base layout.
	 * @param components the components to add */
	public void addToContent(final Component... components) {
		baseLayout.add(components);
	}

	/** Convenience method to remove all components from the base layout. */
	public void clearContent() {
		baseLayout.removeAll();
	}

	/** Convenience method to close this accordion panel. */
	public void closePanel() {
		close();
	}

	/** Gets the title of this accordion.
	 * @return the accordion title */
	public String getAccordionTitle() { return accordionTitle; }

	public VerticalLayout getBaseLayout() { return baseLayout; }

	/** Convenience method to open this accordion panel (index 0). */
	public void openPanel() {
		open(0);
	}

	public void removeAllFromContent() {
		baseLayout.removeAll();
	}

	/** Convenience method to set spacing for the base layout.
	 * @param spacing whether to enable spacing */
	public void setContentSpacing(final boolean spacing) {
		baseLayout.setSpacing(spacing);
	}
}
