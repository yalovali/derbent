package tech.derbent.base.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import tech.derbent.abstracts.utils.CColorUtils;
import tech.derbent.abstracts.views.CAbstractNamedEntityPage;

/**
 * Service for dynamically discovering all routes and their metadata using reflection.
 * This service scans for view classes and extracts their route, icon, title, and display information.
 */
@Service
public class CRouteDiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CRouteDiscoveryService.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * Data class holding route information
     */
    public static class RouteInfo {
        private final String route;
        private final String title;
        private final String displayName;
        private final Class<? extends CAbstractNamedEntityPage<?>> viewClass;
        private final Icon icon;
        private final String iconColor;
        
        public RouteInfo(String route, String title, String displayName, 
                        Class<? extends CAbstractNamedEntityPage<?>> viewClass, 
                        Icon icon, String iconColor) {
            this.route = route;
            this.title = title;
            this.displayName = displayName;
            this.viewClass = viewClass;
            this.icon = icon;
            this.iconColor = iconColor;
        }
        
        public String getRoute() { return route; }
        public String getTitle() { return title; }
        public String getDisplayName() { return displayName; }
        public Class<? extends CAbstractNamedEntityPage<?>> getViewClass() { return viewClass; }
        public Icon getIcon() { return icon; }
        public String getIconColor() { return iconColor; }
        
        @Override
        public String toString() {
            return displayName + " (" + route + ")";
        }
    }
    
    /**
     * Discovers all available routes by scanning for view classes with @Route annotations
     * @return List of RouteInfo objects containing route metadata
     */
    @SuppressWarnings("unchecked")
    public List<RouteInfo> discoverAllRoutes() {
        List<RouteInfo> routes = new ArrayList<>();
        
        try {
            // Get all beans that extend CAbstractNamedEntityPage
            String[] beanNames = applicationContext.getBeanNamesForType(CAbstractNamedEntityPage.class);
            
            for (String beanName : beanNames) {
                try {
                    Object bean = applicationContext.getBean(beanName);
                    Class<?> clazz = bean.getClass();
                    
                    // Check if it's a CAbstractNamedEntityPage
                    if (CAbstractNamedEntityPage.class.isAssignableFrom(clazz)) {
                        Class<? extends CAbstractNamedEntityPage<?>> viewClass = 
                            (Class<? extends CAbstractNamedEntityPage<?>>) clazz;
                        
                        RouteInfo routeInfo = extractRouteInfo(viewClass);
                        if (routeInfo != null) {
                            routes.add(routeInfo);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error processing bean {}: {}", beanName, e.getMessage());
                }
            }
            
            // Add common routes that might not be CAbstractNamedEntityPage
            addCommonRoutes(routes);
            
            // Sort routes by display name
            routes.sort((r1, r2) -> r1.getDisplayName().compareToIgnoreCase(r2.getDisplayName()));
            
            LOGGER.info("Discovered {} routes", routes.size());
            return routes;
            
        } catch (Exception e) {
            LOGGER.error("Error discovering routes", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Extracts route information from a view class using reflection
     */
    private RouteInfo extractRouteInfo(Class<? extends CAbstractNamedEntityPage<?>> viewClass) {
        try {
            // Get route from @Route annotation
            Route routeAnnotation = viewClass.getAnnotation(Route.class);
            if (routeAnnotation == null) {
                LOGGER.debug("No @Route annotation found for {}", viewClass.getSimpleName());
                return null;
            }
            
            String route = routeAnnotation.value();
            if (route == null || route.trim().isEmpty()) {
                LOGGER.debug("Empty route value for {}", viewClass.getSimpleName());
                return null;
            }
            
            // Get title from @PageTitle annotation
            String title = "Unknown";
            PageTitle pageTitleAnnotation = viewClass.getAnnotation(PageTitle.class);
            if (pageTitleAnnotation != null) {
                title = pageTitleAnnotation.value();
            }
            
            // Generate display name from title or class name
            String displayName = generateDisplayName(title, viewClass.getSimpleName());
            
            // Get icon and color using existing CColorUtils
            Icon icon = null;
            String iconColor = null;
            try {
                icon = CColorUtils.getIconForViewClass(viewClass);
                iconColor = CColorUtils.getIconColorCode(viewClass);
            } catch (Exception e) {
                LOGGER.debug("Could not get icon/color for {}: {}", viewClass.getSimpleName(), e.getMessage());
            }
            
            return new RouteInfo(route, title, displayName, viewClass, icon, iconColor);
            
        } catch (Exception e) {
            LOGGER.warn("Error extracting route info from {}: {}", viewClass.getSimpleName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Generates a user-friendly display name from title or class name
     */
    private String generateDisplayName(String title, String className) {
        if (title != null && !title.trim().isEmpty() && !"Unknown".equals(title)) {
            return title;
        }
        
        // Convert class name to display name
        // CProjectsView -> Projects
        // CActivitiesView -> Activities
        String name = className;
        if (name.startsWith("C")) {
            name = name.substring(1);
        }
        if (name.endsWith("View")) {
            name = name.substring(0, name.length() - 4);
        }
        
        // Add spaces before capital letters
        return name.replaceAll("([A-Z])", " $1").trim();
    }
    
    /**
     * Adds common routes that might not extend CAbstractNamedEntityPage
     */
    private void addCommonRoutes(List<RouteInfo> routes) {
        // Add home/dashboard route if not already present
        if (routes.stream().noneMatch(r -> "home".equals(r.getRoute()) || "".equals(r.getRoute()))) {
            routes.add(new RouteInfo("home", "Home", "Home / Dashboard", null, null, null));
        }
    }
    
    /**
     * Gets all route values as a list of strings
     */
    public List<String> getAllRouteValues() {
        return discoverAllRoutes().stream()
                .map(RouteInfo::getRoute)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets route info by route value
     */
    public RouteInfo getRouteInfo(String route) {
        return discoverAllRoutes().stream()
                .filter(r -> r.getRoute().equals(route))
                .findFirst()
                .orElse(null);
    }
}