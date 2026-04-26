package tech.derbent.api.perf;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Opt-in filter that opens a per-request invocation tracking context.
 *
 * <p>
 * Enable with: derbent.perf.invocations.enabled=true
 *
 * <p>
 * Why a filter: Vaadin UI work happens inside HTTP requests (including UIDL roundtrips), so a request boundary is a practical, low-noise scope.
 */
@Component
@ConditionalOnProperty(name = "derbent.perf.invocations.enabled", havingValue = "true")
public class CPerfInvocationTrackingFilter extends OncePerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPerfInvocationTrackingFilter.class);

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
			throws ServletException, java.io.IOException {

		final String requestId = UUID.randomUUID().toString().substring(0, 8);
		final String requestLabel = buildRequestLabel(request);

		CPerfInvocationTracker.start(requestId, requestLabel);
		try {
			filterChain.doFilter(request, response);
		} finally {
			try {
				final CPerfInvocationTracker.CReport report = CPerfInvocationTracker.finishReport();
				if (report != null && report.hasDuplicates() && LOGGER.isDebugEnabled()) {
					LOGGER.debug("PERF-DUP rid={} durMs={} total={} label={} duplicates={}",
						report.getRequestId(),
						report.getDurationNs() / 1_000_000,
						report.getTotalInvocations(),
						report.getRequestLabel(),
						report.getDuplicatesSorted(CPerfInvocationTracker.DEFAULT_MAX_DUPLICATES_TO_LOG));
				}
			} finally {
				CPerfInvocationTracker.clear();
			}
		}
	}

	private static String buildRequestLabel(final HttpServletRequest request) {
		final String vaadinType = request.getHeader("X-Vaadin-Request-Type");
		final String uiId = request.getParameter("v-uiId");
		final String uri = request.getRequestURI();

		// Keep this short; it is intended for log correlation, not for full audit logging.
		return "%s %s vaadinType=%s uiId=%s".formatted(
				request.getMethod(),
				uri,
				vaadinType != null ? vaadinType : "-",
				uiId != null ? uiId : "-");
	}
}
