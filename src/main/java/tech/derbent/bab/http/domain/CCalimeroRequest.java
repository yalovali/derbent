package tech.derbent.bab.http.domain;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.derbent.api.utils.Check;

/** Request builder for Calimero server API. Follows Calimero's message-based JSON format. Calimero API Format: { "type": "question", "path":
 * "/api/v1/resource", "data": { "operation": "...", ...parameters } } @Profile("bab") */
public class CCalimeroRequest {
	/** Builder for CCalimeroRequest. */
	public static class Builder {
		private String type = "question";
		private String path = "/api/v1/system";
		private String operation;
		private final Map<String, Object> parameters = new HashMap<>();
		private final Map<String, String> headers = new HashMap<>();

		public CCalimeroRequest build() {
			// Fail-fast validation
			Check.notBlank(operation, "operation is required");
			Check.notBlank(type, "type is required");
			Check.notBlank(path, "path is required");
			return new CCalimeroRequest(this);
		}

		public Builder header(final String key, final String value) {
			Check.notBlank(key, "header key cannot be blank");
			this.headers.put(key, value);
			return this;
		}

		public Builder operation(final String operation1) {
			Check.notBlank(operation1, "operation cannot be blank");
			this.operation = operation1;
			return this;
		}

		public Builder parameter(final String key, final Object value) {
			Check.notBlank(key, "parameter key cannot be blank");
			this.parameters.put(key, value);
			return this;
		}

		public Builder parameters(final Map<String, Object> parameters1) {
			Check.notNull(parameters1, "parameters cannot be null");
			this.parameters.putAll(parameters1);
			return this;
		}

		public Builder path(final String path1) {
			Check.notBlank(path1, "path cannot be blank");
			this.path = path1;
			return this;
		}

		public Builder type(final String type1) {
			Check.notBlank(type1, "type cannot be blank");
			this.type = type1;
			return this;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CCalimeroRequest.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static Builder builder() {
		return new Builder();
	}

	private final String type;
	private final String path;
	private final String operation;
	private final Map<String, Object> parameters;
	private final Map<String, String> headers;

	private CCalimeroRequest(final Builder builder) {
		this.type = builder.type;
		this.path = builder.path;
		this.operation = builder.operation;
		this.parameters = builder.parameters;
		this.headers = builder.headers;
	}

	public Map<String, String> getHeaders() { return headers; }

	public String getOperation() { return operation; }

	public Map<String, Object> getParameters() { return parameters; }

	public String getPath() { return path; }

	public String getType() { return type; }

	/** Convert request to JSON string for Calimero API. Format: {"kind": "question", "type": "system", "data": {"operation": "info", ...}}
	 * @return JSON representation */
	public String toJson() {
		try {
			final Map<String, Object> requestMap = new HashMap<>();
			requestMap.put("kind", "question"); // Always "question" for client requests
			requestMap.put("type", type); // Resource type: system, node, disk, etc.
			final Map<String, Object> data = new HashMap<>();
			data.put("operation", operation); // Operation: info, status, etc.
			data.putAll(parameters); // Additional parameters
			requestMap.put("data", data);
			final String json = MAPPER.writeValueAsString(requestMap);
			LOGGER.debug("📤 Request JSON: {}", json);
			return json;
		} catch (final JsonProcessingException e) {
			LOGGER.error("❌ Failed to serialize request: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to serialize request", e);
		}
	}

	@Override
	public String toString() {
		return String.format("CCalimeroRequest{type='%s', path='%s', operation='%s', params=%d}", type, path, operation, parameters.size());
	}
}
