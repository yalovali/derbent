package tech.derbent.abstracts.annotations;

import com.vaadin.flow.router.Route;

public class CSpringAuxillaries {

	/**
	 * Retrieves the @Route path value of a given class, if present.
	 * @param clazz The view class to inspect.
	 * @return The route path, or null if not annotated.
	 */
	public static String getRoutePath(final Class<?> clazz) {
		// Check if the @Route annotation is present on the class
		final Route routeAnnotation = clazz.getAnnotation(Route.class);
		if (routeAnnotation != null) {
			// Return the path value from the annotation
			return routeAnnotation.value();
		}
		return null; // No annotation found
	}
}