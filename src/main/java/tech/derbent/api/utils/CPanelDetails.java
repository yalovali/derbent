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
import tech.derbent.api.ui.constants.CUIConstants;
import tech.derbent.api.users.domain.CUser;

public class CPanelDetails extends CVerticalLayoutTop {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPanelDetails.class);
	private static final long serialVersionUID = 1L;
	private Component component = null; // call it with getFormContainerComponent
	final Map<String, Component> componentMap;
	private final String name;

	public CPanelDetails(final String name, final String title, final CUser user) {
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
		// give a little top spacing to avoid content being too close to the top edge of the panel
		getStyle().set("padding-top", CUIConstants.GAP_STANDARD);
		getStyle().set("gap", CUIConstants.GAP_EXTRA_TINY);
		componentMap = new HashMap<>();
		add(component);
	}

	// Override this method to customize the panel content creation
	protected void createPanelContent() { /*****/
	}

	public VerticalLayout getBaseLayout() { return getFormContainerComponent().getBaseLayout(); }

	IFormContainerComponent getFormContainerComponent() { return (IFormContainerComponent) component; }

	public String getName() { return name; }

	/** Processes a detail line by adding it to the form container. 
	 * CRITICAL: This method uses the centralized horizontal layout map passed from CDetailsBuilder 
	 * to ensure all layouts across all panels are globally searchable by CPageService. */
	public void processLine(final IContentOwner contentOwner, final CDetailSection screen, final CDetailLines line, final CFormBuilder<?> formBuilder,
			final Map<String, Component> centralComponentMap, final Map<String, CHorizontalLayout> centralHorizontalLayoutMap) throws Exception {
		try {
			Check.notNull(line, "CDetailLines is required for processing a field line");
			Check.notNull(formBuilder, "CFormBuilder must be provided for line processing");
			Check.notNull(centralComponentMap, "Centralized component map is required to avoid field lookup failures");
			Check.notNull(centralHorizontalLayoutMap, "Centralized horizontal layout map is required for cross-panel binding");
			
			// CRITICAL: The horizontalLayoutMap must be centralized so it can be retrieved 
			// from CFormBuilder during the bind() phase in CPageService implementations.
			formBuilder.addFieldLine(contentOwner, screen.getEntityType(), line, getBaseLayout(), centralComponentMap, centralHorizontalLayoutMap);
		} catch (final Exception e) {
			LOGGER.error("Error processing detail line for field {}", line.getFieldCaption());
			throw e;
		}
	}
}
