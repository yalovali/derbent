package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.ui.component.IFormContainerComponent;

/** CAccordion - Enhanced base class for accordion components. Layer: View (MVC) Provides common accordion functionality with standardized styling and
 * behavior. */
public class CTab extends Accordion implements IFormContainerComponent {

	private static final long serialVersionUID = 1L;
	private final String accordionTitle;
	private final VerticalLayout baseLayout = new VerticalLayout();

	public CTab(final String title) {
		super();
		accordionTitle = title;
		initializeComponent();
	}

	public void addToContent(final Component... components) {
		baseLayout.add(components);
	}

	public void clearContent() {
		baseLayout.removeAll();
	}

	public void closePanel() {
		close();
	}

	public String getAccordionTitle() { return accordionTitle; }

	@Override
	public VerticalLayout getBaseLayout() { return baseLayout; }

	private void initializeComponent() {
		add(accordionTitle, baseLayout);
		addClassName("c-accordion");
		setSizeFull();
		baseLayout.setWidthFull();
		baseLayout.setPadding(false);
		baseLayout.setMargin(false);
		baseLayout.setClassName("c-accordion-content-layout");
	}

	public void openPanel() {
		open(0);
	}

	public void removeAllFromContent() {
		baseLayout.removeAll();
	}

	public void setContentSpacing(final boolean spacing) {
		baseLayout.setSpacing(spacing);
	}
}
