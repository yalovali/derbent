package tech.derbent.bab.http.domain;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic HTTP response wrapper.
 * Value object representing the result of an HTTP request.
 */
public class CHttpResponse {

	private final int statusCode;
	private final String body;
	private final Map<String, String> headers;
	private final LocalDateTime timestamp;
	private final boolean success;
	private final String errorMessage;

	private CHttpResponse(final int statusCode, final String body, final Map<String, String> headers,
			final boolean success, final String errorMessage) {
		this.statusCode = statusCode;
		this.body = body;
		this.headers = headers != null ? headers : new HashMap<>();
		this.timestamp = LocalDateTime.now();
		this.success = success;
		this.errorMessage = errorMessage;
	}

	/**
	 * Create successful response.
	 * @param statusCode HTTP status code
	 * @param body       Response body
	 * @param headers    Response headers
	 * @return Success response
	 */
	public static CHttpResponse success(final int statusCode, final String body, final Map<String, String> headers) {
		return new CHttpResponse(statusCode, body, headers, true, null);
	}

	/**
	 * Create error response.
	 * @param statusCode   HTTP status code
	 * @param errorMessage Error message
	 * @return Error response
	 */
	public static CHttpResponse error(final int statusCode, final String errorMessage) {
		return new CHttpResponse(statusCode, null, null, false, errorMessage);
	}

	public boolean isSuccess() {
		return success;
	}

	public boolean isError() {
		return !success;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getBody() {
		return body;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public String toString() {
		return String.format("CHttpResponse{statusCode=%d, success=%s, body='%s', errorMessage='%s'}", statusCode,
				success, body, errorMessage);
	}
}
