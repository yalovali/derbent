package tech.derbent.bab.dashboard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.dashboard.dto.CNetworkInterface;
import tech.derbent.bab.dashboard.dto.CNetworkInterfaceIpConfiguration;
import tech.derbent.bab.dashboard.dto.CNetworkInterfaceIpUpdate;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.api.ui.notifications.CNotificationService;

/** Helper client responsible for retrieving and updating network interface information via Calimero HTTP API. */
public class CNetworkInterfaceCalimeroClient extends CAbstractCalimeroClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(CNetworkInterfaceCalimeroClient.class);

	public CNetworkInterfaceCalimeroClient(final CClientProject clientProject) {
		super(clientProject);
	}

	public List<CNetworkInterface> fetchInterfaces() {
		final List<CNetworkInterface> interfaces = new ArrayList<>();
		final CCalimeroRequest request = CCalimeroRequest.builder().type("network").operation("getInterfaces").build();
		LOGGER.info("📤 Fetching network interfaces from Calimero");
		
		try {
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				final String message = "Failed to load interface list: " + response.getErrorMessage();
				LOGGER.warn("⚠️ {}", message);
				// Don't show notification here - let caller handle it for graceful degradation
				return interfaces;
			}
			
			final JsonObject data = toJsonObject(response);
			if (data.has("interfaces") && data.get("interfaces").isJsonArray()) {
				for (final JsonElement element : data.getAsJsonArray("interfaces")) {
					if (element.isJsonObject()) {
						final CNetworkInterface iface = CNetworkInterface.createFromJson(element.getAsJsonObject());
						// Fetch detailed configuration for each interface
						enrichInterfaceWithDetailedInfo(iface);
						interfaces.add(iface);
					}
				}
			}
			LOGGER.info("✅ Fetched {} network interfaces from Calimero", interfaces.size());
			return interfaces;
		} catch (final IllegalStateException e) {
			// Authentication/Authorization exceptions - propagate to caller
			LOGGER.error("🔐❌ Authentication error while fetching interfaces: {}", e.getMessage());
			throw e;
		} catch (final Exception e) {
			LOGGER.error("❌ Failed to parse interface payload: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to parse interface payload", e);
			return interfaces;
		}
	}

	/**
	 * Enrich interface with detailed IP configuration.
	 * @param iface Network interface to enrich
	 */
	public void enrichInterfaceIpConfiguration(final CNetworkInterface iface) {
		iface.setIpConfiguration(fetchIpConfiguration(iface.getName()).orElse(null));
	}

	/**
	 * Fetch detailed interface information including addresses and gateway.
	 * Uses Calimero getInterface operation which provides full configuration.
	 * @param iface Network interface to enrich
	 */
	public void enrichInterfaceWithDetailedInfo(final CNetworkInterface iface) {
		Check.notNull(iface, "Interface cannot be null");
		final String interfaceName = iface.getName();
		
		// Fetch detailed interface info from Calimero
		final CCalimeroRequest request = CCalimeroRequest.builder()
				.type("network")
				.operation("getInterface")
				.parameter("interface", interfaceName)
				.build();
		
		final CCalimeroResponse response = clientProject.sendRequest(request);
		if (!response.isSuccess()) {
			LOGGER.warn("Unable to fetch detailed info for {}: {}", interfaceName, response.getErrorMessage());
			return;
		}
		
		try {
			final JsonObject data = toJsonObject(response);
			
			// Extract and set detailed configuration
			if (data.has("addresses") && data.get("addresses").isJsonArray()) {
				final JsonArray addressArray = data.getAsJsonArray("addresses");
				final List<String> addresses = new ArrayList<>();
				addressArray.forEach(element -> addresses.add(element.getAsString()));
				iface.getAddresses().clear();
				iface.getAddresses().addAll(addresses);
			}
			
			// Create IP configuration from detailed data
			final CNetworkInterfaceIpConfiguration config = new CNetworkInterfaceIpConfiguration();
			config.setInterfaceName(interfaceName);
			
			// Extract IPv4 address and prefix from first address (format: "192.168.1.100/24")
			if (!iface.getAddresses().isEmpty()) {
				final String firstAddress = iface.getAddresses().get(0);
				if (firstAddress.contains("/")) {
					final String[] parts = firstAddress.split("/");
					config.setIpv4Address(parts[0]);
					if (parts.length > 1) {
						try {
							config.setIpv4PrefixLength(Integer.parseInt(parts[1]));
						} catch (final NumberFormatException e) {
							LOGGER.warn("Invalid prefix length in address: {}", firstAddress);
						}
					}
				} else {
					config.setIpv4Address(firstAddress);
				}
			}
			
			// Extract gateway
			if (data.has("gateway4")) {
				config.setIpv4Gateway(data.get("gateway4").getAsString());
			}
			
			// Extract DNS servers
			if (data.has("nameservers") && data.get("nameservers").isJsonArray()) {
				final JsonArray nameservers = data.getAsJsonArray("nameservers");
				final List<String> dnsList = new ArrayList<>();
				nameservers.forEach(element -> dnsList.add(element.getAsString()));
				config.setNameservers(dnsList);
			}
			
			// Set DHCP flags
			if (data.has("dhcp4")) {
				iface.setDhcp4(data.get("dhcp4").getAsBoolean());
			}
			if (data.has("dhcp6")) {
				iface.setDhcp6(data.get("dhcp6").getAsBoolean());
			}
			
			iface.setIpConfiguration(config);
			LOGGER.debug("Enriched interface {} with detailed configuration", interfaceName);
			
		} catch (final Exception e) {
			LOGGER.warn("Failed to parse detailed info for {}: {}", interfaceName, e.getMessage());
		}
	}

	public Optional<CNetworkInterfaceIpConfiguration> fetchIpConfiguration(final String interfaceName) {
		Check.notBlank(interfaceName, "Interface name required");
		final CCalimeroRequest request =
				CCalimeroRequest.builder().type("network").operation("getIP").parameter("interface", interfaceName).build();
		final CCalimeroResponse response = clientProject.sendRequest(request);
		if (!response.isSuccess()) {
			LOGGER.warn("Unable to fetch IP configuration for {}: {}", interfaceName, response.getErrorMessage());
			return Optional.empty();
		}
		try {
			final JsonObject data = toJsonObject(response);
			data.addProperty("interface", interfaceName);
			final CNetworkInterfaceIpConfiguration config = CNetworkInterfaceIpConfiguration.fromJsonObject(data);
			return Optional.ofNullable(config);
		} catch (final Exception e) {
			LOGGER.warn("Failed to parse IP configuration for {}: {}", interfaceName, e.getMessage());
			return Optional.empty();
		}
	}

	public CCalimeroResponse updateInterfaceIp(final CNetworkInterfaceIpUpdate update) {
		// Build request according to Calimero API specification
		// Mode: "dhcp" or "static"
		// For static: ip, netmask (full format like 255.255.255.0), gateway (optional)
		
		final CCalimeroRequest.Builder builder = CCalimeroRequest.builder()
			.type("network")
			.operation("setIP")
			.parameter("interface", update.getInterfaceName());
		
		if (update.isUseDhcp()) {
			// DHCP mode - only mode parameter needed
			builder.parameter("mode", "dhcp");
		} else {
			// Static mode - requires mode, ip, netmask
			builder.parameter("mode", "static");
			builder.parameter("ip", update.getIpv4Address());
			
			// Convert prefix length to netmask format (e.g., 24 -> 255.255.255.0)
			if (update.getPrefixLength() != null) {
				final String netmask = prefixLengthToNetmask(update.getPrefixLength());
				builder.parameter("netmask", netmask);
			}
			
			// Gateway is optional for static mode
			if (update.getGateway() != null && !update.getGateway().isEmpty()) {
				builder.parameter("gateway", update.getGateway());
			}
		}
		
		LOGGER.info("📤 Updating interface {} - mode: {}, IP: {}, prefix: {}", 
			update.getInterfaceName(), 
			update.isUseDhcp() ? "dhcp" : "static",
			update.getIpv4Address(),
			update.getPrefixLength());
		
		final CCalimeroResponse response = clientProject.sendRequest(builder.build());
		
		if (response.isSuccess()) {
			LOGGER.info("✅ Successfully updated interface {}", update.getInterfaceName());
		} else {
			LOGGER.error("❌ Failed to update interface {}: {}", 
				update.getInterfaceName(), response.getErrorMessage());
		}
		
		return response;
	}
	
	/**
	 * Convert CIDR prefix length to dotted decimal netmask.
	 * Examples: 24 -> 255.255.255.0, 16 -> 255.255.0.0, 8 -> 255.0.0.0
	 * 
	 * @param prefixLength CIDR prefix length (0-32)
	 * @return Dotted decimal netmask string
	 */
	private String prefixLengthToNetmask(final int prefixLength) {
		if (prefixLength < 0 || prefixLength > 32) {
			throw new IllegalArgumentException("Prefix length must be between 0 and 32, got: " + prefixLength);
		}
		
		// Create 32-bit mask with prefixLength bits set to 1
		final long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
		
		// Extract octets
		final int octet1 = (int) ((mask >> 24) & 0xFF);
		final int octet2 = (int) ((mask >> 16) & 0xFF);
		final int octet3 = (int) ((mask >> 8) & 0xFF);
		final int octet4 = (int) (mask & 0xFF);
		
		return String.format("%d.%d.%d.%d", octet1, octet2, octet3, octet4);
	}
}
