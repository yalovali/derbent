package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.ui.component.IFormContainerComponent;
import tech.derbent.base.users.domain.CUserProjectSettings;

/** CAccordion - Enhanced base class for accordion components. Layer: View (MVC) Provides common accordion functionality with standardized styling and
 * behavior. */
public class CAccordion extends Accordion implements IFormContainerComponent {

	private static final long serialVersionUID = 1L;
	private final String accordionTitle;
	private final VerticalLayout baseLayout = new VerticalLayout();

	/** Constructor for CAccordion.
	 * @param title the title of the accordion panel */
	public CAccordion(final String title) {
		super();
		accordionTitle = title;
		addClassName("c-accordion");
		add(title, baseLayout);
		baseLayout.setClassName("c-accordion-content-layout");
	}

	/** Convenience method to add components to the base layout.
	 * @param components the components to add */
	public void addToContent(final Component... components) {
		baseLayout.add(components);
	}

	/** Convenience method to close this accordion panel. */
	public void closePanel() {
		close();
	}

	protected String createDeleteConfirmationMessage(@SuppressWarnings ("unused") CUserProjectSettings selected) {
		// TODO Auto-generated method stub
		return null;
	}

	/** Gets the title of this accordion.
	 * @return the accordion title */
	public String getAccordionTitle() { return accordionTitle; }

	@Override
	public VerticalLayout getBaseLayout() { return baseLayout; }

	/** Convenience method to open this accordion panel (index 0). */
	public void openPanel() {
		open(0);
	}

	public void removeAllFromContent() {
		baseLayout.removeAll();
	}
}
