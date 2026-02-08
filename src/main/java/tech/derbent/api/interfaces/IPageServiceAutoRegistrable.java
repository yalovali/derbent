package tech.derbent.api.interfaces;

import com.vaadin.flow.component.Component;
import tech.derbent.api.services.pageservice.CPageService;

/** Interface for components that support automatic event binding with CPageService.
 * <p>
 * <b>CURRENT PATTERN (2026-02-08):</b><br>
 * Page services call <code>registerComponent()</code> directly in factory methods. The <code>registerWithPageService()</code> method is now redundant
 * and kept only for backward compatibility.
 * <p>
 * <b>RECOMMENDED Pattern:</b>
 *
 * <pre>
 * // In CPageService factory method:
 * public Component createComponentSprintItems() {
 *     final CComponentListSprintItems component = new CComponentListSprintItems(...);
 *     registerComponent(component.getComponentName(), component);  // ✅ MANDATORY
 *     return component;
 * }
 * </pre>
 * <p>
 * <b>Benefits:</b>
 * <ul>
 * <li>Centralized registration in page service (single source of truth)</li>
 * <li>Explicit component lifecycle management</li>
 * <li>Consistent pattern across all page services</li>
 * <li>Component name derived from component itself via getComponentName()</li>
 * </ul>
 * @see CPageService#registerComponent(String, Component)
 * @see CPageService#bindMethods(CPageService) */
public interface IPageServiceAutoRegistrable {

	/** Returns the component name to use for method binding.
	 * <p>
	 * This name is used in the on_{componentName}_{action} pattern for automatic method binding. For example, "sprintItems" will bind to methods
	 * like:
	 * <ul>
	 * <li>on_sprintItems_drop(Component, Object) - handle drop events</li>
	 * <li>on_sprintItems_change(Component, Object) - handle value changes</li>
	 * </ul>
	 * <p>
	 * Note: dragStart and dragEnd handlers are rarely needed in application code since all drag data is carried in events. Use drop handlers with
	 * event.getDraggedItem() and event.getSourceList() instead.
	 * @return The component name for method binding (e.g., "sprintItems", "backlogItems", "grid") */
	String getComponentName();
}
