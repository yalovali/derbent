package tech.derbent.bab.http.service;

import java.time.Duration;
import java.util.HashMap;
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

/** Core HTTP communication service using Spring RestTemplate. Provides synchronous and asynchronous HTTP operations. */
@Service
@Profile ("bab")
public class CHttpService {

	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
	private static final Logger LOGGER = LoggerFactory.getLogger(CHttpService.class);
	private final RestTemplate restTemplate;

	public CHttpService(final RestTemplateBuilder restTemplateBuilder) {
		restTemplate = restTemplateBuilder.setConnectTimeout(DEFAULT_TIMEOUT).setReadTimeout(DEFAULT_TIMEOUT).build();
	}

	/** Detailed health status check.
	 * @param healthUrl Health check URL
	 * @return Health status object */
	public CHealthStatus checkHealth(final String healthUrl) {
		final long startTime = System.currentTimeMillis();
		final CHttpResponse response = healthCheck(healthUrl);
		final long responseTime = System.currentTimeMillis() - startTime;
		return CHealthStatus.builder().healthy(response.isSuccess()).statusCode(response.getStatusCode())
				.message(response.isSuccess() ? "Server is healthy" : response.getErrorMessage()).responseTime(responseTime).build();
	}

	/** Create HTTP headers from map.
	 * @param headers Header map
	 * @return HttpHeaders object */
	private HttpHeaders createHeaders(final Map<String, String> headers) {
		final HttpHeaders httpHeaders = new HttpHeaders();
		if (headers != null) {
			headers.forEach(httpHeaders::set);
		}
		return httpHeaders;
	}

