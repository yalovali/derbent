package tech.derbent.api.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.derbent.api.utils.Check;

@Service
public class CPageTestAuxillaryService {
	public class RouteEntry {
		public String iconColor;
		final public String iconName;
		final public String route;
		final public String title;

		public RouteEntry(final String title, final String iconName, final String iconColor, final String route) {
			Check.notNull(title, "title cannot be null");
			Check.notNull(iconName, "iconName cannot be null");
			Check.notNull(route, "route cannot be null");
			Check.notNull(iconColor, "iconColor cannot be null");
			this.route = route;
			this.title = title;
			this.iconName = iconName;
			this.iconColor = iconColor;
		}
	}

	Logger LOGGER = LoggerFactory.getLogger(CPageTestAuxillaryService.class);
	final List<RouteEntry> routes = new ArrayList<RouteEntry>();

	public CPageTestAuxillaryService() {
		LOGGER.debug("CPageTestAuxillaryService initialized");
		// Add static test pages that don't appear in the main menu
		addStaticTestRoutes();
	}

	/** Add static test pages that are available but not in the main menu. These pages are specifically created for testing purposes. 
	 * This method is public so it can be called after clearing routes to re-add static test pages. */
	public void addStaticTestRoutes() {
		// Add User Icon Test page
		addRoute("User Icon Test", "vaadin:user", "#6B5FA7", "user-icon-test");
		LOGGER.debug("Added static test routes");
	}

	/** Add a route entry to the service. This method is idempotent — it will not add duplicate entries (same title + route) if called multiple times.
	 * It also normalizes "dynamic." prefixes into the internal dynamic route format.
	 * @param iconColor */
	public synchronized void addRoute(final String title, final String iconName, final String iconColor, final String route) {
		final String resolvedRoute;
		if (route.startsWith("dynamic.")) {
			// Remove "dynamic." prefix and navigate
			final String dynamicPath = route.substring("dynamic.".length());
			// give rest of path as a parameter to dynamicview page
			resolvedRoute = "cdynamicpagerouter/page:" + dynamicPath;
		} else {
			resolvedRoute = route;
		}
		routes.add(new RouteEntry(title, iconName, iconColor, resolvedRoute));
	}

	public synchronized void clearRoutes() {
		routes.clear();
	}

	/** Return an unmodifiable view of the route entries to avoid external modification and make callers treat the list as read-only. */
	public List<RouteEntry> getRoutes() {
		return Collections.unmodifiableList(routes);
	}
}
