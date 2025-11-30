package tech.derbent.api.grid.widget;

import com.vaadin.flow.component.Component;
import tech.derbent.api.entity.domain.CEntityDB;

/** IEntityWidgetProvider - Interface for providing custom widget components for entity display in grids.
 * <p>
 * This interface enables entities to define their own rich visual representation as widgets. The widgets are used in grid columns instead of
 * traditional text-based columns, providing a more engaging and informative display.
 * </p>
 * <p>
 * Each entity type can implement this interface to provide customized widgets that display: - Entity name and description - Status with color coding
 * - Dates and time information - Assigned users with icons - Progress indicators - Custom action buttons
 * </p>
 * <p>
 * <strong>Usage:</strong> Implement this interface in entity service classes or page service classes to provide custom widget rendering for entities
 * in grids.
 * </p>
 *
 * @param <T> the entity type
 * @author Derbent Framework
 * @since 1.0
 * @see CComponentWidgetEntity */
@FunctionalInterface
public interface IComponentWidgetEntityProvider<T extends CEntityDB<?>> {

	/** Creates a widget component for displaying the given entity.
	 * <p>
	 * The widget should display relevant entity information in a visually appealing way. It may include: - Primary information (name, description) -
	 * Status indicators - Date information - Assigned user - Progress bars - Action buttons
	 * </p>
	 *
	 * @param entity the entity to create a widget for
	 * @return a Component representing the entity widget, or null if widget cannot be created */
	Component getComponentWidget(T entity);
}
