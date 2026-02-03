package tech.derbent.bab.http.clientproject.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.bab.http.domain.CConnectionResult;
import tech.derbent.bab.http.domain.CHttpResponse;
import tech.derbent.bab.http.service.CHttpService;
import tech.derbent.bab.project.domain.CProject_Bab;

/** HTTP client for communicating with Calimero server. One instance per project, manages connection lifecycle. */
public class CClientProject {

	private static final String DEFAULT_PORT = "8077";
	private static final Logger LOGGER = LoggerFactory.getLogger(CClientProject.class);

	// Configuration
	private final CProject_Bab project; // BAB project entity (single source of truth)
	private final String targetPort;
	private final CHttpService httpService;
	// State
	private boolean connected = false;
	private LocalDateTime lastConnectionTime;
	private LocalDateTime lastRequestTime;
	private long totalRequests = 0;
	private long failedRequests = 0;

	/** 
	 * Create HTTP client with custom port.
	 * @param project BAB project entity (required)
	 * @param httpService HTTP service for requests (required)
	 * @param targetPort Calimero server port (optional, defaults to 8077)
	 */
	public CClientProject(final CProject_Bab project, 
	                      final CHttpService httpService,
	                      final String targetPort) {
		this.project = Objects.requireNonNull(project, "project required");
		this.httpService = Objects.requireNonNull(httpService, "httpService required");
		this.targetPort = targetPort != null ? targetPort : DEFAULT_PORT;
	}

	/** 
	 * Create HTTP client with default port (8077).
	 * @param project BAB project entity (required)
	 * @param httpService HTTP service for requests (required)
	 */
	public CClientProject(final CProject_Bab project, 
	                      final CHttpService httpService) {
		this(project, httpService, DEFAULT_PORT);
	}

	/** Build full URL for endpoint.
	 * @param endpoint Endpoint path (e.g., "/health", "/api/request")
	 * @return Full URL */
	private String buildUrl(final String endpoint) {
		return String.format("http://%s:%s%s", project.getIpAddress(), targetPort, endpoint);
	}

	/** Connect to Calimero server and verify availability.
	 * @return Connection result with status and details */
	public CConnectionResult connect() {
		try {
			LOGGER.info("🔌 Connecting project '{}' to Calimero server at {}:{}", project.getName(), project.getIpAddress(), targetPort);
			// Health check endpoint
			final String healthUrl = buildUrl("/health");
			final CHttpResponse response = httpService.healthCheck(healthUrl);
			if (response.isSuccess()) {
				connected = true;
				lastConnectionTime = LocalDateTime.now();
				LOGGER.info("✅ Successfully connected to Calimero server");
				return CConnectionResult.success("Connected to Calimero at " + project.getIpAddress() + ":" + targetPort, project.getIpAddress(),
						Integer.parseInt(targetPort));
			}
			connected = false;
			LOGGER.warn("❌ Failed to connect: {}", response.getStatusCode());
			return CConnectionResult.failure("Connection failed: " + response.getErrorMessage(), project.getIpAddress(),
					Integer.parseInt(targetPort));
		} catch (final Exception e) {
			connected = false;
			LOGGER.error("❌ Connection error: {}", e.getMessage(), e);
			return CConnectionResult.failure("Connection error: " + e.getMessage(), project.getIpAddress(), Integer.parseInt(targetPort));
		}
	}

	/** Disconnect from Calimero server. */
	public void disconnect() {
		connected = false;
		LOGGER.info("🔌 Disconnected project '{}' from Calimero server", project.getName());
	}

	public long getFailedRequests() { return failedRequests; }

	public LocalDateTime getLastConnectionTime() { return lastConnectionTime; }

	public LocalDateTime getLastRequestTime() { return lastRequestTime; }

	/** Get connection statistics.
	 * @return Statistics string */
	public String getStatistics() {
		return String.format("Project: %s | Connected: %s | Requests: %d | Failed: %d | Last: %s", project.getName(), connected, totalRequests,
				failedRequests, lastRequestTime);
	}

