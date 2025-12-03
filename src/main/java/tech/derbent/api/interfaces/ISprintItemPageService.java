package tech.derbent.api.interfaces;

import com.vaadin.flow.component.Component;
import tech.derbent.api.entityOfProject.domain.CProjectItem;

/** ISprintItemPageService - Interface for page services that support sprint item display functionality.
 * <p>
 * This interface must be implemented by page services of entities that can be included in sprints (entities implementing ISprintableItem). It ensures
 * that the page service provides the necessary methods to create visual components for displaying sprint items.
 * </p>
 * <p>
 * <strong>Usage Pattern:</strong> When an entity implements ISprintableItem, its corresponding page service should implement this interface to provide
 * sprint display support.
 * </p>
 * <p>
 * <strong>Example:</strong>
 * </p>
 * 
 * <pre>
 * // Entity
 * public class CActivity extends CProjectItem&lt;CActivity&gt; implements ISprintableItem { }
 * 
 * // Page Service
 * public class CPageServiceActivity extends CPageServiceDynamicPage&lt;CActivity&gt;
 * 		implements ISprintItemPageService&lt;CActivity&gt; {
 * 	&#64;Override
 * 	public Component getSprintItemWidget(CActivity entity) {
 * 		return new CComponentWidgetActivity(entity);
 * 	}
 * }
 * </pre>
 * @param <T> the entity type that implements ISprintableItem
 * @author Derbent Framework
 * @since 1.0
 * @see ISprintableItem */
@FunctionalInterface
public interface ISprintItemPageService<T extends CProjectItem<?> & ISprintableItem> {

	/** Creates a widget component for displaying the entity when shown as a sprint item.
	 * <p>
	 * This widget is used to display the entity within sprint contexts, such as: - Sprint item lists - Sprint detail views - Sprint boards and planning
	 * views
	 * </p>
	 * <p>
	 * The widget should provide a compact, informative representation suitable for sprint planning and tracking.
	 * </p>
	 * @param entity the sprintable entity to create a widget for
	 * @return a Component representing the sprint item widget, typically a CComponentWidgetEntity implementation */
	Component getSprintItemWidget(T entity);
}
