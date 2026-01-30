package tech.derbent.bab.dashboard.view;

import com.google.gson.JsonObject;

/**
 * CNetworkInterface - Network interface model from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses.
 * Represents a single network interface with its configuration.
 * <p>
 * JSON structure from Calimero:
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
public class CNetworkInterface {

private String name;
private String type;
private String status;
private String macAddress;
private Integer mtu;
private Boolean dhcp4;
private Boolean dhcp6;

public CNetworkInterface() {
// Default constructor
}

/** Create interface from Calimero JSON response.
 * @param json JSON object from Calimero API
 * @return Network interface instance */
public static CNetworkInterface fromJson(final JsonObject json) {
final CNetworkInterface iface = new CNetworkInterface();

if (json.has("name")) {
iface.name = json.get("name").getAsString();
}
if (json.has("type")) {
iface.type = json.get("type").getAsString();
}
if (json.has("status")) {
iface.status = json.get("status").getAsString();
}
if (json.has("macAddress")) {
iface.macAddress = json.get("macAddress").getAsString();
}
if (json.has("mtu")) {
iface.mtu = json.get("mtu").getAsInt();
}
if (json.has("dhcp4")) {
iface.dhcp4 = json.get("dhcp4").getAsBoolean();
}
if (json.has("dhcp6")) {
iface.dhcp6 = json.get("dhcp6").getAsBoolean();
}

return iface;
}

public Boolean getDhcp4() {
return dhcp4;
}

public Boolean getDhcp6() {
return dhcp6;
}

public String getMacAddress() {
return macAddress;
}

public Integer getMtu() {
return mtu;
}

public String getName() {
return name;
}

public String getStatus() {
return status;
}

public String getType() {
return type;
}

public boolean isUp() {
return "up".equalsIgnoreCase(status);
}

public void setDhcp4(final Boolean dhcp4) {
this.dhcp4 = dhcp4;
}

public void setDhcp6(final Boolean dhcp6) {
this.dhcp6 = dhcp6;
}

public void setMacAddress(final String macAddress) {
this.macAddress = macAddress;
}

public void setMtu(final Integer mtu) {
this.mtu = mtu;
}

public void setName(final String name) {
this.name = name;
}

public void setStatus(final String status) {
this.status = status;
}

public void setType(final String type) {
this.type = type;
}

@Override
public String toString() {
return "CNetworkInterface{" + "name='" + name + '\'' + ", type='" + type + '\'' + ", status='" + status + '\'' + ", macAddress='"
+ macAddress + '\'' + ", mtu=" + mtu + ", dhcp4=" + dhcp4 + ", dhcp6=" + dhcp6 + '}';
}
}
