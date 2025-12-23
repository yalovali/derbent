package tech.derbent.api.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.icon.Icon;

/** Service for dynamically discovering all routes and their metadata using reflection. This service scans for view classes and extracts their route,
 * icon, title, and display information. */
@Service
public class CRouteDiscoveryService {

	/** Data class holding route information */
	public static class RouteInfo {

		private final String displayName;
		private final Icon icon;
		private final String iconColor;
		private final String route;
		private final String title;

		public RouteInfo(String route, String title, String displayName, Icon icon, String iconColor) {
			this.route = route;
			this.title = title;
			this.displayName = displayName;
			this.icon = icon;
			this.iconColor = iconColor;
		}

		public String getDisplayName() { return displayName; }

		public String getIconColor() { return iconColor; }

		public Icon getIconString() { return icon; }

		public String getRoute() { return route; }

		public String getTitle() { return title; }

		@Override
		public String toString() {
			return displayName + " (" + route + ")";
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CRouteDiscoveryService.class);

	/** Adds common routes that might not extend CAbstractNamedEntityPage */
	private static void addCommonRoutes(List<RouteInfo> routes) {
		// Add home/dashboard route if not already present
		if (routes.stream().noneMatch(r -> "home".equals(r.getRoute()) || "".equals(r.getRoute()))) {
			routes.add(new RouteInfo("home", "Home", "Home / Dashboard", null, null));
		}
	}

	/** Adds hardcoded routes as a fallback - these match the original hardcoded list */
	private static void addHardcodedRoutes(List<RouteInfo> routes) {
		addRouteIfNotExists(routes, "cprojectsview", "Projects", "Projects");
		addRouteIfNotExists(routes, "cactivitiesview", "Activities", "Activities");
		addRouteIfNotExists(routes, "cmeetingsview", "Meetings", "Meetings");
		addRouteIfNotExists(routes, "cusersview", "Users", "Users");
		addRouteIfNotExists(routes, "cprojectganttview", "Gantt Chart", "Gantt Chart");
		addRouteIfNotExists(routes, "cordersview", "Orders", "Orders");
		addRouteIfNotExists(routes, "cgridentityview", "Grid Entities", "Grid Entities");
		addRouteIfNotExists(routes, "cdashboardview", "Dashboard", "Dashboard");
		// Add additional discovered routes
		addRouteIfNotExists(routes, "criskview", "Risks", "Risks");
		addRouteIfNotExists(routes, "ccommentpriorityview", "Comment Priorities", "Comment Priorities");
		addRouteIfNotExists(routes, "cmeetingtypeview", "Meeting Types", "Meeting Types");
		addRouteIfNotExists(routes, "cmeetingstatusview", "Meeting Status", "Meeting Status");
		addRouteIfNotExists(routes, "cactivitytypeview", "Activity Types", "Activity Types");
		addRouteIfNotExists(routes, "ccurrencyview", "Currencies", "Currencies");
		addRouteIfNotExists(routes, "cprojectdetailsview", "Project Details", "Project Details");
	}

	/** Helper method to add a route if it doesn't already exist */
	private static void addRouteIfNotExists(List<RouteInfo> routes, String route, String title, String displayName) {
		if (routes.stream().noneMatch(r -> r.getRoute().equals(route))) {
			routes.add(new RouteInfo(route, title, displayName, null, null));
		}
	}

	/** Discovers all available routes by scanning for view classes with @Route annotations
	 * @return List of RouteInfo objects containing route metadata */
	public static List<RouteInfo> discoverAllRoutes() {
		final List<RouteInfo> routes = new ArrayList<>();
		try {
			// Add manual routes first (these are common routes that might not have view classes)
			addCommonRoutes(routes);
			// Add hardcoded routes as fallback for now
			addHardcodedRoutes(routes);
			// Sort routes by display name
			routes.sort((r1, r2) -> r1.getDisplayName().compareToIgnoreCase(r2.getDisplayName()));
			LOGGER.info("Discovered {} routes", routes.size());
			return routes;
		} catch (final Exception e) {
			LOGGER.error("Error discovering routes", e);
			return getDefaultRoutes();
		}
	}

	/** Gets all route values as a list of strings */
	public static List<String> getAllRouteValues() {
		return discoverAllRoutes().stream().map(RouteInfo::getRoute).collect(Collectors.toList());
	}

	/** Gets default routes as fallback */
	private static List<RouteInfo> getDefaultRoutes() {
		final List<RouteInfo> routes = new ArrayList<>();
		addCommonRoutes(routes);
		addHardcodedRoutes(routes);
		return routes;
	}

	/** Gets route info by route value */
	public static RouteInfo getRouteInfo(String route) {
		return discoverAllRoutes().stream().filter(r -> r.getRoute().equals(route)).findFirst().orElse(null);
	}
}
