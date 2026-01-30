package tech.derbent.bab.http.domain;

import java.time.LocalDateTime;

/**
 * Result of a connection attempt to Calimero server.
 * Value object representing success or failure of connection.
 */
public class CConnectionResult {

	private final boolean success;
	private final String message;
	private final LocalDateTime timestamp;
	private final String ipAddress;
	private final Integer port;

	private CConnectionResult(final boolean success, final String message, final String ipAddress, final Integer port) {
		this.success = success;
		this.message = message;
		this.timestamp = LocalDateTime.now();
		this.ipAddress = ipAddress;
		this.port = port;
	}

	/**
	 * Create successful connection result.
	 * @param message   Success message
	 * @param ipAddress Server IP address
	 * @param port      Server port
	 * @return Success result
	 */
	public static CConnectionResult success(final String message, final String ipAddress, final Integer port) {
		return new CConnectionResult(true, message, ipAddress, port);
	}

	/**
	 * Create failed connection result.
	 * @param message   Error message
	 * @param ipAddress Server IP address
	 * @param port      Server port
	 * @return Failure result
	 */
	public static CConnectionResult failure(final String message, final String ipAddress, final Integer port) {
		return new CConnectionResult(false, message, ipAddress, port);
	}

	public boolean isSuccess() {
		return success;
	}

	public boolean isFailure() {
		return !success;
	}

	public String getMessage() {
		return message;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public Integer getPort() {
		return port;
	}

	@Override
	public String toString() {
		return String.format("CConnectionResult{success=%s, message='%s', ipAddress='%s', port=%d}", success, message,
				ipAddress, port);
	}
}
