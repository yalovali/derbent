package tech.derbent.bab.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.clientproject.service.CClientProjectService;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.bab.http.domain.CConnectionResult;

/**
 * CProject_Bab - BAB Gateway-specific project with IP address and HTTP client support.
 * 
 * Features:
 * - IP address field (persisted) for Calimero server location
 * - HTTP client field (transient) for Calimero communication
 * - Connection management methods (connectToCalimero, sayHello)
 * 
 * Layer: Domain (MVC)
 * Active when: 'bab' profile is active
 * Calimero Server: ~/git/calimero (HTTP API port 8077)
 */
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

	/** Default constructor for JPA. */
	protected CProject_Bab() {}

	public CProject_Bab(final String name, final CCompany company) {
		super(CProject_Bab.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		ipAddress = "127.0.0.1"; // Default to localhost for testing
		authToken = "test-token-123"; // Default token for testing (matches Calimero config/http_server.json)
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	// Getters
	public String getIpAddress() { 
		return ipAddress; 
	}
	
	public String getAuthToken() {
		return authToken;
	}
	
	public CClientProject getHttpClient() {
		return httpClient;
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
	
	public void setHttpClient(final CClientProject httpClient) {
		this.httpClient = httpClient;
	}
	
	public void setAuthToken(final String authToken) {
		this.authToken = authToken;
		// If auth token changes, invalidate existing client
		if (httpClient != null) {
			LOGGER.info("Auth token changed for project '{}' - disconnecting existing HTTP client", getName());
			httpClient.disconnect();
			httpClient = null;
		}
		updateLastModified();
	}
	
	/**
	 * Connect to Calimero server and initialize HTTP client.
	 * Creates or retrieves HTTP client via CClientProjectService.
	 * 
	 * @return Connection result with status and details
	 */
	public CConnectionResult connectToCalimero() {
		LOGGER.info("🔌 Connecting project '{}' to Calimero server at {}", getName(), ipAddress);
		
		try {
			// Validate IP address
			if (ipAddress == null || ipAddress.isBlank()) {
				final String error = "IP address not set for project";
				LOGGER.error("❌ {}", error);
				return CConnectionResult.failure(error, "unknown", 0);
			}
			
			// Get or create HTTP client via service
			final CClientProjectService clientService = CSpringContext.getBean(CClientProjectService.class);
			httpClient = clientService.getOrCreateClient(this);
			
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
	
	/**
	 * Send "Hello" test message to Calimero server.
	 * Automatically connects if not already connected.
	 * 
	 * @return Calimero server response
	 */
	public CCalimeroResponse sayHelloToCalimero() {
		LOGGER.info("👋 Project '{}' saying Hello to Calimero", getName());
		
		try {
			// Ensure connected
			if (httpClient == null || !httpClient.isConnected()) {
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
	
	/**
	 * Disconnect from Calimero server.
	 */
	public void disconnectFromCalimero() {
		if (httpClient != null) {
			LOGGER.info("🔌 Disconnecting project '{}' from Calimero", getName());
			httpClient.disconnect();
			httpClient = null;
		}
	}
	
	/**
	 * Check if project is connected to Calimero server.
	 * 
	 * @return true if connected, false otherwise
	 */
	public boolean isConnectedToCalimero() {
		return httpClient != null && httpClient.isConnected();
	}
}
