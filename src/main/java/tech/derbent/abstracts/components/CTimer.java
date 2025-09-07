package tech.derbent.abstracts.components;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CTimer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTimer.class);
	private static long start = System.nanoTime();
	private static final int MAX_ENTRIES = 1000;
	private static final Map<String, Long> stamps = new LinkedHashMap<>() {

		private static final long serialVersionUID = 1L;

		protected boolean removeEldestEntry(final Map.Entry<String, Long> eldest) {
			return size() > MAX_ENTRIES;
		}
	};
	private static String lastStampLabel = null;

	// add this helper
	private static String callingMethodName() {
		for (final StackTraceElement el : Thread.currentThread().getStackTrace()) {
			final String cls = el.getClassName();
			if (!cls.equals(CTimer.class.getName()) && !cls.startsWith("java.lang.Thread")) {
				return el.getMethodName(); // <-- method name only
			}
		}
		return "unknown";
	}

	public static void clear() {
		stamps.clear();
		start = System.nanoTime();
		LOGGER.info("CTimer reset.");
	}

	// keep print(String) as-is, but replace your no-arg print() with:
	public static void print() {
		// if no prior stamp() was made, fall back to the caller's method name
		final String label = (lastStampLabel != null) ? lastStampLabel : callingMethodName();
		print(label);
		lastStampLabel = null;
	}

	public static void print(final String label) {
		final Long stampTime = stamps.get(label);
		if (stampTime == null) {
			LOGGER.warn("No stamp found for label: {}", label);
			return;
		}
		final long delta = (System.nanoTime() - stampTime) / 1_000_000;
		LOGGER.info("{} took {} ms", label, delta);
	}

	public static void printAll() {
		long last = start;
		LOGGER.info("---- Timing Report ----");
		for (final Map.Entry<String, Long> entry : stamps.entrySet()) {
			final long delta = (entry.getValue() - last) / 1_000_000;
			LOGGER.info("{} took {} ms", entry.getKey(), delta);
			last = entry.getValue();
		}
		final long total = (System.nanoTime() - start) / 1_000_000;
		LOGGER.info("Total elapsed time: {} ms", total);
		LOGGER.info("------------------------");
		stamps.clear(); // clear after printing
		lastStampLabel = null;
	}

	// replace your current no-arg stamp()
	public static void stamp() {
		final String label = callingMethodName(); // e.g., "createSampleScreens"
		stamps.put(label, System.nanoTime());
		LOGGER.debug("stamp added: {}", label);
		lastStampLabel = label;
	}

	public static void stamp(final String name) {
		stamps.put(name, System.nanoTime());
		lastStampLabel = name;
	}
}
