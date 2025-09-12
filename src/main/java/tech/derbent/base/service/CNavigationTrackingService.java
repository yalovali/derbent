package tech.derbent.base.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinServletService;
import tech.derbent.setup.service.CSystemSettingsService;

/** Service to track navigation and update the last visited page in system settings. This service listens to navigation events and stores the current
 * route as the last visited page. */
@Service
public class CNavigationTrackingService implements VaadinServiceInitListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CNavigationTrackingService.class);
	@Autowired
	private CSystemSettingsService systemSettingsService;

	@Override
	public void serviceInit(com.vaadin.flow.server.ServiceInitEvent event) {
		event.getSource().addUIInitListener(uiEvent -> {
			uiEvent.getUI().addBeforeEnterListener(this::onBeforeEnter);
		});
	}

	/** Called before entering any view to track the navigation */
	private void onBeforeEnter(BeforeEnterEvent event) {
		try {
			String route = event.getLocation().getPath();
			// Skip tracking for certain routes that shouldn't be "last visited"
			if (shouldSkipRoute(route)) {
				return;
			}
			// Clean the route (remove leading slash if present)
			if (route.startsWith("/")) {
				route = route.substring(1);
			}
			// Default to "home" if route is empty
			if (route.trim().isEmpty()) {
				route = "home";
			}
			// Update the last visited view in system settings
			updateLastVisitedView(route);
		} catch (Exception e) {
			LOGGER.warn("Error tracking navigation: {}", e.getMessage());
		}
	}

	/** Determines if a route should be skipped from last visited tracking */
	private boolean shouldSkipRoute(String route) {
		if (route == null) {
			return true;
		}
		// Skip login and error pages
		return route.equals("/login") || route.equals("/logout") || route.startsWith("/error") || route.startsWith("/VAADIN")
				|| route.contains("?logout") || route.contains("?error");
	}

	/** Updates the last visited view setting */
	private void updateLastVisitedView(String route) {
		try {
			if (systemSettingsService != null) {
				systemSettingsService.updateLastVisitedView(route);
				LOGGER.debug("Updated last visited view to: {}", route);
			}
		} catch (Exception e) {
			LOGGER.warn("Error updating last visited view to {}: {}", route, e.getMessage());
		}
	}
}
