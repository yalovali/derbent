package tech.derbent.api.grid.widget;

import com.vaadin.flow.component.Component;
import tech.derbent.api.entity.domain.CEntityDB;

/**
 * Interface for creating entity display widgets used in grid columns.
 * Each entity type can have its own display widget that shows the entity
 * in a rich format with name, description, dates, icons, etc.
 *
 * @param <EntityClass> the entity type
 */
public interface IEntityDisplayWidget<EntityClass extends CEntityDB<EntityClass>> {

	/**
	 * Creates a display widget component for the given entity.
	 * The widget should display the entity's key information in a visually
	 * appealing format suitable for use in a grid cell.
	 *
	 * @param entity the entity to display
	 * @return a Component that displays the entity information
	 */
	Component createWidget(EntityClass entity);

	/**
	 * Gets the entity class this widget handles.
	 *
	 * @return the entity class
	 */
	Class<EntityClass> getEntityClass();
}
