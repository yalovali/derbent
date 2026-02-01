package tech.derbent.bab.http.clientproject.domain;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.bab.http.domain.CConnectionResult;
import tech.derbent.bab.http.domain.CHttpResponse;
import tech.derbent.bab.http.service.CHttpService;

/** HTTP client for communicating with Calimero server. One instance per project, manages connection lifecycle. */
public class CClientProject {

	/** Builder for CClientProject instances. */
	public static class Builder {

		private String authToken; // Optional Bearer token for authentication
		private CHttpService httpService;
		private String projectId;
		private String projectName;
		private String targetIp = "127.0.0.1";
		private String targetPort = DEFAULT_PORT;

		public Builder authToken(final String authToken1) {
			authToken = authToken1;
			return this;
		}

		public CClientProject build() {
			if (projectId == null) {
				throw new IllegalArgumentException("projectId required");
			}
			if (projectName == null) {
				throw new IllegalArgumentException("projectName required");
			}
			if (httpService == null) {
				throw new IllegalArgumentException("httpService required");
			}
			return new CClientProject(projectId, projectName, targetIp, targetPort, authToken, httpService);
		}

		public Builder httpService(final CHttpService httpService1) {
			httpService = httpService1;
			return this;
		}

		public Builder projectId(final String projectId1) {
			projectId = projectId1;
			return this;
		}

		public Builder projectName(final String projectName1) {
			projectName = projectName1;
			return this;
		}

		public Builder targetIp(final String targetIp1) {
			targetIp = targetIp1;
			return this;
		}

		public Builder targetPort(final String targetPort1) {
			targetPort = targetPort1;
			return this;
		}
	}

	private static final String DEFAULT_PORT = "8077";
	private static final Logger LOGGER = LoggerFactory.getLogger(CClientProject.class);

	public static Builder builder() {
		return new Builder();
	}

	private final String authToken; // Bearer token for API authentication
	// State
	private boolean connected = false;
	private long failedRequests = 0;
	private final CHttpService httpService;
	private LocalDateTime lastConnectionTime;
	private LocalDateTime lastRequestTime;
	// Configuration
	private final String projectId;
	private final String projectName;
	private final String targetIp;
	private final String targetPort;
	private long totalRequests = 0;

