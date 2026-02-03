package tech.derbent.bab.dashboard.dto;

import java.util.Objects;
import tech.derbent.api.utils.Check;

/** DTO for updating interface IP configuration via Calimero API.
 * <p>
 * Follows Calimero Network API specification:
 * - DHCP mode: Only interface name required
 * - Static mode: interface, ip, netmask (converted from prefix), gateway (optional)
 */
public class CDTONetworkInterfaceIpUpdate {

	private final String interfaceName;
	private final String ipv4Address;
	private final Integer prefixLength;
	private final String gateway;
	private final boolean useDhcp;

	public CDTONetworkInterfaceIpUpdate(final String interfaceName, final String ipv4Address, final Integer prefixLength, 
			final String gateway, final boolean useDhcp) {
		Check.notBlank(interfaceName, "Interface name is required");
		if (!useDhcp) {
			Check.notBlank(ipv4Address, "IPv4 address is required when not using DHCP");
			Check.notNull(prefixLength, "Prefix length is required when not using DHCP");
		}
		this.interfaceName = interfaceName;
		this.ipv4Address = ipv4Address;
		this.prefixLength = prefixLength;
		this.gateway = gateway;
		this.useDhcp = useDhcp;
	}

	public String getInterfaceName() { return interfaceName; }

	public String getIpv4Address() { return ipv4Address; }

	public Integer getPrefixLength() { return prefixLength; }

	public String getGateway() { return gateway; }

	public boolean isUseDhcp() { return useDhcp; }

	@Override
	public String toString() {
		return "CDTONetworkInterfaceIpUpdate{" + "interfaceName='" + interfaceName + '\'' + ", ipv4Address='" + ipv4Address + '\'' + ", prefixLength="
				+ prefixLength + ", gateway='" + gateway + '\'' + ", useDhcp=" + useDhcp + '}';
	}

	@Override
	public int hashCode() { return Objects.hash(interfaceName, ipv4Address, prefixLength, gateway, useDhcp); }

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) { return true; }
		if ((obj == null) || (getClass() != obj.getClass())) { return false; }
		final CDTONetworkInterfaceIpUpdate other = (CDTONetworkInterfaceIpUpdate) obj;
		return useDhcp == other.useDhcp && Objects.equals(interfaceName, other.interfaceName)
				&& Objects.equals(ipv4Address, other.ipv4Address) && Objects.equals(prefixLength, other.prefixLength)
				&& Objects.equals(gateway, other.gateway);
	}
}

