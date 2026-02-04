package tech.derbent.bab.dashboard.dashboardproject_bab.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/** Represents IPv4/IPv6 configuration for a Calimero network interface. */
public class CDTONetworkInterfaceIpConfiguration extends CObject {
	private static final long serialVersionUID = 1L;

	public static CDTONetworkInterfaceIpConfiguration fromJsonObject(final JsonObject json) {
		final CDTONetworkInterfaceIpConfiguration config = new CDTONetworkInterfaceIpConfiguration();
		config.fromJson(json);
		return config;
	}

	private String interfaceName;
	private String ipv4Address;
	private Integer ipv4PrefixLength;
	private String ipv4Netmask;
	private String ipv4Gateway;
	private String ipv6Address;
	private Integer ipv6PrefixLength;
	private List<String> nameservers = new ArrayList<>();

	@Override
	protected void fromJson(final JsonObject json) {
		if (json.has("interface")) {
			interfaceName = json.get("interface").getAsString();
		}
		if (json.has("ipv4") && json.get("ipv4").isJsonObject()) {
			parseIpv4(json.getAsJsonObject("ipv4"));
		}
		if (json.has("ipv6") && json.get("ipv6").isJsonObject()) {
			parseIpv6(json.getAsJsonObject("ipv6"));
		}
		if (json.has("nameservers") && json.get("nameservers").isJsonArray()) {
			final JsonArray nsArray = json.getAsJsonArray("nameservers");
			nameservers.clear();
			nsArray.forEach(element -> nameservers.add(element.getAsString()));
		}
	}

	public String getInterfaceName() { return interfaceName; }

	public String getIpv4Address() { return ipv4Address; }

	public Optional<String> getIpv4AddressDisplay() {
		if ((ipv4Address == null) || ipv4Address.isBlank()) {
			return Optional.empty();
		}
		if (ipv4PrefixLength != null) {
			return Optional.of(ipv4Address + "/" + ipv4PrefixLength);
		}
		return Optional.of(ipv4Address);
	}

	public String getIpv4Gateway() { return ipv4Gateway; }

	public String getIpv4GatewayOrDash() { return ((ipv4Gateway == null) || ipv4Gateway.isBlank()) ? "-" : ipv4Gateway; }

	public String getIpv4LabelOrDash() { return getIpv4AddressDisplay().orElse("-"); }

	public String getIpv4Netmask() { return ipv4Netmask; }

	public Integer getIpv4PrefixLength() { return ipv4PrefixLength; }

	public String getIpv6Address() { return ipv6Address; }

	public Integer getIpv6PrefixLength() { return ipv6PrefixLength; }

	public List<String> getNameservers() { return nameservers; }

	public String getNameserversDisplay() {
		return nameservers.isEmpty() ? "-" : String.join(", ", nameservers);
	}

	private void parseIpv4(final JsonObject ipv4Json) {
		if (ipv4Json.has("address")) {
			ipv4Address = ipv4Json.get("address").getAsString();
		}
		if (ipv4Json.has("cidr")) {
			final String cidr = ipv4Json.get("cidr").getAsString();
			parsePrefixFromCidr(cidr);
		}
		if (ipv4Json.has("netmask")) {
			ipv4Netmask = ipv4Json.get("netmask").getAsString();
		}
		if (ipv4Json.has("gateway")) {
			ipv4Gateway = ipv4Json.get("gateway").getAsString();
		}
		if ((ipv4PrefixLength == null) && ipv4Json.has("prefixLength")) {
			ipv4PrefixLength = ipv4Json.get("prefixLength").getAsInt();
		}
	}

	private void parseIpv6(final JsonObject ipv6Json) {
		if (ipv6Json.has("address")) {
			ipv6Address = ipv6Json.get("address").getAsString();
		}
		if (ipv6Json.has("prefixLength")) {
			ipv6PrefixLength = ipv6Json.get("prefixLength").getAsInt();
		}
		if (ipv6Json.has("cidr")) {
			final String cidr = ipv6Json.get("cidr").getAsString();
			final int slashIndex = cidr.indexOf('/');
			if (slashIndex > 0) {
				ipv6Address = cidr.substring(0, slashIndex);
				if ((slashIndex + 1) < cidr.length()) {
					try {
						ipv6PrefixLength = Integer.parseInt(cidr.substring(slashIndex + 1));
					} catch (final NumberFormatException ignored) { /***/
					}
				}
			}
		}
	}

	private void parsePrefixFromCidr(final String cidr) {
		if ((cidr == null) || cidr.isBlank()) {
			return;
		}
		final int slashIndex = cidr.indexOf('/');
		if (slashIndex > 0) {
			ipv4Address = cidr.substring(0, slashIndex);
			if ((slashIndex + 1) < cidr.length()) {
				try {
					ipv4PrefixLength = Integer.parseInt(cidr.substring(slashIndex + 1));
				} catch (final NumberFormatException ignored) { /***/
				}
			}
		}
	}

	public void setInterfaceName(final String interfaceName) { this.interfaceName = interfaceName; }

	public void setIpv4Address(final String ipv4Address) { this.ipv4Address = ipv4Address; }

	public void setIpv4Gateway(final String ipv4Gateway) { this.ipv4Gateway = ipv4Gateway; }

	public void setIpv4PrefixLength(final Integer ipv4PrefixLength) { this.ipv4PrefixLength = ipv4PrefixLength; }

	public void setIpv6Address(final String ipv6Address) { this.ipv6Address = ipv6Address; }

	public void setIpv6PrefixLength(final Integer ipv6PrefixLength) { this.ipv6PrefixLength = ipv6PrefixLength; }

	public void setNameservers(final List<String> nameservers) {
		this.nameservers = nameservers != null ? nameservers : new ArrayList<>();
	}

	@Override
	protected String toJson() {
		final JsonObject json = new JsonObject();
		if (interfaceName != null) {
			json.addProperty("interface", interfaceName);
		}
		final JsonObject ipv4 = new JsonObject();
		if (ipv4Address != null) {
			ipv4.addProperty("address", ipv4Address);
		}
		if (ipv4PrefixLength != null) {
			ipv4.addProperty("prefixLength", ipv4PrefixLength);
		}
		if (ipv4Netmask != null) {
			ipv4.addProperty("netmask", ipv4Netmask);
		}
		if (ipv4Gateway != null) {
			ipv4.addProperty("gateway", ipv4Gateway);
		}
		json.add("ipv4", ipv4);
		final JsonObject ipv6 = new JsonObject();
		if (ipv6Address != null) {
			ipv6.addProperty("address", ipv6Address);
		}
		if (ipv6PrefixLength != null) {
			ipv6.addProperty("prefixLength", ipv6PrefixLength);
		}
		json.add("ipv6", ipv6);
		if (!nameservers.isEmpty()) {
			final JsonArray nsArray = new JsonArray();
			nameservers.forEach(nsArray::add);
			json.add("nameservers", nsArray);
		}
		return json.toString();
	}
}