	public String getTargetUrl() { return buildUrl(""); }

	public long getTotalRequests() { return totalRequests; }

	// Getters
	public boolean isConnected() { return connected; }

	/** Send "Hello" test message to Calimero server. Verifies bidirectional communication.
	 * @return Calimero server response */
	public CCalimeroResponse sayHello() {
		LOGGER.info("👋 Sending Hello message to Calimero server from project '{}'", project.getName());
		try {
			// Build hello request with proper Calimero API format
			final CCalimeroRequest.Builder requestBuilder =
					CCalimeroRequest.builder().type("system").operation("info").parameter("project_id", project.getId().toString())
							.parameter("project_name", project.getName()).parameter("timestamp", System.currentTimeMillis());
			// Add authentication header if token is configured
			if ((project.getAuthToken() != null) && !project.getAuthToken().isBlank()) {
				requestBuilder.header("Authorization", "Bearer " + project.getAuthToken());
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
			if ((project.getAuthToken() != null) && !project.getAuthToken().isBlank() && !request.getHeaders().containsKey("Authorization")) {
				requestBuilder.header("Authorization", "Bearer " + project.getAuthToken());
				LOGGER.info("🔐 Adding authentication token to request");
			}
			final CCalimeroRequest authenticatedRequest = requestBuilder.build();
			final String apiUrl = buildUrl("/api/request");
			LOGGER.info("📤 Sending request: type={}, operation={}, url={}", authenticatedRequest.getType(), authenticatedRequest.getOperation(),
					apiUrl);
			final CHttpResponse httpResponse = httpService.sendPost(apiUrl, authenticatedRequest.toJson(), authenticatedRequest.getHeaders());
			totalRequests++;
			lastRequestTime = LocalDateTime.now();
			if (httpResponse.isSuccess()) {
				LOGGER.info("✅ Request successful: status={}", httpResponse.getStatusCode());
				return CCalimeroResponse.fromJson(httpResponse.getBody());
			}
			failedRequests++;
			// Check for authentication/authorization errors
			if (httpResponse.getStatusCode() == 401) {
				LOGGER.error("🔐❌ AUTHENTICATION FAILED: Invalid or missing authorization token");
				throw new IllegalStateException(
						"Authentication failed: Invalid or missing authorization token. " + "Please check your Calimero API token configuration.");
			} else if (httpResponse.getStatusCode() == 403) {
				LOGGER.error("🔐❌ AUTHORIZATION FAILED: Access denied");
				throw new IllegalStateException(
						"Authorization failed: Access denied for this resource. " + "Please verify your token has the required permissions.");
			}
			LOGGER.warn("⚠️ Request failed: status={}, error={}", httpResponse.getStatusCode(), httpResponse.getErrorMessage());
			return CCalimeroResponse.error(httpResponse.getErrorMessage());
		} catch (final IllegalStateException e) {
			// Re-throw authentication/authorization exceptions (don't reset connection)
			failedRequests++;
			throw e;
		} catch (final Exception e) {
			failedRequests++;
			// Check if this is a connection-related error
			final String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
			final boolean isConnectionError = errorMsg.contains("connection refused") || errorMsg.contains("connection reset")
					|| errorMsg.contains("connect timed out") || errorMsg.contains("no route to host") || errorMsg.contains("network is unreachable")
					|| (e instanceof java.net.ConnectException) || (e instanceof java.net.SocketTimeoutException) || ((e.getCause() != null)
							&& ((e.getCause() instanceof java.net.ConnectException) || (e.getCause() instanceof java.net.SocketTimeoutException)));
			if (isConnectionError) {
				// Connection error - reset connection flag to trigger reconnect on next request
				connected = false;
				LOGGER.error("🔌❌ Connection lost: {} - connection flag reset, will attempt reconnect on next request", e.getMessage());
				return CCalimeroResponse.error("Connection lost: " + e.getMessage());
			}
			// Other errors - log but keep connection flag (may be transient)
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
