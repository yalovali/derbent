package tech.derbent.api.ui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import tech.derbent.api.interfaces.IDetailsContainer;

/**
 * TabSheet wrapper that implements IDetailsContainer interface.
 * This allows TabSheet to be used in the unified container hierarchy.
 */
public class CDetailsTabSheet implements IDetailsContainer {

	private final TabSheet tabSheet;
	private final VerticalLayout baseLayout;

	public CDetailsTabSheet() {
		this.tabSheet = new TabSheet();
		this.baseLayout = new VerticalLayout();
		this.baseLayout.setPadding(false);
		this.baseLayout.setMargin(false);
		this.baseLayout.setWidthFull();
	}

	@Override
	public void addItem(Component component) {
		// TabSheet requires a name, so throw an exception to require explicit naming
		throw new UnsupportedOperationException("TabSheet requires a name for each item. Use addItem(String name, Component component) instead.");
	}

	@Override
	public void addItem(String name, Component component) {
		tabSheet.add(name, component);
	}

	@Override
	public VerticalLayout getBaseLayout() {
		// For TabSheet container itself, return the base layout
		// This is used when fields need to be added directly to the container
		return baseLayout;
	}

	@Override
	public Component asComponent() {
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
}
