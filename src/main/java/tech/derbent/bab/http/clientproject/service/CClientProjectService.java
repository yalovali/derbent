package tech.derbent.bab.http.clientproject.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.service.CHttpService;
import tech.derbent.bab.project.domain.CProject_Bab;

/** Service for creating and managing CClientProject instances. Implements Factory and Registry patterns. */
@Service
@Profile ("bab")
public class CClientProjectService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CClientProjectService.class);
	// Dependencies
	private final CHttpService httpService;
	// Client registry (projectId -> client instance)
	private final Map<String, CClientProject> clientRegistry = new ConcurrentHashMap<>();

	public CClientProjectService(final CHttpService httpService) {
		this.httpService = httpService;
	}

	/** Close all registered clients. Called during application shutdown. */
	public void closeAllClients() {
		LOGGER.info("🔌 Closing {} HTTP clients", clientRegistry.size());
		clientRegistry.values().forEach(CClientProject::disconnect);
		clientRegistry.clear();
		LOGGER.info("✅ All HTTP clients closed");
	}

	/** Close and remove client for project.
	 * @param project Project entity */
	public void closeClient(final CProject_Bab project) {
		Check.notNull(project, "project cannot be null");
		Check.notNull(project.getId(), "project must be persisted");
		final String projectId = project.getId().toString();
		final CClientProject client = clientRegistry.remove(projectId);
		if (client != null) {
			client.disconnect();
			LOGGER.info("🔌 Closed HTTP client for project '{}'", project.getName());
		}
	}

	/** Create new HTTP client for project.
	 * @param projectId   Project identifier
	 * @param projectName Project name
	 * @param ipAddress   Target Calimero server IP
	 * @param authToken   Authentication Bearer token (optional)
	 * @return New client instance */
	public CClientProject createClient(final String projectId, final String projectName, final String ipAddress, 
			final String authToken) {
		Check.notBlank(projectId, "projectId cannot be blank");
		Check.notBlank(projectName, "projectName cannot be blank");
		Check.notBlank(ipAddress, "ipAddress cannot be blank");
		LOGGER.debug("Creating HTTP client for project '{}' at {} with auth: {}", 
				projectName, ipAddress, (authToken != null && !authToken.isBlank()) ? "yes" : "no");
		final CClientProject client =
				CClientProject.builder()
					.projectId(projectId)
					.projectName(projectName)
					.targetIp(ipAddress)
					.authToken(authToken)
					.httpService(httpService)
					.build();
		return client;
	}

	/** Get number of active clients.
	 * @return Number of registered clients */
	public int getActiveClientCount() { return clientRegistry.size(); }

	/** Get statistics for all clients.
	 * @return Statistics string */
	public String getAllStatistics() {
		final StringBuilder sb = new StringBuilder();
		sb.append("HTTP Client Statistics:\n");
		sb.append("Active Clients: ").append(clientRegistry.size()).append("\n");
		clientRegistry.values().forEach(client -> {
			sb.append("  - ").append(client.getStatistics()).append("\n");
		});
		return sb.toString();
	}

	/** Get existing client for project.
	 * @param project Project entity
	 * @return HTTP client or null if not exists */
	public CClientProject getClient(final CProject_Bab project) {
		Check.notNull(project, "project cannot be null");
		Check.notNull(project.getId(), "project must be persisted");
		return clientRegistry.get(project.getId().toString());
	}

	/** Get existing client or create new one for project. Implements singleton pattern per project.
	 * @param project Project entity
	 * @return HTTP client instance */
	public CClientProject getOrCreateClient(final CProject_Bab project) {
		Check.notNull(project, "project cannot be null");
		Check.notNull(project.getId(), "project must be persisted");
		final String projectId = project.getId().toString();
		// Check registry
		final CClientProject existingClient = clientRegistry.get(projectId);
		if (existingClient != null) {
			LOGGER.debug("Returning existing client for project '{}'", project.getName());
			return existingClient;
		}
		// Create new client
		String ipAddress = project.getIpAddress();
		if ((ipAddress == null) || ipAddress.isBlank()) {
			ipAddress = "127.0.0.1"; // Default
			LOGGER.warn("No IP address set for project '{}', using default: {}", project.getName(), ipAddress);
		}
		
		// Get auth token from project (persisted field)
		String authToken = project.getAuthToken();
		if ((authToken == null) || authToken.isBlank()) {
			LOGGER.warn("⚠️ No auth token configured for project '{}' - requests may fail", project.getName());
		} else {
			LOGGER.info("🔐 Using configured auth token for project '{}'", project.getName());
		}
		
		final CClientProject newClient = createClient(projectId, project.getName(), ipAddress, authToken);
		// Register client
		clientRegistry.put(projectId, newClient);
		LOGGER.info("✅ Created and registered new HTTP client for project '{}'", project.getName());
		return newClient;
	}
}
