# BAB HTTP Client - Complete Source Code

All Java source files ready for copy-paste implementation.

**Version**: 1.0  
**Date**: 2026-01-30

---

## File 1: CCalimeroRequest.java

**Location**: `src/main/java/tech/derbent/bab/http/domain/CCalimeroRequest.java`

```java
package tech.derbent.bab.http.domain;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Request builder for Calimero server API.
 * Follows Calimero's JSON request format.
 */
public class CCalimeroRequest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final String type;
	private final String operation;
	private final Map<String, Object> parameters;
	private final Map<String, String> headers;

	private CCalimeroRequest(final Builder builder) {
		this.type = builder.type;
		this.operation = builder.operation;
		this.parameters = builder.parameters;
		this.headers = builder.headers;
	}

	/**
	 * Convert request to JSON string.
	 * @return JSON representation
	 */
	public String toJson() {
		try {
			final Map<String, Object> requestMap = new HashMap<>();
			requestMap.put("type", type);

			final Map<String, Object> data = new HashMap<>();
			data.put("operation", operation);
			data.putAll(parameters);

			requestMap.put("data", data);

			return MAPPER.writeValueAsString(requestMap);

		} catch (final JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize request", e);
		}
	}

	public String getType() { return type; }

	public String getOperation() { return operation; }

	public Map<String, Object> getParameters() { return parameters; }

	public Map<String, String> getHeaders() { return headers; }

	/**
	 * Builder for CCalimeroRequest.
	 */
	public static class Builder {

		private String type = "system";
		private String operation;
		private final Map<String, Object> parameters = new HashMap<>();
		private final Map<String, String> headers = new HashMap<>();

		public Builder type(final String type) {
			this.type = type;
			return this;
		}

		public Builder operation(final String operation) {
			this.operation = operation;
			return this;
		}

		public Builder parameter(final String key, final Object value) {
			this.parameters.put(key, value);
			return this;
		}

		public Builder parameters(final Map<String, Object> parameters) {
			this.parameters.putAll(parameters);
			return this;
		}

		public Builder header(final String key, final String value) {
			this.headers.put(key, value);
			return this;
		}

		public CCalimeroRequest build() {
			if (operation == null) {
				throw new IllegalArgumentException("operation required");
			}
			return new CCalimeroRequest(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}
}
```

---

## File 2: CCalimeroResponse.java

**Location**: `src/main/java/tech/derbent/bab/http/domain/CCalimeroResponse.java`

```java
package tech.derbent.bab.http.domain;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Response parser for Calimero server API.
 * Parses Calimero's JSON response format.
 */
public class CCalimeroResponse {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final String type;
	private final int status;
	private final Map<String, Object> data;
	private final String message;

	private CCalimeroResponse(final String type, final int status, final Map<String, Object> data,
			final String message) {
		this.type = type;
		this.status = status;
		this.data = data != null ? data : new HashMap<>();
		this.message = message;
	}

	/**
	 * Parse JSON response from Calimero.
	 * @param json JSON string
	 * @return Parsed response
	 */
	@SuppressWarnings("unchecked")
	public static CCalimeroResponse fromJson(final String json) {
		try {
			final Map<String, Object> responseMap = MAPPER.readValue(json, Map.class);

			final String type = (String) responseMap.get("type");
			final int status = (int) responseMap.getOrDefault("status", 0);
			final Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
			final String message = (String) responseMap.get("message");

			return new CCalimeroResponse(type, status, data, message);

		} catch (final JsonProcessingException e) {
			return error("Failed to parse response: " + e.getMessage());
		}
	}

	/**
	 * Create error response.
	 * @param errorMessage Error message
	 * @return Error response
	 */
	public static CCalimeroResponse error(final String errorMessage) {
		return new CCalimeroResponse("error", 1, null, errorMessage);
	}

	/**
	 * Create success response.
	 * @param data Response data
	 * @return Success response
	 */
	public static CCalimeroResponse success(final Map<String, Object> data) {
		return new CCalimeroResponse("reply", 0, data, "Success");
	}

	public boolean isSuccess() {
		return status == 0;
	}

	public boolean isError() {
		return status != 0;
	}

	public String getType() { return type; }

	public int getStatus() { return status; }

	public Map<String, Object> getData() { return data; }

	public String getMessage() { return message; }

	/**
	 * Get data field as specific type.
	 * @param key          Field key
	 * @param defaultValue Default value if not found
	 * @return Field value
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDataField(final String key, final T defaultValue) {
		final Object value = data.get(key);
		if (value == null) {
			return defaultValue;
		}
		return (T) value;
	}

	@Override
	public String toString() {
		return String.format("CCalimeroResponse{type='%s', status=%d, message='%s', data=%s}", type, status, message,
				data);
	}
}
```

---

## File 3: CHttpService.java

