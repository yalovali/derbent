package tech.derbent.abstracts.utils;

import java.util.HashMap;
import java.util.Map;
import com.vaadin.flow.component.Component;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAccordion;
import tech.derbent.abstracts.views.CHorizontalLayout;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;

public class CPanelDetails extends CAccordion {
	private static final long serialVersionUID = 1L;
	private final String name;
	final Map<String, Component> componentMap;
	final Map<String, CHorizontalLayout> horizontalLayoutMap;

	public CPanelDetails(final String name, final String title) {
		super(title);
		this.name = name;
		this.componentMap = new HashMap<>();
		this.horizontalLayoutMap = new HashMap<>();
	}

	// Override this method to customize the panel content creation
	protected void createPanelContent() {}

	public Component getComponentByName(final String componentName) {
		return componentMap.get(componentName);
	}

	public String getName() { return name; }

	public void processLine(final int counter, final CScreen screen, final CScreenLines line, final CEntityFormBuilder<?> formBuilder)
			throws Exception {
		try {
			formBuilder.addFieldLine(screen.getEntityType(), line, getBaseLayout(), componentMap, horizontalLayoutMap);
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("Error processing line: " + line.getFieldCaption(), e);
		}
	}
}
