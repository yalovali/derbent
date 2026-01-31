package tech.derbent.bab.dashboard.view;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import tech.derbent.api.ui.notifications.CNotificationService;
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
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CNetworkInterface.class);

	public static CNetworkInterface createFromJson(final JsonObject json) {
		final CNetworkInterface iface = new CNetworkInterface();
		iface.fromJson(json);
		return iface;
	}

	private String name = "";
	private String type = "";
	private String status = "";
	private String macAddress = "";
	private Integer mtu = 0;
	private Boolean dhcp4 = false;
	private Boolean dhcp6 = false;
	private final List<String> addresses = new ArrayList<>();
	private CNetworkInterfaceIpConfiguration ipConfiguration;

	public CNetworkInterface() {
		// Default constructor
	}

	@Override
	protected void fromJson(final JsonObject json) {
		try {
			name = json.get("name").getAsString();
			type = json.get("type").getAsString();
			status = json.get("status").getAsString();
			macAddress = json.get("macAddress").getAsString();
			mtu = json.get("mtu").getAsInt();
			dhcp4 = json.get("dhcp4").getAsBoolean();
			dhcp6 = json.get("dhcp6").getAsBoolean();
			if (json.has("addresses") && json.get("addresses").isJsonArray()) {
				final JsonArray pairs = json.getAsJsonArray("addresses");
				addresses.clear();
				pairs.forEach(element -> addresses.add(element.getAsString()));
			}
		} catch (final Exception e) {
			LOGGER.error("Error parsing CNetworkInterface from JSON {}", e.getMessage());
			CNotificationService.showException("Error parsing network interface data", e);
		}
	}

	public List<String> getAddresses() { return addresses; }

	public Boolean getDhcp4() { return dhcp4; }

	public Boolean getDhcp6() { return dhcp6; }

	public CNetworkInterfaceIpConfiguration getIpConfiguration() { return ipConfiguration; }

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

	public String getMacAddress() { return macAddress; }

	public Integer getMtu() { return mtu; }

	public String getName() { return name; }

	public String getStatus() { return status; }

	public String getType() { return type; }

	public boolean isUp() { return "up".equalsIgnoreCase(status); }

	public void setDhcp4(final Boolean dhcp4) { this.dhcp4 = dhcp4; }

	public void setDhcp6(final Boolean dhcp6) { this.dhcp6 = dhcp6; }

	public void setIpConfiguration(final CNetworkInterfaceIpConfiguration ipConfiguration) { this.ipConfiguration = ipConfiguration; }

	public void setMacAddress(final String macAddress) { this.macAddress = macAddress; }

	public void setMtu(final Integer mtu) { this.mtu = mtu; }

	public void setName(final String name) { this.name = name; }

	public void setStatus(final String status) { this.status = status; }

	public void setType(final String type) { this.type = type; }

	@Override
	protected String toJson() {
		final JsonObject json = new JsonObject();
		json.addProperty("name", name);
		json.addProperty("type", type);
		json.addProperty("status", status);
		json.addProperty("macAddress", macAddress);
		json.addProperty("mtu", mtu);
		json.addProperty("dhcp4", dhcp4);
		json.addProperty("dhcp6", dhcp6);
		// Serialize addresses array
		final JsonArray addressArray = new JsonArray();
		addresses.forEach(addressArray::add);
		json.add("addresses", addressArray);
		// Include IP configuration if present
		if (ipConfiguration != null) {
			json.addProperty("ipConfiguration", ipConfiguration.toJson());
		}
		return json.toString();
	}

	@Override
	public String toString() {
		return "CNetworkInterface{" + "name='" + name + '\'' + ", type='" + type + '\'' + ", status='" + status + '\'' + ", macAddress='" + macAddress
				+ '\'' + ", mtu=" + mtu + ", dhcp4=" + dhcp4 + ", dhcp6=" + dhcp6 + ", ipv4=" + getIpv4Display() + '}';
	}
}
