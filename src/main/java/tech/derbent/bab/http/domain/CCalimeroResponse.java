package tech.derbent.bab.http.domain;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Response parser for Calimero server API.
 * Parses Calimero's message-based JSON response format.
 * 
 * Calimero API Response Format:
 * {
 *   "type": "reply",
 *   "path": "/api/v1/resource",
 *   "status": 0,  // 0=SUCCESS, 1=ERROR, 2=INVALID_REQUEST, etc.
 *   "data": {...}
 * }
 * 
 * @Profile("bab")
 */
public class CCalimeroResponse {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCalimeroResponse.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final String type;
	private final String path;
	private final int status;
	private final Map<String, Object> data;
	private final String errorMessage;

	private CCalimeroResponse(final String type, final String path, final int status, 
			final Map<String, Object> data, final String errorMessage) {
		this.type = type;
		this.path = path;
		this.status = status;
		this.data = data != null ? data : new HashMap<>();
		this.errorMessage = errorMessage;
	}

	/**
	 * Parse JSON response from Calimero.
	 * @param json JSON string
	 * @return Parsed response
	 */
	@SuppressWarnings("unchecked")
	public static CCalimeroResponse fromJson(final String json) {
		if (json == null || json.isBlank()) {
			LOGGER.error("❌ Cannot parse null or empty JSON response");
			return error("Empty response from server");
		}

		try {
			LOGGER.debug("📥 Parsing response JSON: {}", json);
			final Map<String, Object> responseMap = MAPPER.readValue(json, Map.class);

			final String type = (String) responseMap.get("type");
			final String path = (String) responseMap.get("path");
			final Object statusObj = responseMap.get("status");
			final int status = statusObj != null ? ((Number) statusObj).intValue() : 0;
			final Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
			final String errorMessage = (String) responseMap.get("error");

			final CCalimeroResponse response = new CCalimeroResponse(type, path, status, data, errorMessage);
			
			if (response.isSuccess()) {
				LOGGER.debug("✅ Response parsed successfully: status={}", status);
			} else {
				LOGGER.warn("⚠️ Error response: status={}, message={}", status, errorMessage);
			}
			
			return response;

		} catch (final JsonProcessingException e) {
			LOGGER.error("❌ JSON parsing failed: {}", e.getMessage(), e);
			return error("Failed to parse response: " + e.getMessage());
		} catch (final ClassCastException e) {
			LOGGER.error("❌ Invalid response format: {}", e.getMessage(), e);
			return error("Invalid response format: " + e.getMessage());
		}
	}

	/**
	 * Create error response.
	 * @param errorMessage Error message
	 * @return Error response
	 */
	public static CCalimeroResponse error(final String errorMessage) {
		LOGGER.debug("Creating error response: {}", errorMessage);
		return new CCalimeroResponse("error", null, 1, null, errorMessage);
	}

	/**
	 * Create success response.
	 * @param data Response data
	 * @return Success response
	 */
	public static CCalimeroResponse success(final Map<String, Object> data) {
		LOGGER.debug("Creating success response with data: {}", data);
		return new CCalimeroResponse("reply", null, 0, data, null);
	}

	public boolean isSuccess() {
		return status == 0;
	}

	public boolean isError() {
		return status != 0;
	}

	public String getType() {
		return type;
	}

	public String getPath() {
		return path;
	}

	public int getStatus() {
		return status;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public String getErrorMessage() {
		return errorMessage != null ? errorMessage : "Unknown error";
	}

	/**
	 * Get data field as specific type with fail-fast validation.
	 * @param key          Field key
	 * @param defaultValue Default value if not found
	 * @return Field value
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDataField(final String key, final T defaultValue) {
		if (key == null || key.isBlank()) {
			LOGGER.warn("⚠️ Attempted to get data field with null/empty key");
			return defaultValue;
		}
		
		final Object value = data.get(key);
		if (value == null) {
			LOGGER.debug("Data field '{}' not found, using default", key);
			return defaultValue;
		}
		
		try {
			return (T) value;
		} catch (final ClassCastException e) {
			LOGGER.warn("⚠️ Failed to cast data field '{}' to expected type: {}", key, e.getMessage());
			return defaultValue;
		}
	}

	@Override
	public String toString() {
		return String.format("CCalimeroResponse{type='%s', path='%s', status=%d, error='%s', dataSize=%d}", 
			type, path, status, errorMessage, data.size());
	}
}
