package tech.derbent.abstracts.utils;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAccordion;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;

public class CPanelDetails extends CAccordion {

	private static final long serialVersionUID = 1L;

	public CPanelDetails(final String title, final CEntityFormBuilder<?> detailsBuilder) {
		super(title);
		getBaseLayout().add(detailsBuilder.getFormLayout());
	}

	// Override this method to customize the panel content creation
	protected void createPanelContent() {}

	public void processLine(final int counter, final CScreen screen,
		final CScreenLines line, final CEntityFormBuilder<?> detailsBuilder)
		throws Exception {

		try {
			detailsBuilder.addFieldLine(screen.getEntityType(), line);
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("Error processing line: " + line.getFieldCaption(), e);
		}
	}
}