	/** Private constructor - use Builder or CClientProjectService factory. */
	private CClientProject(final String projectId, final String projectName, final String targetIp, final String targetPort, final String authToken,
			final CHttpService httpService) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.targetIp = targetIp;
		this.targetPort = targetPort;
		this.authToken = authToken;
		this.httpService = httpService;
	}

	/** Build full URL for endpoint.
	 * @param endpoint Endpoint path (e.g., "/health", "/api/request")
	 * @return Full URL */
	private String buildUrl(final String endpoint) {
		return String.format("http://%s:%s%s", targetIp, targetPort, endpoint);
	}

	/** Connect to Calimero server and verify availability.
	 * @return Connection result with status and details */
	public CConnectionResult connect() {
		try {
			LOGGER.info("🔌 Connecting project '{}' to Calimero server at {}:{}", projectName, targetIp, targetPort);
			// Health check endpoint
			final String healthUrl = buildUrl("/health");
			final CHttpResponse response = httpService.healthCheck(healthUrl);
			if (response.isSuccess()) {
				connected = true;
				lastConnectionTime = LocalDateTime.now();
				LOGGER.info("✅ Successfully connected to Calimero server");
				return CConnectionResult.success("Connected to Calimero at " + targetIp + ":" + targetPort, targetIp, Integer.parseInt(targetPort));
			}
			connected = false;
			LOGGER.warn("❌ Failed to connect: {}", response.getStatusCode());
			return CConnectionResult.failure("Connection failed: " + response.getErrorMessage(), targetIp, Integer.parseInt(targetPort));
		} catch (final Exception e) {
			connected = false;
			LOGGER.error("❌ Connection error: {}", e.getMessage(), e);
			return CConnectionResult.failure("Connection error: " + e.getMessage(), targetIp, Integer.parseInt(targetPort));
		}
	}

	/** Disconnect from Calimero server. */
	public void disconnect() {
		connected = false;
		LOGGER.info("🔌 Disconnected project '{}' from Calimero server", projectName);
	}

	public long getFailedRequests() { return failedRequests; }

	public LocalDateTime getLastConnectionTime() { return lastConnectionTime; }

	public LocalDateTime getLastRequestTime() { return lastRequestTime; }

	/** Get connection statistics.
	 * @return Statistics string */
	public String getStatistics() {
		return String.format("Project: %s | Connected: %s | Requests: %d | Failed: %d | Last: %s", projectName, connected, totalRequests,
				failedRequests, lastRequestTime);
	}

	public String getTargetUrl() { return buildUrl(""); }

	public long getTotalRequests() { return totalRequests; }

	// Getters
	public boolean isConnected() { return connected; }

	/** Send "Hello" test message to Calimero server. Verifies bidirectional communication.
	 * @return Calimero server response */
	public CCalimeroResponse sayHello() {
		LOGGER.info("👋 Sending Hello message to Calimero server from project '{}'", projectName);
		try {
			// Build hello request with proper Calimero API format
			final CCalimeroRequest.Builder requestBuilder = CCalimeroRequest.builder().type("system").operation("info")
					.parameter("project_id", projectId).parameter("project_name", projectName).parameter("timestamp", System.currentTimeMillis());
			// Add authentication header if token is configured
			if (authToken != null && !authToken.isBlank()) {
				requestBuilder.header("Authorization", "Bearer " + authToken);
				LOGGER.debug("🔐 Added auth token to request");
			}
			final CCalimeroRequest request = requestBuilder.build();
			// Send request to Calimero's POST endpoint
			final String apiUrl = buildUrl("/api/request");
			final CHttpResponse httpResponse = httpService.sendPost(apiUrl, request.toJson(), request.getHeaders());
			totalRequests++;
			lastRequestTime = LocalDateTime.now();
			if (httpResponse.isSuccess()) {
				LOGGER.info("✅ Hello response received: {}", httpResponse.getBody());
				return CCalimeroResponse.fromJson(httpResponse.getBody());
			}
			failedRequests++;
			LOGGER.warn("⚠️ Hello request failed: {}", httpResponse.getErrorMessage());
			return CCalimeroResponse.error(httpResponse.getErrorMessage());
		} catch (final Exception e) {
			failedRequests++;
			LOGGER.error("❌ Error sending Hello: {}", e.getMessage(), e);
			return CCalimeroResponse.error("Error: " + e.getMessage());
		}
	}

	/** Send generic request to Calimero server.
	 * @param request Calimero request object
	 * @return Calimero response */
	public CCalimeroResponse sendRequest(final CCalimeroRequest request) {
		if (!connected) {
			LOGGER.warn("⚠️ Not connected - attempting to connect first");
			final CConnectionResult result = connect();
			if (!result.isSuccess()) {
				return CCalimeroResponse.error("Not connected: " + result.getMessage());
			}
		}
		try {
			// Add authentication header if token is configured and not already in request
			final CCalimeroRequest.Builder requestBuilder =
					CCalimeroRequest.builder().type(request.getType()).operation(request.getOperation()).parameters(request.getParameters());
			// Copy existing headers
			request.getHeaders().forEach(requestBuilder::header);
			// Add auth token if configured and not present
			if (authToken != null && !authToken.isBlank() && !request.getHeaders().containsKey("Authorization")) {
				requestBuilder.header("Authorization", "Bearer " + authToken);
				LOGGER.debug("🔐 Added auth token to request");
			}
			final CCalimeroRequest authenticatedRequest = requestBuilder.build();
			final String apiUrl = buildUrl("/api/request");
			final CHttpResponse httpResponse = httpService.sendPost(apiUrl, authenticatedRequest.toJson(), authenticatedRequest.getHeaders());
			totalRequests++;
			lastRequestTime = LocalDateTime.now();
			if (httpResponse.isSuccess()) {
				return CCalimeroResponse.fromJson(httpResponse.getBody());
			}
			failedRequests++;
			return CCalimeroResponse.error(httpResponse.getErrorMessage());
		} catch (final Exception e) {
			failedRequests++;
			LOGGER.error("❌ Request error: {}", e.getMessage(), e);
			return CCalimeroResponse.error("Error: " + e.getMessage());
		}
	}

	/** Send asynchronous request to Calimero server.
	 * @param request Calimero request object
	 * @return CompletableFuture with response */
	public CompletableFuture<CCalimeroResponse> sendRequestAsync(final CCalimeroRequest request) {
		return CompletableFuture.supplyAsync(() -> sendRequest(request));
	}
}
