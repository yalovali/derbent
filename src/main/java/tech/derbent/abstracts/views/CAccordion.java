package tech.derbent.abstracts.views;

import com.vaadin.flow.component.accordion.Accordion;

public class CAccordion extends Accordion {

	private static final long serialVersionUID = 1L;
	private static final String title = "";

	/**
	 * Default constructor for CAccordion.
	 */
	public CAccordion(String title) {
		super();
		addClassName("c-accordion");
		setWidthFull();
		setHeightFull();
	}
}