**Location**: `src/main/java/tech/derbent/bab/http/service/CHttpService.java`

```java
package tech.derbent.bab.http.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tech.derbent.bab.http.domain.CHealthStatus;
import tech.derbent.bab.http.domain.CHttpResponse;

/**
 * Core HTTP communication service using Spring RestTemplate.
 * Provides synchronous and asynchronous HTTP operations.
 */
@Service
@Profile("bab")
public class CHttpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CHttpService.class);
	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

	private final RestTemplate restTemplate;

	public CHttpService(final RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.setConnectTimeout(DEFAULT_TIMEOUT).setReadTimeout(DEFAULT_TIMEOUT)
				.build();
	}

	/**
	 * Send HTTP GET request.
	 * @param url     Target URL
	 * @param headers Request headers
	 * @return HTTP response
	 */
	public CHttpResponse sendGet(final String url, final Map<String, String> headers) {
		LOGGER.debug("🔵 GET {}", url);

		try {
			final HttpHeaders httpHeaders = createHeaders(headers);
			final HttpEntity<String> entity = new HttpEntity<>(httpHeaders);

			final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

			return CHttpResponse.success(response.getStatusCode().value(), response.getBody(),
					response.getHeaders().toSingleValueMap());

		} catch (final RestClientException e) {
			LOGGER.error("❌ GET request failed: {}", e.getMessage());
			return CHttpResponse.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "Request failed: " + e.getMessage());
		}
	}

	/**
	 * Send HTTP POST request.
	 * @param url     Target URL
	 * @param body    Request body
	 * @param headers Request headers
	 * @return HTTP response
	 */
	public CHttpResponse sendPost(final String url, final String body, final Map<String, String> headers) {
		LOGGER.debug("🟢 POST {} | Body: {}", url, body);

		try {
			final HttpHeaders httpHeaders = createHeaders(headers);
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);

			final HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);

			final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

			LOGGER.debug("✅ POST response: {} | {}", response.getStatusCode(), response.getBody());

			return CHttpResponse.success(response.getStatusCode().value(), response.getBody(),
					response.getHeaders().toSingleValueMap());

		} catch (final RestClientException e) {
			LOGGER.error("❌ POST request failed: {}", e.getMessage());
			return CHttpResponse.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "Request failed: " + e.getMessage());
		}
	}

	/**
	 * Send asynchronous HTTP request.
	 * @param url     Target URL
	 * @param method  HTTP method
	 * @param body    Request body (optional)
	 * @param headers Request headers
	 * @return CompletableFuture with response
	 */
	public CompletableFuture<CHttpResponse> sendAsync(final String url, final HttpMethod method, final String body,
			final Map<String, String> headers) {

		return CompletableFuture.supplyAsync(() -> {
			if (method == HttpMethod.GET) {
				return sendGet(url, headers);
			} else if (method == HttpMethod.POST) {
				return sendPost(url, body, headers);
			} else {
				throw new UnsupportedOperationException("Method not supported: " + method);
			}
		});
	}

	/**
	 * Health check endpoint.
	 * @param healthUrl Health check URL
	 * @return HTTP response
	 */
	public CHttpResponse healthCheck(final String healthUrl) {
		LOGGER.debug("💓 Health check: {}", healthUrl);

		try {
			final ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);

			final boolean healthy = response.getStatusCode().is2xxSuccessful();

			LOGGER.debug("{} Health check result: {}", healthy ? "✅" : "❌", response.getStatusCode());

			return CHttpResponse.success(response.getStatusCode().value(), response.getBody(),
					response.getHeaders().toSingleValueMap());

		} catch (final RestClientException e) {
			LOGGER.warn("⚠️ Health check failed: {}", e.getMessage());
			return CHttpResponse.error(HttpStatus.SERVICE_UNAVAILABLE.value(),
					"Health check failed: " + e.getMessage());
		}
	}

	/**
	 * Detailed health status check.
	 * @param healthUrl Health check URL
	 * @return Health status object
	 */
	public CHealthStatus checkHealth(final String healthUrl) {
		final CHttpResponse response = healthCheck(healthUrl);

		return CHealthStatus.builder().healthy(response.isSuccess()).statusCode(response.getStatusCode())
				.message(response.isSuccess() ? "Server is healthy" : response.getErrorMessage()).responseTime(0) // TODO:
																													// Measure
																													// actual
																													// response
																													// time
				.build();
	}

	/**
	 * Create HTTP headers from map.
	 * @param headers Header map
	 * @return HttpHeaders object
	 */
	private HttpHeaders createHeaders(final Map<String, String> headers) {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.setAcceptCharset(java.util.List.of(StandardCharsets.UTF_8));

		if (headers != null) {
			headers.forEach(httpHeaders::set);
		}

		return httpHeaders;
	}

	/**
	 * Get RestTemplate instance for advanced usage.
	 * @return RestTemplate instance
	 */
	public RestTemplate getRestTemplate() {
		return restTemplate;
	}
}
```

