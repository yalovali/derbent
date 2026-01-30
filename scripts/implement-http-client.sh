#!/bin/bash
# BAB HTTP Client - Complete Implementation Script
# This script creates all necessary directories and Java source files
# Version: 1.0
# Date: 2026-01-30

set -e  # Exit on error

PROJECT_ROOT="/home/yasin/git/derbent"
HTTP_BASE="$PROJECT_ROOT/src/main/java/tech/derbent/bab/http"

echo "======================================================================"
echo "🚀 BAB HTTP Client - Complete Implementation"
echo "======================================================================"
echo ""
echo "📍 Project Root: $PROJECT_ROOT"
echo "📍 HTTP Base: $HTTP_BASE"
echo ""

# ============================================================================
# STEP 1: Create Directory Structure
# ============================================================================
echo "📁 Step 1: Creating directory structure..."
mkdir -p "$HTTP_BASE/domain"
mkdir -p "$HTTP_BASE/service"

echo "✅ Directories created successfully"
echo ""

# ============================================================================
# STEP 2: Create Domain Classes
# ============================================================================
echo "📝 Step 2: Creating domain classes..."

# --------------------------------------------------------------------------
# CHttpResponse.java
# --------------------------------------------------------------------------
cat > "$HTTP_BASE/domain/CHttpResponse.java" << 'EOF'
package tech.derbent.bab.http.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic HTTP response wrapper.
 * Encapsulates HTTP response data, status, and headers.
 */
public class CHttpResponse {

	private final int statusCode;
	private final String body;
	private final Map<String, String> headers;
	private final boolean success;
	private final String errorMessage;

	private CHttpResponse(final int statusCode, final String body, final Map<String, String> headers,
			final boolean success, final String errorMessage) {
		this.statusCode = statusCode;
		this.body = body;
		this.headers = headers != null ? headers : new HashMap<>();
		this.success = success;
		this.errorMessage = errorMessage;
	}

	/**
	 * Create successful HTTP response.
	 * @param statusCode HTTP status code
	 * @param body       Response body
	 * @param headers    Response headers
	 * @return Success response
	 */
	public static CHttpResponse success(final int statusCode, final String body, final Map<String, String> headers) {
		return new CHttpResponse(statusCode, body, headers, true, null);
	}

	/**
	 * Create error HTTP response.
	 * @param statusCode   HTTP status code
	 * @param errorMessage Error message
	 * @return Error response
	 */
	public static CHttpResponse error(final int statusCode, final String errorMessage) {
		return new CHttpResponse(statusCode, null, null, false, errorMessage);
	}

	public int getStatusCode() { return statusCode; }

	public String getBody() { return body; }

	public Map<String, String> getHeaders() { return headers; }

	public boolean isSuccess() { return success; }

	public String getErrorMessage() { return errorMessage; }

	/**
	 * Get header value by name.
	 * @param headerName Header name
	 * @return Header value or null
	 */
	public String getHeader(final String headerName) {
		return headers.get(headerName);
	}

	@Override
	public String toString() {
		if (success) {
			return String.format("CHttpResponse{status=%d, bodyLength=%d}", statusCode,
					body != null ? body.length() : 0);
		}
		return String.format("CHttpResponse{status=%d, error='%s'}", statusCode, errorMessage);
	}
}
EOF

echo "   ✅ Created: CHttpResponse.java"

# --------------------------------------------------------------------------
# CConnectionResult.java
# --------------------------------------------------------------------------
cat > "$HTTP_BASE/domain/CConnectionResult.java" << 'EOF'
package tech.derbent.bab.http.domain;

/**
 * Connection result for Calimero server connection attempts.
 * Encapsulates success/failure status and details.
 */
public class CConnectionResult {

	private final boolean success;
	private final String message;
	private final Exception exception;

	private CConnectionResult(final boolean success, final String message, final Exception exception) {
		this.success = success;
		this.message = message;
		this.exception = exception;
	}

	/**
	 * Create successful connection result.
	 * @param message Success message
	 * @return Success result
	 */
	public static CConnectionResult success(final String message) {
		return new CConnectionResult(true, message, null);
	}

	/**
	 * Create failed connection result.
	 * @param message Failure message
	 * @return Failure result
	 */
	public static CConnectionResult failure(final String message) {
		return new CConnectionResult(false, message, null);
	}

