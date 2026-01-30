package tech.derbent.bab.dashboard.view;

import java.io.Serializable;

/** NetworkInterface - DTO for Calimero network interface data.
 * <p>
 * This is NOT a JPA entity - it's a simple data transfer object for displaying network interface information from Calimero HTTP API.
 */
public class NetworkInterface implements Serializable {

private static final long serialVersionUID = 1L;

private String name;
private String type;
private String status;
private String macAddress;
private Integer mtu;
private Boolean dhcp4;
private Boolean dhcp6;

public NetworkInterface() {
// Default constructor
}

public NetworkInterface(final String name, final String type, final String status, final String macAddress, final Integer mtu,
final Boolean dhcp4, final Boolean dhcp6) {
this.name = name;
this.type = type;
this.status = status;
this.macAddress = macAddress;
this.mtu = mtu;
this.dhcp4 = dhcp4;
this.dhcp6 = dhcp6;
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
return "NetworkInterface{" + "name='" + name + '\'' + ", type='" + type + '\'' + ", status='" + status + '\'' + ", macAddress='"
+ macAddress + '\'' + ", mtu=" + mtu + ", dhcp4=" + dhcp4 + ", dhcp6=" + dhcp6 + '}';
}
}
