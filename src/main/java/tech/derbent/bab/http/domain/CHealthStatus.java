package tech.derbent.bab.http.domain;

import java.time.LocalDateTime;

/** Health check status for Calimero server. Builder pattern for flexible construction. */
public class CHealthStatus {
	/** Builder for CHealthStatus. */
	public static class Builder {
		private boolean healthy;
		private int statusCode;
		private String message;
		private long responseTime;

		public CHealthStatus build() {
			return new CHealthStatus(this);
		}

		public Builder healthy(final boolean healthy1) {
			this.healthy = healthy1;
			return this;
		}

		public Builder message(final String message1) {
			this.message = message1;
			return this;
		}

		public Builder responseTime(final long responseTime1) {
			this.responseTime = responseTime1;
			return this;
		}

		public Builder statusCode(final int statusCode1) {
			this.statusCode = statusCode1;
			return this;
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private final boolean healthy;
	private final int statusCode;
	private final String message;
	private final long responseTime;
	private final LocalDateTime timestamp;

	private CHealthStatus(final Builder builder) {
		this.healthy = builder.healthy;
		this.statusCode = builder.statusCode;
		this.message = builder.message;
		this.responseTime = builder.responseTime;
		this.timestamp = LocalDateTime.now();
	}

	public String getMessage() { return message; }

	public long getResponseTime() { return responseTime; }

	public int getStatusCode() { return statusCode; }

	public LocalDateTime getTimestamp() { return timestamp; }

	public boolean isHealthy() { return healthy; }

	@Override
	public String toString() {
		return String.format("CHealthStatus{healthy=%s, statusCode=%d, message='%s', responseTime=%dms}", healthy, statusCode, message, responseTime);
	}
}
