package tech.derbent.api.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.Route;

/**
 * Service that scans for @MyMenu annotations and builds menu entries.
 * 
 * Similar to Vaadin's MenuConfiguration but for custom @MyMenu annotation.
 * 
 * Usage:
 * <pre>
 * {@literal @}Autowired
 * private MyMenuConfiguration myMenuConfig;
 * 
 * // Get all entries
 * List&lt;MyMenuEntry&gt; entries = myMenuConfig.getMyMenuEntries();
 * </pre>
 */
@Service
public final class MyMenuConfiguration {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MyMenuConfiguration.class);
	
	private final List<MyMenuEntry> menuEntries = new ArrayList<>();
	private boolean scanned = false;
	
	/**
	 * Scan classpath for @MyMenu annotated classes.
	 * 
	 * Call this at application startup to build menu structure.
	 * This method is idempotent - multiple calls will only scan once.
	 */
	public void scanMyMenuAnnotations() {
		if (scanned) {
			LOGGER.debug("@MyMenu annotations already scanned, skipping");
			return;
		}
		
		LOGGER.info("Scanning for @MyMenu annotations...");
		
		try {
			// Scan all tech.derbent packages for @MyMenu annotations
			final Reflections reflections = new Reflections("tech.derbent");
			final Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(MyMenu.class);
			
			for (final Class<?> clazz : annotatedClasses) {
				try {
					processMyMenuClass(clazz);
				} catch (final Exception e) {
					LOGGER.error("Error processing @MyMenu on class {}: {}", clazz.getName(), e.getMessage(), e);
				}
			}
			
			scanned = true;
			LOGGER.info("Found {} @MyMenu entries", menuEntries.size());
			
		} catch (final Exception e) {
			LOGGER.error("Error scanning for @MyMenu annotations: {}", e.getMessage(), e);
		}
	}
	
	/**
	 * Process a single class annotated with @MyMenu.
	 * 
	 * @param clazz the annotated class
	 */
	private void processMyMenuClass(final Class<?> clazz) throws Exception {
		final MyMenu annotation = clazz.getAnnotation(MyMenu.class);
		
		// Validate that class extends Component (Vaadin view)
		if (!Component.class.isAssignableFrom(clazz)) {
			LOGGER.warn("@MyMenu found on non-Component class: {}", clazz.getName());
			return;
		}
		
		@SuppressWarnings("unchecked")
		final Class<? extends Component> componentClass = (Class<? extends Component>) clazz;
		
		// Determine route path
		String route = annotation.route();
		if (route.isEmpty()) {
			// Try to get from @Route annotation
			final Route routeAnnotation = clazz.getAnnotation(Route.class);
			if (routeAnnotation != null) {
				route = routeAnnotation.value();
			} else {
				// Infer from class name: CActivityView → "activities"
				route = inferRouteFromClassName(clazz.getSimpleName());
			}
		}
		
		// Create MyMenuEntry
		final MyMenuEntry entry = new MyMenuEntry(route, annotation.title(), annotation.order(), annotation.icon(), componentClass,
				annotation.showInQuickToolbar());
		
		menuEntries.add(entry);
		LOGGER.debug("Registered @MyMenu: {}", entry);
	}
	
	/**
	 * Get all registered menu entries.
	 * 
	 * @return list of menu entries (empty if not yet scanned)
	 */
	public List<MyMenuEntry> getMyMenuEntries() {
		if (!scanned) {
			LOGGER.warn("@MyMenu annotations not yet scanned. Call scanMyMenuAnnotations() first.");
		}
		return new ArrayList<>(menuEntries);
	}
	
	/**
	 * Infer route from class name.
	 * 
	 * Examples:
	 * - CActivityView → "activities"
	 * - CMeetingPage → "meetings"
	 * - CActivityTypeView → "activitytypes"
	 * 
	 * @param className the simple class name
	 * @return the inferred route
	 */
	private String inferRouteFromClassName(final String className) {
		// Remove C prefix and View/Page suffix
		final String name = className.replaceFirst("^C", "").replaceFirst("(View|Page)$", "");
		// Convert to lowercase
		return name.toLowerCase();
	}
}
