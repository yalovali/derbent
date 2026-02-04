package tech.derbent.bab.dashboard.dashboardproject_bab.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * CDTORouteConfigurationUpdate - DTO for route configuration updates.
 * <p>
 * Contains default gateway and list of static routes to be applied.
 */
public class CDTORouteConfigurationUpdate implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String defaultGateway;
	private List<CDTORouteEntry> staticRoutes;
	
	public CDTORouteConfigurationUpdate() {
		this.staticRoutes = new ArrayList<>();
	}
	
	public CDTORouteConfigurationUpdate(final String defaultGateway, final List<CDTORouteEntry> staticRoutes) {
		this.defaultGateway = defaultGateway;
		this.staticRoutes = staticRoutes != null ? staticRoutes : new ArrayList<>();
	}
	
	public String getDefaultGateway() {
		return defaultGateway;
	}
	
	public void setDefaultGateway(final String defaultGateway) {
		this.defaultGateway = defaultGateway;
	}
	
	public List<CDTORouteEntry> getStaticRoutes() {
		return staticRoutes;
	}
	
	public void setStaticRoutes(final List<CDTORouteEntry> staticRoutes) {
		this.staticRoutes = staticRoutes;
	}
	
	/**
	 * Validate this configuration update.
	 * @return true if configuration is valid
	 */
	public boolean isValid() {
		// Default gateway can be optional
		// All static routes must be valid if present
		if (staticRoutes != null) {
			for (final CDTORouteEntry route : staticRoutes) {
				if (!route.isValid()) {
					return false;
				}
			}
		}
		return true;
	}
	
	public int getRouteCount() {
		return staticRoutes != null ? staticRoutes.size() : 0;
	}
	
	@Override
	public String toString() {
		return String.format("RouteConfig[defaultGateway=%s, routes=%d]",
				defaultGateway, getRouteCount());
	}
}
