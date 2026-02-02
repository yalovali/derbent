package tech.derbent.bab.dashboard.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * CDnsConfigurationUpdate - DTO for DNS configuration update requests.
 * <p>
 * Encapsulates DNS server configuration changes to be applied via Calimero HTTP API.
 * Supports both manual DNS configuration and DHCP-based DNS configuration.
 * Uses persistent configuration via nmcli or systemd-resolved commands.
 * <p>
 * Structure:
 * <pre>
 * Manual mode:
 * {
 *   "nameservers": ["8.8.8.8", "8.8.4.4", "1.1.1.1"],
 *   "useDhcp": false
 * }
 * 
 * DHCP mode:
 * {
 *   "nameservers": [],
 *   "useDhcp": true
 * }
 * </pre>
 * <p>
 * Usage:
 * <pre>
 * // Manual DNS
 * List&lt;String&gt; dnsServers = Arrays.asList("8.8.8.8", "8.8.4.4");
 * CDnsConfigurationUpdate update = new CDnsConfigurationUpdate(dnsServers, false);
 * 
 * // DHCP DNS
 * CDnsConfigurationUpdate update = new CDnsConfigurationUpdate(new ArrayList&lt;&gt;(), true);
 * </pre>
 */
public class CDnsConfigurationUpdate {
	
	private final List<String> nameservers;
	private final boolean useDhcp;
	
	/**
	 * Constructor for DNS configuration update.
	 * @param nameservers List of DNS server IP addresses (first is primary), empty for DHCP
	 * @param useDhcp true to use DHCP DNS, false for manual configuration
	 */
	public CDnsConfigurationUpdate(final List<String> nameservers, final boolean useDhcp) {
		this.nameservers = nameservers != null ? new ArrayList<>(nameservers) : new ArrayList<>();
		this.useDhcp = useDhcp;
	}
	
	/**
	 * Get DNS server list.
	 * @return Immutable copy of nameservers list
	 */
	public List<String> getNameservers() {
		return new ArrayList<>(nameservers);
	}
	
	/**
	 * Check if DHCP DNS is enabled.
	 * @return true if using DHCP for DNS configuration
	 */
	public boolean isUseDhcp() {
		return useDhcp;
	}
	
	/**
	 * Get primary DNS server (first in list).
	 * @return Primary DNS server IP or null if empty
	 */
	public String getPrimaryDns() {
		return nameservers.isEmpty() ? null : nameservers.get(0);
	}
	
	/**
	 * Get secondary DNS servers (all except first).
	 * @return List of secondary DNS servers
	 */
	public List<String> getSecondaryDns() {
		return nameservers.size() <= 1 ? 
			new ArrayList<>() : 
			new ArrayList<>(nameservers.subList(1, nameservers.size()));
	}
	
	/**
	 * Check if configuration is valid.
	 * @return true if DHCP mode OR at least one manual nameserver configured
	 */
	public boolean isValid() {
		return useDhcp || !nameservers.isEmpty();
	}
	
	/**
	 * Get count of DNS servers.
	 * @return Number of DNS servers
	 */
	public int getServerCount() {
		return nameservers.size();
	}
	
	@Override
	public String toString() {
		return "CDnsConfigurationUpdate{" +
			"nameservers=" + nameservers +
			", useDhcp=" + useDhcp +
			", primary=" + getPrimaryDns() +
			", count=" + getServerCount() +
			'}';
	}
}
