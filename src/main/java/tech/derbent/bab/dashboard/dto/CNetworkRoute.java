package tech.derbent.bab.dashboard.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CNetworkRoute - Network routing table entry from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses.
 * Represents a routing table entry.
 * <p>
 * JSON structure from Calimero:
 * <pre>
 * {
 *   "destination": "192.168.1.0/24",
 *   "gateway": "192.168.1.1",
 *   "interface": "eth0",
 *   "metric": 100,
 *   "flags": "UG"
 * }
 * </pre>
 */
public class CNetworkRoute extends CObject {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CNetworkRoute.class);
	
	public static CNetworkRoute createFromJson(final JsonObject json) {
		final CNetworkRoute route = new CNetworkRoute();
		route.fromJson(json);
		return route;
	}
	
	private String destination = "";
	private String gateway = "";
	private String interfaceName = "";
	private Integer metric = 0;
	private String flags = "";
	
	public CNetworkRoute() {
		// Default constructor
	}
	
	@Override
	protected void fromJson(final JsonObject json) {
		try {
			if (json.has("destination")) {
				destination = json.get("destination").getAsString();
			}
			if (json.has("gateway")) {
				gateway = json.get("gateway").getAsString();
			}
			if (json.has("interface")) {
				interfaceName = json.get("interface").getAsString();
			}
			if (json.has("metric")) {
				metric = json.get("metric").getAsInt();
			}
			if (json.has("flags")) {
				flags = json.get("flags").getAsString();
			}
		} catch (final Exception e) {
			LOGGER.error("Error parsing CNetworkRoute from JSON: {}", e.getMessage());
		}
	}
	
	public String getDestination() { return destination; }
	public String getGateway() { return gateway; }
	public String getInterfaceName() { return interfaceName; }
	public Integer getMetric() { return metric; }
	public String getFlags() { return flags; }
	
	/**
	 * Check if this is the default route.
	 * @return true if destination is "default" or "0.0.0.0/0"
	 */
	public boolean isDefaultRoute() {
		return "default".equalsIgnoreCase(destination) || 
		       "0.0.0.0/0".equals(destination) ||
		       "0.0.0.0".equals(destination);
	}
	
	/**
	 * Check if gateway is set (not direct route).
	 * @return true if gateway is not empty or "*"
	 */
	public boolean hasGateway() {
		return gateway != null && 
		       !gateway.isEmpty() && 
		       !"*".equals(gateway) &&
		       !"0.0.0.0".equals(gateway);
	}
	
	@Override
	protected String toJson() {
		// Not used for outbound requests - routes are read-only from server
		return "{}";
	}
}
