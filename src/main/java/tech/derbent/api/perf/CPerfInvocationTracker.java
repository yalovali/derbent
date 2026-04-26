package tech.derbent.api.perf;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.data.util.ProxyUtils;

/** Request-scoped invocation tracker used to detect accidental repeated server-side calls during a single HTTP request (Vaadin UIDL roundtrip).
 * <p>
 * Design goals:
 * <ul>
 * <li>opt-in only (a Servlet filter opens/closes the ThreadLocal context)</li>
 * <li>very low overhead when disabled (null ThreadLocal check)</li>
 * <li>focus on duplicate detection (counts), not full tracing/profiling</li>
 * </ul>
 */
public final class CPerfInvocationTracker {

	private static final class CContext {

		private final LinkedHashMap<String, Integer> counts =
				new LinkedHashMap<>();
		private final String requestId;
		private final String requestLabel;
		private final long startNs = System.nanoTime();

		private CContext(final String requestId, final String requestLabel) {
			this.requestId = Objects.requireNonNull(requestId, "requestId");
			this.requestLabel =
					Objects.requireNonNull(requestLabel, "requestLabel");
		}

		private void increment(final String key) {
			counts.merge(key, 1, (arg0, arg1) -> Integer.sum(arg0, arg1));
		}

		private CReport toReport() {
			final long durationNs = System.nanoTime() - startNs;
			return new CReport(requestId, requestLabel, durationNs,
					Map.copyOf(counts));
		}
	}

	public static final class CReport {

		private final Map<String, Integer> counts;
		private final long durationNs;
		private final String requestId;
		private final String requestLabel;

		private CReport(final String requestId, final String requestLabel,
				final long durationNs, final Map<String, Integer> counts) {
			this.requestId = requestId;
			this.requestLabel = requestLabel;
			this.durationNs = durationNs;
			this.counts = counts;
		}

		public Map<String, Integer> getCounts() { return counts; }

		public List<Map.Entry<String, Integer>>
				getDuplicatesSorted(final int max) {
			final int limit = Math.max(1, max);
			return counts.entrySet().stream()
					.filter(e -> e.getValue() != null && e.getValue() > 1)
					.sorted(Comparator
							.comparingInt(Map.Entry<String, Integer>::getValue)
							.reversed())
					.limit(limit).collect(Collectors.toList());
		}

		public long getDurationNs() { return durationNs; }

		public String getRequestId() { return requestId; }

		public String getRequestLabel() { return requestLabel; }

		public int getTotalInvocations() {
			return counts.values().stream().mapToInt(Integer::intValue).sum();
		}

		public boolean hasDuplicates() {
			return counts.values().stream()
					.anyMatch(count -> count != null && count > 1);
		}
	}

	private static final ThreadLocal<CContext> CONTEXT_THREAD_LOCAL =
			new ThreadLocal<>();
	public static final int DEFAULT_MAX_DUPLICATES_TO_LOG = 20;

	public static void clear() {
		CONTEXT_THREAD_LOCAL.remove();
	}

	public static @Nullable CReport finishReport() {
		final CContext context = CONTEXT_THREAD_LOCAL.get();
		return context == null ? null : context.toReport();
	}

	public static boolean isActive() {
		return CONTEXT_THREAD_LOCAL.get() != null;
	}

	public static void recordInvocation(final Object target,
			final Method method) {
		final CContext context = CONTEXT_THREAD_LOCAL.get();
		if (context == null) {
			return;
		}
		// Use user class names so Spring proxies do not pollute the report.
		final Class<?> userClass = ProxyUtils.getUserClass(target.getClass());
		final String key = userClass.getSimpleName() + "." + method.getName()
				+ "/" + method.getParameterCount();
		context.increment(key);
	}

	public static void start(final String requestId,
			final String requestLabel) {
		CONTEXT_THREAD_LOCAL.set(new CContext(requestId, requestLabel));
	}

	private CPerfInvocationTracker() {
		// Utility class
	}
}
