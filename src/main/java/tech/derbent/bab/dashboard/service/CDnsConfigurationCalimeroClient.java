package tech.derbent.bab.dashboard.service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.derbent.bab.dashboard.dto.CDnsConfigurationUpdate;
import tech.derbent.bab.dashboard.dto.CDnsServer;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;

/** CDnsConfigurationCalimeroClient - Client for DNS configuration operations via Calimero HTTP API.
 * <p>
 * Handles DNS server queries and configuration management for BAB Gateway projects. Uses the Calimero HTTP API to retrieve DNS server information
 * from the remote system.
 * <p>
 * API Operations:
 * <ul>
 * <li>getDnsServers - Get DNS server configuration</li>
 * <li>setDns - Apply DNS server configuration</li>
 * </ul>
 */
public class CDnsConfigurationCalimeroClient {

	private static final Gson GSON = new Gson();
	private static final Logger LOGGER = LoggerFactory.getLogger(CDnsConfigurationCalimeroClient.class);
	private final CClientProject clientProject;

	public CDnsConfigurationCalimeroClient(final CClientProject clientProject) {
		this.clientProject = clientProject;
	}

	/** Apply DNS server configuration to the system.
	 * <p>
	 * Uses Calimero HTTP API to configure DNS servers persistently via nmcli or systemd-resolved. The first server in the list becomes the primary
	 * DNS server.
	 * <p>
	 * Calimero API: POST /api/request with type="network", operation="setDns"
	 * @param update DNS configuration update containing nameservers list
	 * @return true if configuration was applied successfully */
	public boolean applyDnsConfiguration(final CDnsConfigurationUpdate update) {
		if (update == null || !update.isValid()) {
			LOGGER.warn("⚠️ Invalid DNS configuration update: {}", update);
			return false;
		}
		LOGGER.info("📤 Applying DNS configuration to Calimero: {}", update);
		try {
			// Build request with nameservers as parameter
			final CCalimeroRequest request =
					CCalimeroRequest.builder().type("network").operation("setDns").parameter("nameservers", update.getNameservers()).build();
			final CCalimeroResponse response = clientProject.sendRequest(request);
			if (!response.isSuccess()) {
				final String message = "Failed to apply DNS configuration: " + response.getErrorMessage();
				LOGGER.warn("⚠️ {}", message);
				return false;
			}
			LOGGER.info("✅ DNS configuration applied successfully: {} server(s)", update.getServerCount());
			return true;
		} catch (final IllegalStateException e) {
			// Authentication/Authorization exceptions - propagate to caller
			LOGGER.error("🔐❌ Authentication error while applying DNS config: {}", e.getMessage());
			throw e;
		} catch (final Exception e) {
			LOGGER.error("❌ Failed to apply DNS configuration: {}", e.getMessage(), e);
			return false;
		}
	}

	/** Fetch DNS server configuration from Calimero.
	 * @return List of DNS servers configured on the system */
	public List<CDnsServer> fetchDnsServers() {
		final List<CDnsServer> dnsServers = new ArrayList<>();
		final CCalimeroRequest request = CCalimeroRequest.builder().type("network").operation("getDnsServers")
				.build();
		LOGGER.info("📤 Fetching DNS configuration from Calimero");
		try {
			final CCalimeroResponse response = clientProject.sendRequest(request);
			if (!response.isSuccess()) {
				final String message = "Failed to load DNS configuration: " + response.getErrorMessage();
				LOGGER.warn("⚠️ {}", message);
				// Don't show notification here - let caller handle it for graceful degradation
				return dnsServers;
			}
			final JsonObject data = toJsonObject(response);
			// Parse structured DNS info (resolvectl format)
			if (data.has("dnsInfo") && data.get("dnsInfo").isJsonArray()) {
				final JsonArray dnsInfoArray = data.getAsJsonArray("dnsInfo");
				for (final JsonElement element : dnsInfoArray) {
					if (element.isJsonObject()) {
						final JsonObject dnsInfo = element.getAsJsonObject();
						parseDnsInfoEntry(dnsInfo, dnsServers);
					}
				}
			}
			// Parse simple server list (resolv.conf fallback)
			else if (data.has("servers") && data.get("servers").isJsonArray()) {
				final JsonArray serversArray = data.getAsJsonArray("servers");
				boolean isFirst = true;
				for (final JsonElement element : serversArray) {
					if (!element.isJsonNull()) {
						final String serverIp = element.getAsString();
						final CDnsServer dnsServer = new CDnsServer(serverIp);
						dnsServer.setIsPrimary(isFirst);
						dnsServer.setSource("resolv.conf");
						dnsServers.add(dnsServer);
						isFirst = false;
					}
				}
			}
			LOGGER.info("✅ Fetched {} DNS servers from Calimero", dnsServers.size());
			return dnsServers;
		} catch (final IllegalStateException e) {
			// Authentication/Authorization exceptions - propagate to caller
			LOGGER.error("🔐❌ Authentication error while fetching DNS config: {}", e.getMessage());
			throw e;
		} catch (final Exception e) {
			LOGGER.error("❌ Failed to parse DNS configuration: {}", e.getMessage(), e);
			return dnsServers;
		}
	}

	/** Parse DNS info entry from resolvectl format.
	 * @param dnsInfo    JSON object containing interface DNS info
	 * @param dnsServers Output list to add DNS servers to */
	private void parseDnsInfoEntry(final JsonObject dnsInfo, final List<CDnsServer> dnsServers) {
		try {
			final String interfaceName = dnsInfo.has("interface") ? dnsInfo.get("interface").getAsString() : "";
			// Parse servers array
			if (dnsInfo.has("servers") && dnsInfo.get("servers").isJsonArray()) {
				final JsonArray servers = dnsInfo.getAsJsonArray("servers");
				boolean isFirst = true;
				for (final JsonElement serverElement : servers) {
					if (!serverElement.isJsonNull()) {
						final String serverIp = serverElement.getAsString();
						final CDnsServer dnsServer = new CDnsServer(interfaceName, serverIp, "");
						dnsServer.setIsPrimary(isFirst);
						dnsServer.setSource("resolvectl");
						dnsServers.add(dnsServer);
						isFirst = false;
					}
				}
			}
			// Parse domains array
			if (dnsInfo.has("domains") && dnsInfo.get("domains").isJsonArray()) {
				final JsonArray domains = dnsInfo.getAsJsonArray("domains");
				for (final JsonElement domainElement : domains) {
					if (!domainElement.isJsonNull()) {
						final String domain = domainElement.getAsString();
						// Update existing servers with domain info
						for (final CDnsServer server : dnsServers) {
							if (interfaceName.equals(server.getInterfaceName()) && server.getDomain().isEmpty()) {
								server.setDomain(domain);
								break; // Only set domain for first server per interface
							}
						}
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.warn("Failed to parse DNS info entry: {}", e.getMessage());
		}
	}

	private JsonObject toJsonObject(final CCalimeroResponse response) {
		return GSON.fromJson(GSON.toJson(response.getData()), JsonObject.class);
	}
}
