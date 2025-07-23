package tech.derbent.abstracts.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class CAccordion extends Accordion {

	private static final long serialVersionUID = 1L;
	private final VerticalLayout baseLayout = new VerticalLayout();
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	/**
	 * Default constructor for CAccordion.
	 */
	public CAccordion(final String title) {
		super();
		addClassName("c-accordion");
		getStyle().set("min-width", "300px");
		setWidthFull();
		setHeightFull();
		add(title, baseLayout);
	}

	public VerticalLayout getBaseLayout() { return baseLayout; }
}
