package tech.derbent.api.interfaces;

import tech.derbent.api.services.pageservice.CPageService;

/**
 * Interface for components that can automatically register themselves with a CPageService.
 * <p>
 * Components implementing this interface can be automatically registered with the page service
 * for event binding (drag-drop, value changes, etc.) without requiring manual registerComponent()
 * and bindMethods() calls in the page service.
 * <p>
 * This interface enforces a unified pattern for component registration and eliminates redundant
 * registration code in page services.
 * <p>
 * <b>Benefits:</b>
 * <ul>
 * <li>Eliminates redundant registerComponent() + bindMethods() calls</li>
 * <li>Enforces consistent registration pattern</li>
 * <li>Simplifies page service code</li>
 * <li>Makes component registration self-documenting</li>
 * </ul>
 * <p>
 * <b>Usage Pattern:</b>
 * <pre>
 * // Before (manual registration):
 * componentSprintItems = new CComponentListSprintItems(...);
 * registerComponent("sprintItems", componentSprintItems);
 * bindMethods(this);
 * 
 * // After (automatic registration):
 * componentSprintItems = new CComponentListSprintItems(...);
 * componentSprintItems.registerWithPageService(this);
 * </pre>
 * <p>
 * <b>Implementation Example:</b>
 * <pre>
 * public class CComponentListSprintItems extends CComponentListEntityBase 
 *         implements IPageServiceAutoRegistrable {
 *     
 *     &#64;Override
 *     public void registerWithPageService(CPageService<?> pageService) {
 *         Check.notNull(pageService, "Page service cannot be null");
 *         pageService.registerComponent("sprintItems", this);
 *         pageService.bindMethods(pageService);
 *         LOGGER.debug("[BindDebug] {} auto-registered with page service", 
 *             getClass().getSimpleName());
 *     }
 *     
 *     &#64;Override
 *     public String getComponentName() {
 *         return "sprintItems";
 *     }
 * }
 * </pre>
 * 
 * @see CPageService#registerComponent(String, com.vaadin.flow.component.Component)
 * @see CPageService#bindMethods(CPageService)
 */
public interface IPageServiceAutoRegistrable {

	/**
	 * Registers this component with the provided page service for automatic event binding.
	 * <p>
	 * Implementations should:
	 * <ol>
	 * <li>Call pageService.registerComponent(getComponentName(), this)</li>
	 * <li>Call pageService.bindMethods(pageService)</li>
	 * <li>Log the registration for debugging</li>
	 * </ol>
	 * <p>
	 * This method is called once during component initialization in the page service.
	 * 
	 * @param pageService The page service to register with
	 * @throws IllegalArgumentException if pageService is null
	 */
	void registerWithPageService(CPageService<?> pageService);

	/**
	 * Returns the component name to use for method binding.
	 * <p>
	 * This name is used in the on_{componentName}_{action} pattern for automatic
	 * method binding. For example, "sprintItems" will bind to methods like:
	 * <ul>
	 * <li>on_sprintItems_dragStart(Component, Object)</li>
	 * <li>on_sprintItems_dragEnd(Component, Object)</li>
	 * <li>on_sprintItems_drop(Component, Object)</li>
	 * <li>on_sprintItems_change(Component, Object)</li>
	 * </ul>
	 * 
	 * @return The component name for method binding (e.g., "sprintItems", "backlogItems", "grid")
	 */
	String getComponentName();
}