	/**
	 * Create error connection result with exception.
	 * @param message   Error message
	 * @param exception Exception that occurred
	 * @return Error result
	 */
	public static CConnectionResult error(final String message, final Exception exception) {
		return new CConnectionResult(false, message, exception);
	}

	public boolean isSuccess() { return success; }

	public boolean isFailure() { return !success; }

	public String getMessage() { return message; }

	public Exception getException() { return exception; }

	@Override
	public String toString() {
		if (success) {
			return String.format("✅ Connection Success: %s", message);
		}
		if (exception != null) {
			return String.format("❌ Connection Error: %s (Exception: %s)", message, exception.getMessage());
		}
		return String.format("❌ Connection Failed: %s", message);
	}
}
EOF

echo "   ✅ Created: CConnectionResult.java"

# --------------------------------------------------------------------------
# CHealthStatus.java
# --------------------------------------------------------------------------
cat > "$HTTP_BASE/domain/CHealthStatus.java" << 'EOF'
package tech.derbent.bab.http.domain;

/**
 * Health status result for Calimero server health checks.
 * Provides detailed health information.
 */
public class CHealthStatus {

	private final boolean healthy;
	private final int statusCode;
	private final String message;
	private final long responseTime;

	private CHealthStatus(final Builder builder) {
		this.healthy = builder.healthy;
		this.statusCode = builder.statusCode;
		this.message = builder.message;
		this.responseTime = builder.responseTime;
	}

	public boolean isHealthy() { return healthy; }

	public int getStatusCode() { return statusCode; }

	public String getMessage() { return message; }

	public long getResponseTime() { return responseTime; }

	@Override
	public String toString() {
		return String.format("%s Health Status: %s (Status: %d, Response: %dms)", healthy ? "✅" : "❌", message,
				statusCode, responseTime);
	}

	/**
	 * Builder for CHealthStatus.
	 */
	public static class Builder {

		private boolean healthy;
		private int statusCode;
		private String message;
		private long responseTime;

		public Builder healthy(final boolean healthy) {
			this.healthy = healthy;
			return this;
		}

		public Builder statusCode(final int statusCode) {
			this.statusCode = statusCode;
			return this;
		}

		public Builder message(final String message) {
			this.message = message;
			return this;
		}

		public Builder responseTime(final long responseTime) {
			this.responseTime = responseTime;
			return this;
		}

		public CHealthStatus build() {
			return new CHealthStatus(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}
}
EOF

echo "   ✅ Created: CHealthStatus.java"

echo ""
echo "✅ Domain classes created (3 files)"
echo ""

# ============================================================================
# COMPLETION MESSAGE
# ============================================================================
echo "======================================================================"
echo "✅ BAB HTTP Client Implementation Complete!"
echo "======================================================================"
echo ""
echo "📊 Summary:"
echo "   - Domain classes: 3 files"
echo "   - Service classes: Pending (see implementation guide)"
echo "   - Client classes: Pending (see implementation guide)"
echo ""
echo "📖 Next Steps:"
echo "   1. Review created files in: $HTTP_BASE"
echo "   2. Continue with service implementation (see HTTP_CLIENT_IMPLEMENTATION.md)"
echo "   3. Implement CCalimeroRequest and CCalimeroResponse"
echo "   4. Implement CHttpService"
echo "   5. Implement CClientProject and CClientProjectService"
echo "   6. Modify CProject class"
echo "   7. Build: mvn clean compile -Pagents -DskipTests"
echo "   8. Test connection with Calimero server"
echo ""
echo "📚 Documentation:"
echo "   - Architecture: docs/bab/HTTP_CLIENT_ARCHITECTURE.md"
echo "   - Implementation: docs/bab/HTTP_CLIENT_IMPLEMENTATION.md"
echo ""
echo "🎯 Calimero Projects:"
echo "   - Server: ~/git/calimero"
echo "   - Test: ~/git/calimeroTest"
echo ""
echo "======================================================================"
EOF

echo "✅ Created implementation script: scripts/implement-http-client.sh"
echo ""

# Make the script executable
chmod +x /home/yasin/git/derbent/scripts/implement-http-client.sh

echo "✅ Script is now executable"
echo ""
echo "To run the script:"
echo "  cd /home/yasin/git/derbent"
echo "  ./scripts/implement-http-client.sh"
