package tech.derbent.api.utils;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.ui.component.IFormContainerComponent;
import tech.derbent.api.ui.component.basic.CAccordion;
import tech.derbent.api.ui.component.basic.CContainerDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayoutTop;
import tech.derbent.api.users.domain.CUser;

public class CPanelDetails extends CVerticalLayoutTop {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPanelDetails.class);
	private static final long serialVersionUID = 1L;
	private Component component = null; // call it with getFormContainerComponent
	final Map<String, Component> componentMap;
	final Map<String, CHorizontalLayout> horizontalLayoutMap;
	private final String name;

	public CPanelDetails(final String name, final String title, final CUser user) {
		super();
		setSizeFull();
		addClassName("cpaneldetails");
		// set height minimum?
		// setHeightUndefined();
		if (user.getAttributeDisplaySectionsAsTabs()) {
			component = new CContainerDiv(title);
			if (component instanceof HasSize) {
				((HasSize) component).setSizeFull();
			}
			component.addClassName("cpaneldetails-tabsection");
		} else {
			component = new CAccordion(title);
		}
		this.name = name;
		componentMap = new HashMap<>();
		horizontalLayoutMap = new HashMap<>();
		add(component);
	}

	// Override this method to customize the panel content creation
	protected void createPanelContent() { /*****/
	}

	public VerticalLayout getBaseLayout() { return getFormContainerComponent().getBaseLayout(); }

	IFormContainerComponent getFormContainerComponent() { return (IFormContainerComponent) component; }

	public String getName() { return name; }

	public void processLine(final IContentOwner contentOwner, final CDetailSection screen, final CDetailLines line, final CFormBuilder<?> formBuilder,
			final Map<String, Component> centralComponentMap) throws Exception {
		try {
			formBuilder.addFieldLine(contentOwner, screen.getEntityType(), line, getBaseLayout(), centralComponentMap, horizontalLayoutMap);
		} catch (final Exception e) {
			LOGGER.error("Error processing detail line for field {}", line.getFieldCaption());
			throw e;
		}
	}
}