	/** Health check endpoint.
	 * @param healthUrl Health check URL
	 * @return HTTP response */
	public CHttpResponse healthCheck(final String healthUrl) {
		LOGGER.info("💓 Health check: {}", healthUrl);
		try {
			final ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
			final boolean healthy = response.getStatusCode().is2xxSuccessful();
			LOGGER.info("{} Health check result: {}", healthy ? "✅" : "❌", response.getStatusCode());
			return CHttpResponse.success(response.getStatusCode().value(), response.getBody(), response.getHeaders().toSingleValueMap());
		} catch (final org.springframework.web.client.ResourceAccessException e) {
			// Connection errors (Connection refused, timeout, etc.)
			LOGGER.warn("⚠️ Health check connection failed: {}", e.getMessage());
			return CHttpResponse.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "Connection failed: " + e.getMessage());
		} catch (final RestClientException e) {
			// Other REST client errors
			LOGGER.warn("⚠️ Health check failed: {}", e.getMessage());
			return CHttpResponse.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "Health check failed: " + e.getMessage());
		}
	}

	/** Send asynchronous HTTP request.
	 * @param url     Target URL
	 * @param method  HTTP method
	 * @param body    Request body (optional)
	 * @param headers Request headers
	 * @return CompletableFuture with response */
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

	/** Send GET request with default empty headers.
	 * @param url Target URL
	 * @return HTTP response */
	public CHttpResponse sendGet(final String url) {
		return sendGet(url, new HashMap<>());
	}

	/** Send HTTP GET request.
	 * @param url     Target URL
	 * @param headers Request headers
	 * @return HTTP response */
	public CHttpResponse sendGet(final String url, final Map<String, String> headers) {
		LOGGER.info("🔵 GET {} | Headers: {}", url, headers);
		try {
			final HttpHeaders httpHeaders = createHeaders(headers);
			LOGGER.info("🔐 Final HTTP headers for GET request: {}", httpHeaders);
			final HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
			final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			LOGGER.info("✅ GET response: {} | Body: {}", response.getStatusCode(), response.getBody());
			return CHttpResponse.success(response.getStatusCode().value(), response.getBody(), response.getHeaders().toSingleValueMap());
		} catch (final org.springframework.web.client.HttpClientErrorException e) {
			// HTTP 4xx errors (401 Unauthorized, 403 Forbidden, etc.)
			LOGGER.error("❌ GET request failed with HTTP client error: {} {} | Response body: {} | Headers sent: {}", 
				e.getStatusCode(), e.getStatusText(), e.getResponseBodyAsString(), headers);
			
			if (e.getStatusCode().value() == 401) {
				return CHttpResponse.error(401, "Authentication failed: Invalid or missing authorization token");
			} else if (e.getStatusCode().value() == 403) {
				return CHttpResponse.error(403, "Authorization failed: Access denied for this resource");
			}
			return CHttpResponse.error(e.getStatusCode().value(), "Request failed: " + e.getMessage());
		} catch (final org.springframework.web.client.HttpServerErrorException e) {
			// HTTP 5xx errors (500 Internal Server Error, 503 Service Unavailable, etc.)
			LOGGER.error("❌ GET request failed with HTTP server error: {} {} | Response body: {} | Headers sent: {}", 
				e.getStatusCode(), e.getStatusText(), e.getResponseBodyAsString(), headers);
			return CHttpResponse.error(e.getStatusCode().value(), "Server error: " + e.getMessage());
		} catch (final org.springframework.web.client.ResourceAccessException e) {
			// Connection errors (Connection refused, timeout, etc.)
			LOGGER.error("❌ GET request failed with connection error: {} | Headers sent: {}", e.getMessage(), headers);
			return CHttpResponse.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "Connection failed: " + e.getMessage());
		} catch (final RestClientException e) {
			// Other REST client errors
			LOGGER.error("❌ GET request failed with REST client error: {} | Headers sent: {}", e.getMessage(), headers);
			return CHttpResponse.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "Request failed: " + e.getMessage());
		}
	}

	/** Send POST request with default empty headers.
	 * @param url  Target URL
	 * @param body Request body
	 * @return HTTP response */
	public CHttpResponse sendPost(final String url, final String body) {
		return sendPost(url, body, new HashMap<>());
	}

	/** Send HTTP POST request.
	 * @param url     Target URL
	 * @param body    Request body
	 * @param headers Request headers
	 * @return HTTP response */
	public CHttpResponse sendPost(final String url, final String body, final Map<String, String> headers) {
		LOGGER.info("🟢 POST {} | Body length: {} chars | Headers: {}", url, body != null ? body.length() : 0, headers);
		LOGGER.debug("🟢 POST {} | Full body: {}", url, body);
		try {
			final HttpHeaders httpHeaders = createHeaders(headers);
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			LOGGER.info("🔐 Final HTTP headers for POST request: {}", httpHeaders);
			final HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);
			final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			LOGGER.info("✅ POST response: {} | Body: {}", response.getStatusCode(), response.getBody());
			return CHttpResponse.success(response.getStatusCode().value(), response.getBody(), response.getHeaders().toSingleValueMap());
		} catch (final org.springframework.web.client.HttpClientErrorException e) {
			// HTTP 4xx errors (401 Unauthorized, 403 Forbidden, etc.)
			LOGGER.error("❌ POST request failed with HTTP client error: {} {} | Response body: {} | Headers sent: {}", 
				e.getStatusCode(), e.getStatusText(), e.getResponseBodyAsString(), headers);
			
			if (e.getStatusCode().value() == 401) {
				return CHttpResponse.error(401, "Authentication failed: Invalid or missing authorization token");
			} else if (e.getStatusCode().value() == 403) {
				return CHttpResponse.error(403, "Authorization failed: Access denied for this resource");
			}
			return CHttpResponse.error(e.getStatusCode().value(), "Request failed: " + e.getMessage());
		} catch (final org.springframework.web.client.HttpServerErrorException e) {
			// HTTP 5xx errors (500 Internal Server Error, 503 Service Unavailable, etc.)
			LOGGER.error("❌ POST request failed with HTTP server error: {} {} | Response body: {} | Headers sent: {}", 
				e.getStatusCode(), e.getStatusText(), e.getResponseBodyAsString(), headers);
			return CHttpResponse.error(e.getStatusCode().value(), "Server error: " + e.getMessage());
		} catch (final org.springframework.web.client.ResourceAccessException e) {
			// Connection errors (Connection refused, timeout, etc.)
			LOGGER.error("❌ POST request failed with connection error: {} | Headers sent: {}", e.getMessage(), headers);
			return CHttpResponse.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "Connection failed: " + e.getMessage());
		} catch (final RestClientException e) {
			// Other REST client errors
			LOGGER.error("❌ POST request failed with REST client error: {} | Headers sent: {}", e.getMessage(), headers);
			return CHttpResponse.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "Request failed: " + e.getMessage());
		}
	}
}
