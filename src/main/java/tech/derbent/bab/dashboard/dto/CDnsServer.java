package tech.derbent.bab.dashboard.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CDnsServer - DNS server configuration model from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses.
 * Represents a DNS server configuration.
 * <p>
 * JSON structure from Calimero (resolvectl format):
 * <pre>
 * {
 *   "interface": "eth0",
 *   "servers": ["8.8.8.8", "8.8.4.4"],
 *   "domains": ["example.com"]
 * }
 * </pre>
 * 
 * Or fallback format (resolv.conf):
 * <pre>
 * {
 *   "servers": ["8.8.8.8", "8.8.4.4"],
 *   "source": "resolv.conf"
 * }
 * </pre>
 */
public class CDnsServer extends CObject {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDnsServer.class);
	
	public static CDnsServer createFromJson(final JsonObject json) {
		final CDnsServer dnsServer = new CDnsServer();
		dnsServer.fromJson(json);
		return dnsServer;
	}
	
	private String interfaceName = "";
	private String server = "";
	private String domain = "";
	private String source = ""; // "resolvectl" or "resolv.conf"
	private Boolean isPrimary = false;
	
	public CDnsServer() {
		// Default constructor
	}
	
	/**
	 * Constructor for simple server entry.
	 * @param server DNS server IP address
	 */
	public CDnsServer(final String server) {
		this.server = server;
		this.source = "resolv.conf";
	}
	
	/**
	 * Constructor for interface-specific DNS entry.
	 * @param interfaceName Network interface name
	 * @param server DNS server IP address
	 * @param domain DNS domain
	 */
	public CDnsServer(final String interfaceName, final String server, final String domain) {
		this.interfaceName = interfaceName;
		this.server = server;
		this.domain = domain;
		this.source = "resolvectl";
	}
	
	@Override
	protected void fromJson(final JsonObject json) {
		try {
			if (json.has("interface")) {
				interfaceName = json.get("interface").getAsString();
			}
			if (json.has("server")) {
				server = json.get("server").getAsString();
			}
			if (json.has("domain")) {
				domain = json.get("domain").getAsString();
			}
			if (json.has("source")) {
				source = json.get("source").getAsString();
			}
			if (json.has("isPrimary")) {
				isPrimary = json.get("isPrimary").getAsBoolean();
			}
		} catch (final Exception e) {
			LOGGER.error("Error parsing CDnsServer from JSON: {}", e.getMessage());
		}
	}
	
	public String getInterfaceName() { return interfaceName; }
	public String getServer() { return server; }
	public String getDomain() { return domain; }
	public String getSource() { return source; }
	public Boolean getIsPrimary() { return isPrimary; }
	
	public void setInterfaceName(final String interfaceName) { this.interfaceName = interfaceName; }
	public void setServer(final String server) { this.server = server; }
	public void setDomain(final String domain) { this.domain = domain; }
	public void setSource(final String source) { this.source = source; }
	public void setIsPrimary(final Boolean isPrimary) { this.isPrimary = isPrimary; }
	
	/**
	 * Get display name for DNS server.
	 * @return Formatted display name
	 */
	public String getDisplayName() {
		if (!interfaceName.isEmpty()) {
			return server + " (" + interfaceName + ")";
		}
		return server;
	}
	
	/**
	 * Get priority display.
	 * @return Priority string (Primary, Secondary, etc.)
	 */
	public String getPriorityDisplay() {
		if (isPrimary != null && isPrimary) {
			return "Primary";
		}
		return "Secondary";
	}
	
	/**
	 * Check if this is a valid DNS server.
	 * @return true if server IP is not empty
	 */
	public boolean isValid() {
		return server != null && !server.trim().isEmpty() && !"0.0.0.0".equals(server);
	}
	
	@Override
	protected String toJson() {
		final JsonObject json = new JsonObject();
		json.addProperty("interface", interfaceName);
		json.addProperty("server", server);
		json.addProperty("domain", domain);
		json.addProperty("source", source);
		json.addProperty("isPrimary", isPrimary);
		return json.toString();
	}
	
	@Override
	public String toString() {
		return "CDnsServer{" +
			"interface='" + interfaceName + '\'' +
			", server='" + server + '\'' +
			", domain='" + domain + '\'' +
			", source='" + source + '\'' +
			", isPrimary=" + isPrimary +
			'}';
	}
}