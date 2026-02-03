package tech.derbent.bab.project.domain;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Pattern;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.clientproject.service.CClientProjectService;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.bab.http.domain.CConnectionResult;

/** CProject_Bab - BAB Gateway-specific project with IP address and HTTP client support. Features: - IP address field (persisted) for Calimero server
 * location - HTTP client field (transient) for Calimero communication - Connection management methods (connectToCalimero, sayHello) Layer: Domain
 * (MVC) Active when: 'bab' profile is active Calimero Server: ~/git/calimero (HTTP API port 8077) */
@Entity
@DiscriminatorValue ("BAB")
public class CProject_Bab extends CProject<CProject_Bab> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CProject_Bab.class);
	public static final String DEFAULT_COLOR = "#6B5FA7"; // CDE Purple - organizational entity
	public static final String DEFAULT_ICON = "vaadin:folder-open";
	public static final String ENTITY_TITLE_PLURAL = "BAB Gateway Projects";
	public static final String ENTITY_TITLE_SINGULAR = "BAB Gateway Project";
	public static final String VIEW_NAME = "BAB Gateway Projects View";
	// Persisted field - IP address of Calimero server
	@Column (name = "ip_address", length = 45) // 45 chars supports IPv6
	@Pattern (
			regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{0,4}:){7}[0-9a-fA-F]{0,4}$|^$",
			message = "Invalid IP address format (IPv4 or IPv6)"
	)
	@AMetaData (
			displayName = "IP Address", required = false, readOnly = false, description = "Gateway IP address for BAB projects (IPv4 or IPv6)",
			hidden = false, maxLength = 45
	)
	private String ipAddress;
	// Persisted field - Authentication token for Calimero server
	@Column (name = "auth_token", length = 255)
	@AMetaData (
			displayName = "Auth Token", required = false, readOnly = false, description = "Bearer authentication token for Calimero HTTP API",
			hidden = false, maxLength = 255
	)
	private String authToken;
	// Transient field - HTTP client instance (not persisted, created on demand)
	@Transient
	private CClientProject httpClient;
	
	// Transient field - Last connection attempt timestamp for rate limiting
	@Transient
	private LocalDateTime lastConnectionAttempt = null;
	
	// Transient field - Connection attempt cooldown period (30 seconds)
	@Transient
	private static final long CONNECTION_COOLDOWN_SECONDS = 30;

	/** Default constructor for JPA. */
	protected CProject_Bab() {}

	public CProject_Bab(final String name, final CCompany company) {
		super(CProject_Bab.class, name, company);
		initializeDefaults();
	}

	/** Connect to Calimero server and initialize HTTP client. Creates or retrieves HTTP client via CClientProjectService.
	 * @return Connection result with status and details */
	public CConnectionResult connectToCalimero() {
		// Track connection attempt for rate limiting
		lastConnectionAttempt = LocalDateTime.now();
		
		LOGGER.info("🔌 Connecting project '{}' to Calimero server at {}", getName(), ipAddress);
		try {
			// Validate IP address
			if ((ipAddress == null) || ipAddress.isBlank()) {
				final String error = "IP address not set for project";
				LOGGER.error("❌ {}", error);
				return CConnectionResult.failure(error, "unknown", 0);
			}
			// Get or create HTTP client via service
			final CClientProjectService clientService = CSpringContext.getBean(CClientProjectService.class);
			setHttpClient(clientService.getOrCreateClient(this));
			// Attempt connection
			final CConnectionResult result = httpClient.connect();
			if (result.isSuccess()) {
				LOGGER.info("✅ Successfully connected project '{}' to Calimero", getName());
			} else {
				LOGGER.warn("⚠️ Connection failed for project '{}': {}", getName(), result.getMessage());
			}
			return result;
		} catch (final Exception e) {
			final String error = "Connection error: " + e.getMessage();
			LOGGER.error("❌ {}", error, e);
			return CConnectionResult.failure(error, ipAddress, 8077);
		}
	}

	/** Disconnect from Calimero server. */
	public void disconnectFromCalimero() {
		if (httpClient != null) {
			LOGGER.info("🔌 Disconnecting project '{}' from Calimero", getName());
			httpClient.disconnect();
			httpClient = null;
		}
	}

	public String getAuthToken() { return authToken; }

	/**
	 * Get HTTP client with lazy rate-limited auto-connect.
	 * <p>
	 * Design Pattern (2026-02-03):
	 * <ul>
	 * <li>If client exists and connected → return immediately</li>
	 * <li>If client null/disconnected AND IP configured → attempt connection</li>
	 * <li>Rate limit: Skip connection if last attempt was &lt; 30 seconds ago</li>
	 * <li>This prevents connection spam while allowing automatic recovery</li>
	 * </ul>
	 * 
	 * @return HTTP client (may be null if connection failed or rate-limited)
	 */
	public CClientProject getHttpClient() {
		// Fast path: Return existing connected client
		if (httpClient != null && httpClient.isConnected()) {
			return httpClient;
		}
		
		// Check if IP address configured
		if (ipAddress == null || ipAddress.isBlank()) {
			LOGGER.debug("⚙️ HTTP client not available - IP address not configured for project '{}'", getName());
			return null;
		}
		
		// Check rate limit (30 second cooldown)
		if (!shouldAttemptConnection()) {
			final long secondsSinceLastAttempt = java.time.Duration.between(lastConnectionAttempt, LocalDateTime.now()).getSeconds();
			LOGGER.debug("⏳ Connection attempt rate-limited for project '{}' - last attempt {}s ago (cooldown: {}s)", 
				getName(), secondsSinceLastAttempt, CONNECTION_COOLDOWN_SECONDS);
			return httpClient;  // Return existing client (may be null)
		}
		
		// Attempt lazy auto-connect
		LOGGER.info("🔄 Lazy auto-connect triggered for project '{}' - attempting connection to {}", getName(), ipAddress);
		final CConnectionResult result = connectToCalimero();
		
		if (result.isSuccess()) {
			LOGGER.info("✅ Lazy auto-connect SUCCESS for project '{}'", getName());
		} else {
			LOGGER.warn("⚠️ Lazy auto-connect FAILED for project '{}': {} (will retry after {}s cooldown)", 
				getName(), result.getMessage(), CONNECTION_COOLDOWN_SECONDS);
		}
		
		return httpClient;
	}
	
	/**
	 * Check if connection attempt should be made (rate limiting).
	 * <p>
	 * Returns true if:
	 * <ul>
	 * <li>This is the first connection attempt (lastConnectionAttempt is null)</li>
	 * <li>More than 30 seconds have passed since last attempt</li>
	 * </ul>
	 * 
	 * @return true if connection attempt allowed, false if rate-limited
	 */
	private boolean shouldAttemptConnection() {
		if (lastConnectionAttempt == null) {
			return true;  // First attempt - always allowed
		}
		
		final LocalDateTime now = LocalDateTime.now();
		final LocalDateTime nextAllowedAttempt = lastConnectionAttempt.plusSeconds(CONNECTION_COOLDOWN_SECONDS);
		
		return now.isAfter(nextAllowedAttempt);
	}

	// Getters
	public String getIpAddress() { return ipAddress; }

	private final void initializeDefaults() {
		ipAddress = "127.0.0.1"; // Default to localhost for testing
		authToken = "test-token-123"; // Default token for testing (matches Calimero config/http_server.json)
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	/** Check if project is connected to Calimero server.
	 * @return true if connected, false otherwise */
	public boolean isConnectedToCalimero() { return (httpClient != null) && httpClient.isConnected(); }

	/** Send "Hello" test message to Calimero server. Automatically connects if not already connected.
	 * @return Calimero server response */
	public CCalimeroResponse sayHelloToCalimero() {
		LOGGER.info("👋 Project '{}' saying Hello to Calimero", getName());
		try {
			// Ensure connected
			if ((httpClient == null) || !httpClient.isConnected()) {
				LOGGER.info("Not connected - establishing connection first");
				final CConnectionResult connectionResult = connectToCalimero();
				if (!connectionResult.isSuccess()) {
					return CCalimeroResponse.error("Not connected: " + connectionResult.getMessage());
				}
			}
			// Send hello
			final CCalimeroResponse response = httpClient.sayHello();
			if (response.isSuccess()) {
				LOGGER.info("✅ Hello response received from Calimero");
			} else {
				LOGGER.warn("⚠️ Hello request failed: {}", response.getErrorMessage());
			}
			return response;
		} catch (final Exception e) {
			final String error = "Hello error: " + e.getMessage();
			LOGGER.error("❌ {}", error, e);
			return CCalimeroResponse.error(error);
		}
	}

	public void setAuthToken(final String authToken) {
		this.authToken = authToken;
		// If auth token changes, invalidate existing client
		if (httpClient != null) {
			LOGGER.info("Auth token changed for project '{}' - disconnecting existing HTTP client", getName());
			httpClient.disconnect();
			setHttpClient(null);
		}
		updateLastModified();
	}

	public void setHttpClient(final CClientProject httpClient) {
		this.httpClient = httpClient;
		if (httpClient != null) {
			LOGGER.info("HTTP client set for project '{}'", getName());
		} else {
			LOGGER.info("HTTP client cleared for project '{}'", getName());
		}
	}

	// Setters
	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
		// If IP changes, invalidate existing client
		if (httpClient != null) {
			LOGGER.info("IP address changed for project '{}' - disconnecting existing HTTP client", getName());
			httpClient.disconnect();
			httpClient = null;
		}
		updateLastModified();
	}
}
