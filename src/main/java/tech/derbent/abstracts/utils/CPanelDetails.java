package tech.derbent.abstracts.utils;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAccordion;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;

public class CPanelDetails extends CAccordion {

	private static final long serialVersionUID = 1L;

	private final String name;

	public CPanelDetails(final String name, final String title) {
		super(title);
		this.name = name;
	}

	// Override this method to customize the panel content creation
	protected void createPanelContent() {}

	public String getName() { return name; }

	public void processLine(final int counter, final CScreen screen,
		final CScreenLines line, final CEntityFormBuilder<?> formBuilder)
		throws Exception {

		try {
			formBuilder.addFieldLine(screen.getEntityType(), line, getBaseLayout());
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("Error processing line: " + line.getFieldCaption(), e);
		}
	}
}
