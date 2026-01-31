package tech.derbent.bab.dashboard.view;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/** CNetworkInterface - Network interface model from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses. Represents a single network interface with its
 * configuration.
 * <p>
 * JSON structure from Calimero:
 *
 * <pre>
 * {
 *   "name": "eth0",
 *   "type": "ethernet",
 *   "status": "up",
 *   "macAddress": "00:11:22:33:44:55",
 *   "mtu": 1500,
 *   "dhcp4": false,
 *   "dhcp6": false
 * }
 * </pre>
 */
public class CNetworkInterface extends CObject {
	public static CNetworkInterface createFromJson(final JsonObject json) {
		final CNetworkInterface iface = new CNetworkInterface();
		iface.fromJson(json);
		return iface;
	}

	private String name;
	private String type;
	private String status;
	private String macAddress;
	private Integer mtu;
	private Boolean dhcp4;
	private Boolean dhcp6;
	private final List<String> addresses = new ArrayList<>();
	private CNetworkInterfaceIpConfiguration ipConfiguration;

	public CNetworkInterface() {
		// Default constructor
	}

	@Override
	protected void fromJson(final JsonObject json) {
		if (json.has("name")) {
			name = json.get("name").getAsString();
		}
		if (json.has("type")) {
			type = json.get("type").getAsString();
		}
		if (json.has("status")) {
			status = json.get("status").getAsString();
		}
		if (json.has("macAddress")) {
			macAddress = json.get("macAddress").getAsString();
		}
		if (json.has("mtu")) {
			mtu = json.get("mtu").getAsInt();
		}
		if (json.has("dhcp4")) {
			dhcp4 = json.get("dhcp4").getAsBoolean();
		}
		if (json.has("dhcp6")) {
			dhcp6 = json.get("dhcp6").getAsBoolean();
		}
		if (json.has("addresses") && json.get("addresses").isJsonArray()) {
			final JsonArray pairs = json.getAsJsonArray("addresses");
			addresses.clear();
			pairs.forEach(element -> addresses.add(element.getAsString()));
		}
	}

	public Boolean getDhcp4() { return dhcp4; }

	public Boolean getDhcp6() { return dhcp6; }

	public String getMacAddress() { return macAddress; }

	public Integer getMtu() { return mtu; }

	public String getName() { return name; }

	public String getStatus() { return status; }

	public String getType() { return type; }

	public boolean isUp() { return "up".equalsIgnoreCase(status); }

	public void setDhcp4(final Boolean dhcp4) { this.dhcp4 = dhcp4; }

	public void setDhcp6(final Boolean dhcp6) { this.dhcp6 = dhcp6; }

	public void setMacAddress(final String macAddress) { this.macAddress = macAddress; }

	public void setMtu(final Integer mtu) { this.mtu = mtu; }

	public void setName(final String name) { this.name = name; }

	public void setStatus(final String status) { this.status = status; }

	public void setType(final String type) { this.type = type; }

	public List<String> getAddresses() { return addresses; }

	public CNetworkInterfaceIpConfiguration getIpConfiguration() { return ipConfiguration; }

	public void setIpConfiguration(final CNetworkInterfaceIpConfiguration ipConfiguration) { this.ipConfiguration = ipConfiguration; }

	public String getIpv4Display() {
		if (ipConfiguration != null) {
			return ipConfiguration.getIpv4LabelOrDash();
		}
		return addresses.isEmpty() ? "-" : addresses.get(0);
	}

	public String getIpv4GatewayDisplay() {
		if (ipConfiguration != null) {
			return ipConfiguration.getIpv4GatewayOrDash();
		}
		return "-";
	}

	@Override
	protected String toJson() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return "CNetworkInterface{" + "name='" + name + '\'' + ", type='" + type + '\'' + ", status='" + status + '\'' + ", macAddress='" + macAddress
				+ '\'' + ", mtu=" + mtu + ", dhcp4=" + dhcp4 + ", dhcp6=" + dhcp6 + ", ipv4=" + getIpv4Display() + '}';
	}
}
