package tech.derbent.api.interfaces;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Common interface for all container components in the details builder system.
 * Containers can hold form fields and other containers (like CPanelDetails).
 * 
 * This interface enables a simplified algorithm where:
 * 1. A container is created
 * 2. Items are added to it until an end-of-section marker
 * 3. The container determines how to display its items (tabs, accordion, etc.)
 */
public interface IDetailsContainer {

	/**
	 * Adds a component to this container.
	 * 
	 * @param component the component to add
	 */
	void addItem(Component component);

	/**
	 * Adds a component to this container with a name/label.
	 * Used for containers that organize items by name (like tabs).
	 * 
	 * @param name the name/label for the component
	 * @param component the component to add
	 */
	void addItem(String name, Component component);

	/**
	 * Gets the base layout where form fields should be added.
	 * For simple containers, this might be the container itself.
	 * For complex containers like accordions, this is the content area.
	 * 
	 * @return the base layout for adding fields
	 */
	VerticalLayout getBaseLayout();

	/**
	 * Gets this container as a Vaadin component.
	 * 
	 * @return the component representation
	 */
	Component asComponent();
}
