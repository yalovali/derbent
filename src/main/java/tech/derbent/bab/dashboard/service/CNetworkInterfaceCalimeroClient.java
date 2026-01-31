package tech.derbent.bab.dashboard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.dashboard.view.CNetworkInterface;
import tech.derbent.bab.dashboard.view.CNetworkInterfaceIpConfiguration;
import tech.derbent.bab.dashboard.view.CNetworkInterfaceIpUpdate;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.api.ui.notifications.CNotificationService;

/** Helper client responsible for retrieving and updating network interface information via Calimero HTTP API. */
public class CNetworkInterfaceCalimeroClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(CNetworkInterfaceCalimeroClient.class);
	private static final Gson GSON = new Gson();

	private final CClientProject clientProject;

	public CNetworkInterfaceCalimeroClient(final CClientProject clientProject) {
		this.clientProject = clientProject;
	}

	public List<CNetworkInterface> fetchInterfaces() {
		final List<CNetworkInterface> interfaces = new ArrayList<>();
		final CCalimeroRequest request = CCalimeroRequest.builder().type("network").operation("getInterfaces").build();
		final CCalimeroResponse response = clientProject.sendRequest(request);
		if (!response.isSuccess()) {
			final String message = "Failed to load interface list: " + response.getErrorMessage();
			LOGGER.warn(message);
			CNotificationService.showWarning(message);
			return interfaces;
		}
		try {
			final JsonObject data = toJsonObject(response);
			if (data.has("interfaces") && data.get("interfaces").isJsonArray()) {
				for (final JsonElement element : data.getAsJsonArray("interfaces")) {
					if (element.isJsonObject()) {
						final CNetworkInterface iface = CNetworkInterface.createFromJson(element.getAsJsonObject());
						enrichInterfaceIpConfiguration(iface);
						interfaces.add(iface);
					}
				}
			}
			return interfaces;
		} catch (final Exception e) {
			LOGGER.error("Failed to parse interface payload: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to parse interface payload", e);
			return interfaces;
		}
	}

	public void enrichInterfaceIpConfiguration(final CNetworkInterface iface) {
		iface.setIpConfiguration(fetchIpConfiguration(iface.getName()).orElse(null));
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
		final CCalimeroRequest request = CCalimeroRequest.builder().type("network").operation("setIP")
				.parameter("interface", update.getInterfaceName()).parameter("address", update.toAddressArgument())
				.parameter("gateway", update.getGateway() == null ? "" : update.getGateway()).parameter("readOnly", update.isReadOnly()).build();
		return clientProject.sendRequest(request);
	}

	private JsonObject toJsonObject(final CCalimeroResponse response) {
		return GSON.fromJson(GSON.toJson(response.getData()), JsonObject.class);
	}
}
