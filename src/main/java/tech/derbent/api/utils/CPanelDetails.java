package tech.derbent.api.utils;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IDetailsContainer;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.ui.component.CAccordion;
import tech.derbent.api.ui.component.CDiv;
import tech.derbent.api.ui.component.CHorizontalLayout;
import tech.derbent.api.ui.component.IFormContainerComponent;

public class CPanelDetails extends CDiv implements IDetailsContainer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPanelDetails.class);
	private static final long serialVersionUID = 1L;
	private final String name;
	final Map<String, Component> componentMap;
	final Map<String, CHorizontalLayout> horizontalLayoutMap;
	private Component component = null; // call it with getFormContainerComponent

	/**
	 * Creates a CPanelDetails with an accordion layout.
	 * 
	 * @param name the internal name of the panel
	 * @param title the display title
	 */
	public CPanelDetails(final String name, final String title) {
		super();
		setWidthFull();
		component = new CAccordion(title);
		this.name = name;
		this.componentMap = new HashMap<>();
		this.horizontalLayoutMap = new HashMap<>();
		add(component);
	}

	// Override this method to customize the panel content creation
	protected void createPanelContent() {}

	@Override
	public void addItem(Component component) {
		getBaseLayout().add(component);
	}

	@Override
	public void addItem(String name, Component component) {
		// CPanelDetails doesn't use named items, so just add normally
		addItem(component);
	}

	@Override
	public Component asComponent() {
		return this;
	}

	@Override
	public VerticalLayout getBaseLayout() { return getFormContainerComponent().getBaseLayout(); }

	public Component getComponentByName(final String componentName) {
		return componentMap.get(componentName);
	}

	IFormContainerComponent getFormContainerComponent() { return (IFormContainerComponent) component; }

	public String getName() { return name; }

	public void processLine(final IContentOwner contentOwner, final int counter, final CDetailSection screen, final CDetailLines line,
			final CFormBuilder<?> formBuilder) throws Exception {
		try {
			formBuilder.addFieldLine(contentOwner, screen.getEntityType(), line, getBaseLayout(), componentMap, horizontalLayoutMap);
		} catch (final Exception e) {
			LOGGER.error("Error processing detail line for field {}", line.getFieldCaption());
			throw e;
		}
	}
}
