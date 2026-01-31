package tech.derbent.bab.dashboard.view;

import java.util.Objects;
import tech.derbent.api.utils.Check;

/** DTO used when editing an interface IP configuration. */
public class CNetworkInterfaceIpUpdate {

	private final String interfaceName;
	private final String ipv4Address;
	private final Integer prefixLength;
	private final String gateway;
	private final boolean readOnly;

	public CNetworkInterfaceIpUpdate(final String interfaceName, final String ipv4Address, final Integer prefixLength, final String gateway,
			final boolean readOnly) {
		Check.notBlank(interfaceName, "Interface name is required");
		Check.notBlank(ipv4Address, "IPv4 address is required");
		this.interfaceName = interfaceName;
		this.ipv4Address = ipv4Address;
		this.prefixLength = prefixLength;
		this.gateway = gateway;
		this.readOnly = readOnly;
	}

	public String getInterfaceName() { return interfaceName; }

	public String getIpv4Address() { return ipv4Address; }

	public Integer getPrefixLength() { return prefixLength; }

	public String getGateway() { return gateway; }

	public boolean isReadOnly() { return readOnly; }

	public String toAddressArgument() {
		if (prefixLength == null) {
			return ipv4Address;
		}
		return ipv4Address + "/" + prefixLength;
	}

	@Override
	public String toString() {
		return "CNetworkInterfaceIpUpdate{" + "interfaceName='" + interfaceName + '\'' + ", ipv4Address='" + ipv4Address + '\'' + ", prefixLength="
				+ prefixLength + ", gateway='" + gateway + '\'' + ", readOnly=" + readOnly + '}';
	}

	@Override
	public int hashCode() { return Objects.hash(interfaceName, ipv4Address, prefixLength, gateway, readOnly); }

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) { return true; }
		if ((obj == null) || (getClass() != obj.getClass())) { return false; }
		final CNetworkInterfaceIpUpdate other = (CNetworkInterfaceIpUpdate) obj;
		return readOnly == other.readOnly && Objects.equals(interfaceName, other.interfaceName) && Objects.equals(ipv4Address, other.ipv4Address)
				&& Objects.equals(prefixLength, other.prefixLength) && Objects.equals(gateway, other.gateway);
	}
}
