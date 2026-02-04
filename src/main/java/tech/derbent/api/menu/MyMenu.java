package tech.derbent.api.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom menu annotation that preserves hierarchical ordering as String.
 * 
 * Replaces Vaadin's @Menu annotation to support 3+ level menu hierarchies
 * without data loss caused by Double conversion.
 * 
 * Example usage:
 * <pre>
 * {@literal @}MyMenu(
 *     title = "Project.Activities.Type1",
 *     order = "5.4.3",  // ← Preserved exactly as written!
 *     icon = "vaadin:tasks",
 *     route = "activities/type1"
 * )
 * {@literal @}Route(value = "activities/type1", layout = MainLayout.class)
 * {@literal @}PageTitle("Activity Types")
 * public class CActivityTypeView extends CAbstractPage {
 *     // View implementation
 * }
 * </pre>
 * 
 * Benefits over @Menu:
 * - ✅ Supports unlimited hierarchy levels
 * - ✅ Supports multi-digit positions (e.g., "10.20.30")
 * - ✅ No ambiguity: "5.4.3" ≠ "5.43" ≠ "5.4.30"
 * - ✅ Human-readable in source code
 * 
 * Order format:
 * - "5" - Single level, position 5
 * - "4.1" - Two levels: parent at 4, child at 1
 * - "10.20.30" - Three levels: grandparent at 10, parent at 20, child at 30
 * - "523.123" - Two levels: parent at 523, child at 123
 * 
 * @see MyMenuEntry
 * @see MyMenuConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyMenu {
	
	/**
	 * Menu title with hierarchy separated by dots.
	 * Example: "Project.Activities.Type1"
	 * 
	 * @return the hierarchical menu title
	 */
	String title();
	
	/**
	 * Menu ordering as String with hierarchy separated by dots.
	 * Each part is an independent integer position.
	 * 
	 * CRITICAL: This is kept as String to preserve exact structure!
	 * Unlike Vaadin's @Menu which uses Double (causing data loss for 3+ levels),
	 * this String field preserves the exact hierarchical structure.
	 * 
	 * Examples:
	 * - "5" - Single level, position 5
	 * - "4.1" - Two levels: parent at 4, child at 1
	 * - "10.20.30" - Three levels: grandparent at 10, parent at 20, child at 30
	 * - "523.123" - Two levels: parent at 523, child at 123
	 * 
	 * NO DATA LOSS: "5.4.3" stays as ["5", "4", "3"], not concatenated to "5.43"!
	 * 
	 * @return the hierarchical order string
	 */
	String order();
	
	/**
	 * Icon identifier (Vaadin icon name or class reference).
	 * 
	 * Examples:
	 * - "vaadin:tasks"
	 * - "class:tech.derbent.plm.activities.domain.CActivity"
	 * 
	 * @return the icon identifier
	 */
	String icon() default "";
	
	/**
	 * Route path for navigation.
	 * Optional - can be inferred from class name.
	 * 
	 * @return the route path
	 */
	String route() default "";
	
	/**
	 * Whether to show in quick access toolbar.
	 * 
	 * @return true if shown in quick toolbar
	 */
	boolean showInQuickToolbar() default false;
}
