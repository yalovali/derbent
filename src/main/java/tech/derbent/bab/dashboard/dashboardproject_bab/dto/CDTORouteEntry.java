package tech.derbent.bab.dashboard.dashboardproject_bab.dto;

import java.io.Serializable;

/**
 * CDTORouteEntry - DTO for a single network route entry.
 * <p>
 * Represents a static route with network/netmask and gateway.
 * Only manual (non-protocol) routes are editable.
 */
public class CDTORouteEntry implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String network;      // e.g., "192.168.1.0/24" or "192.168.1.0"
	private String netmask;      // e.g., "255.255.255.0" or "24"
	private String gateway;      // e.g., "192.168.1.1"
	private boolean isManual;    // true if user-created (editable)
	
	public CDTORouteEntry() {
		this.isManual = true;  // New routes are always manual
	}
	
	public CDTORouteEntry(final String network, final String netmask, final String gateway) {
		this.network = network;
		this.netmask = netmask;
		this.gateway = gateway;
		this.isManual = true;
	}
	
	public CDTORouteEntry(final String network, final String netmask, final String gateway, final boolean isManual) {
		this.network = network;
		this.netmask = netmask;
		this.gateway = gateway;
		this.isManual = isManual;
	}
	
	public String getNetwork() {
		return network;
	}
	
	public void setNetwork(final String network) {
		this.network = network;
	}
	
	public String getNetmask() {
		return netmask;
	}
	
	public void setNetmask(final String netmask) {
		this.netmask = netmask;
	}
	
	public String getGateway() {
		return gateway;
	}
	
	public void setGateway(final String gateway) {
		this.gateway = gateway;
	}
	
	public boolean isManual() {
		return isManual;
	}
	
	public void setManual(final boolean manual) {
		isManual = manual;
	}
	
	/**
	 * Validate this route entry.
	 * @return true if all fields are valid
	 */
	public boolean isValid() {
		return network != null && !network.trim().isEmpty()
				&& netmask != null && !netmask.trim().isEmpty()
				&& gateway != null && !gateway.trim().isEmpty();
	}
	
	@Override
	public String toString() {
		return String.format("Route[network=%s, netmask=%s, gateway=%s, manual=%s]",
				network, netmask, gateway, isManual);
	}
}
