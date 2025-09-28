package tech.derbent.api.utils;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.views.components.CAccordion;
import tech.derbent.api.views.components.CHorizontalLayout;
import tech.derbent.screens.domain.CDetailLines;
import tech.derbent.screens.domain.CDetailSection;

public class CPanelDetails extends CAccordion {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPanelDetails.class);
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

	public void processLine(IContentOwner contentOwner, final int counter, final CDetailSection screen, final CDetailLines line,
			final CFormBuilder<?> formBuilder) throws Exception {
		try {
			formBuilder.addFieldLine(contentOwner, screen.getEntityType(), line, getBaseLayout(), componentMap, horizontalLayoutMap);
		} catch (final Exception e) {
			LOGGER.error("Error processing detail line for field '{}': {}", line.getFieldCaption(), e.getMessage(), e);
			throw new Exception("Error processing line: " + line.getFieldCaption(), e);
		}
	}
}
