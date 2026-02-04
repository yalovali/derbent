package tech.derbent.bab.dashboard.dashboardproject_bab.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CDTOWebServiceEndpoint - API endpoint metadata from webservice discovery.
 * <p>
 * Represents a single HTTP API endpoint with its operations and parameters.
 * Used for API introspection and documentation.
 * <p>
 * JSON structure from Calimero:
 * <pre>
 * {
 *   "type": "systemservices",
 *   "action": "list",
 *   "description": "List all systemd services",
 *   "parameters": {
 *     "activeOnly": "boolean (optional)",
 *     "runningOnly": "boolean (optional)"
 *   },
 *   "endpoint": "/api/v1/message"
 * }
 * </pre>
 * <p>
 * Thread Safety: Immutable after construction.
 */
public class CDTOWebServiceEndpoint extends CObject {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDTOWebServiceEndpoint.class);
	
	/**
	 * Create from JSON object.
	 * @param json JSON object from Calimero
	 * @return New instance
	 */
	public static CDTOWebServiceEndpoint createFromJson(final JsonObject json) {
		final CDTOWebServiceEndpoint endpoint = new CDTOWebServiceEndpoint();
		endpoint.fromJson(json);
		return endpoint;
	}
	
	private String type = "";
	private String action = "";
	private String description = "";
	private JsonObject parameters = new JsonObject();
	private String endpoint = "";
	
	public CDTOWebServiceEndpoint() {
		// Default constructor
	}
	
	@Override
	protected void fromJson(final JsonObject json) {
		try {
			if (json.has("type")) {
				type = json.get("type").getAsString();
			}
			if (json.has("action")) {
				action = json.get("action").getAsString();
			}
			if (json.has("description")) {
				description = json.get("description").getAsString();
			}
			if (json.has("parameters") && json.get("parameters").isJsonObject()) {
				parameters = json.getAsJsonObject("parameters");
			}
			if (json.has("endpoint")) {
				endpoint = json.get("endpoint").getAsString();
			}
		} catch (final Exception e) {
			LOGGER.error("Error parsing CDTOWebServiceEndpoint from JSON: {}", e.getMessage());
		}
	}
	
	@Override
	protected String toJson() {
		return "{}"; // Read-only from server
	}
	
	// Getters
	public String getType() { return type; }
	public String getAction() { return action; }
	public String getDescription() { return description; }
	public JsonObject getParameters() { return parameters; }
	public String getEndpoint() { return endpoint; }
	
	/**
	 * Get full operation name (type.action).
	 * @return Formatted operation name (e.g., "systemservices.list")
	 */
	public String getFullOperation() {
		return String.format("%s.%s", type, action);
	}
	
	/**
	 * Get formatted parameter list.
	 * @return Comma-separated parameter names
	 */
	public String getParameterList() {
		if (parameters == null || parameters.size() == 0) {
			return "none";
		}
		final StringBuilder sb = new StringBuilder();
		for (final String key : parameters.keySet()) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(key);
		}
		return sb.toString();
	}
	
	/**
	 * Check if endpoint has required parameters.
	 * @return true if any parameter is marked as "required"
	 */
	public boolean hasRequiredParameters() {
		if (parameters == null || parameters.size() == 0) {
			return false;
		}
		for (final String key : parameters.keySet()) {
			final String value = parameters.get(key).getAsString();
			if (value != null && value.toLowerCase().contains("required")) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("CDTOWebServiceEndpoint{type='%s', action='%s', description='%s'}", 
			type, action, description);
	}
}
