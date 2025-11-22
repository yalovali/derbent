package tech.derbent.api.ui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import tech.derbent.api.interfaces.IDetailsContainer;

/**
 * TabSheet wrapper that implements IDetailsContainer interface.
 * This allows TabSheet to be used in the unified container hierarchy.
 * 
 * TabSheets can contain:
 * - Regular fields (added to base layout)
 * - Other TabSheets (as named tabs)
 * - Panels/CPanelDetails (as named tabs)
 * All of these can coexist within the same TabSheet.
 */
public class CDetailsTabSheet implements IDetailsContainer {

	private final TabSheet tabSheet;
	private final VerticalLayout baseLayout;
	private final String caption;

	public CDetailsTabSheet(String caption) {
		this.caption = caption;
		this.tabSheet = new TabSheet();
		this.baseLayout = new VerticalLayout();
		this.baseLayout.setPadding(false);
		this.baseLayout.setMargin(false);
		this.baseLayout.setWidthFull();
	}

	@Override
	public void addItem(Component component) {
		// Add component directly to base layout (for fields)
		baseLayout.add(component);
	}

	@Override
	public void addItem(String name, Component component) {
		// Add as a named tab
		tabSheet.add(name, component);
	}

	@Override
	public VerticalLayout getBaseLayout() {
		// Return the base layout where fields can be added directly
		return baseLayout;
	}

	@Override
	public Component asComponent() {
		// If baseLayout has content, add it as the first tab
		if (baseLayout.getComponentCount() > 0) {
			// Create a wrapper to ensure base layout content appears first
			VerticalLayout wrapper = new VerticalLayout();
			wrapper.setPadding(false);
			wrapper.setMargin(false);
			wrapper.setWidthFull();
			wrapper.add(baseLayout);
			wrapper.add(tabSheet);
			return wrapper;
		}
		return tabSheet;
	}

	/**
	 * Gets the underlying TabSheet component.
	 * 
	 * @return the TabSheet
	 */
	public TabSheet getTabSheet() {
		return tabSheet;
	}

	/**
	 * Gets the caption/title of this TabSheet.
	 * 
	 * @return the caption
	 */
	public String getCaption() {
		return caption;
	}
}