---

## File 4: CClientProject.java

**Location**: `src/main/java/tech/derbent/bab/http/clientproject/domain/CClientProject.java`

See HTTP_CLIENT_ARCHITECTURE.md, Section 2 "CClientProject" for complete implementation (700+ lines).

**Note**: Due to length, refer to architecture document for full source code.

---

## File 5: CClientProjectService.java

**Location**: `src/main/java/tech/derbent/bab/http/clientproject/service/CClientProjectService.java`

See HTTP_CLIENT_ARCHITECTURE.md, Section 3 "CClientProjectService" for complete implementation (250+ lines).

**Note**: Due to length, refer to architecture document for full source code.

---

## CProject Modifications

**Location**: `src/main/java/tech/derbent/api/projects/domain/CProject.java`

Add these members at the end of the class:

```java
// Add these imports at top
import jakarta.persistence.Transient;
// Note: Use fully-qualified names for BAB classes to avoid cross-profile dependencies

// Add these fields after existing fields (around line 65)

/** IP address for Calimero server communication (not persisted) */
@Transient
@AMetaData(displayName = "Calimero IP Address", required = false, description = "IP address of Calimero server for this project", hidden = false)
private String ipAddress = "127.0.0.1"; // Default for testing

/** HTTP client instance for Calimero communication (not persisted) */
@Transient
private tech.derbent.bab.http.clientproject.domain.CClientProject httpClient;

// Add these methods at the end of the class (before closing brace)

/**
 * Initialize HTTP client and connect to Calimero server.
 * Called after project activation.
 */
public void connectToCalimero() {
	if (httpClient == null) {
		final tech.derbent.bab.http.clientproject.service.CClientProjectService service = tech.derbent.api.utils.CSpringContext
				.getBean(tech.derbent.bab.http.clientproject.service.CClientProjectService.class);
		httpClient = service.getOrCreateClient(this);
	}
	httpClient.connect();
}

/**
 * Send hello message to Calimero server for testing.
 * @return Response from Calimero server
 */
public tech.derbent.bab.http.domain.CCalimeroResponse sayHelloToCalimero() {
	if (httpClient == null) {
		connectToCalimero();
	}
	return httpClient.sayHello();
}

/**
 * Get or initialize HTTP client instance.
 * @return HTTP client for this project
 */
public tech.derbent.bab.http.clientproject.domain.CClientProject getHttpClient() {
	if (httpClient == null) {
		connectToCalimero();
	}
	return httpClient;
}

/**
 * Get IP address from project settings or return default.
 * @return IP address for Calimero server
 */
public String getIpAddress() {
	if (ipAddress == null || ipAddress.isBlank()) {
		// Try to load from project settings
		final String settingsIp = getSettingValue("calimero.ip.address");
		if (settingsIp != null && !settingsIp.isBlank()) {
			ipAddress = settingsIp;
		} else {
			ipAddress = "127.0.0.1"; // Default
		}
	}
	return ipAddress;
}

/**
 * Set IP address and save to project settings.
 * @param ipAddress New IP address for Calimero server
 */
public void setIpAddress(final String ipAddress) {
	this.ipAddress = ipAddress;
	// Save to project settings for persistence
	setSettingValue("calimero.ip.address", ipAddress);
}
```

---

## Implementation Checklist

- [ ] Create directory: `src/main/java/tech/derbent/bab/http/domain`
- [ ] Create directory: `src/main/java/tech/derbent/bab/http/service`  
- [ ] Create: `CHttpResponse.java` (from implement-http-client.sh)
- [ ] Create: `CConnectionResult.java` (from implement-http-client.sh)
- [ ] Create: `CHealthStatus.java` (from implement-http-client.sh)
- [ ] Create: `CCalimeroRequest.java` (from this document)
- [ ] Create: `CCalimeroResponse.java` (from this document)
- [ ] Create: `CHttpService.java` (from this document)
- [ ] Create: `CClientProject.java` (from HTTP_CLIENT_ARCHITECTURE.md)
- [ ] Create: `CClientProjectService.java` (from HTTP_CLIENT_ARCHITECTURE.md)
- [ ] Modify: `CProject.java` (add fields and methods from this document)
- [ ] Compile: `mvn clean compile -Pagents -DskipTests`
- [ ] Fix any errors
- [ ] Full build: `mvn clean verify -Pagents`

---

**End of Source Code Document**

**References**:
- Complete CClientProject implementation: HTTP_CLIENT_ARCHITECTURE.md, Section 2
- Complete CClientProjectService implementation: HTTP_CLIENT_ARCHITECTURE.md, Section 3
- Implementation guide: HTTP_CLIENT_IMPLEMENTATION.md
- Summary: IMPLEMENTATION_SUMMARY.md
