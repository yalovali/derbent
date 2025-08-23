package tech.derbent.abstracts.services;

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

	public static void clear() {
		stamps.clear();
		start = System.nanoTime();
		LOGGER.info("CTimer reset.");
	}

	public static void print() {
		if (lastStampLabel == null) {
			LOGGER.warn("No stamps recorded yet.");
			return;
		}
		print(lastStampLabel);
		lastStampLabel = null;
	}

	public static void print(final Object caller) {
		final String label = resolveCaller(caller);
		print(label);
	}

	public static void print(final String name) {
		final Long time = stamps.remove(name); // stamp removed after printing
		if (time == null) {
			LOGGER.warn("No stamp found for name: {}", name);
			return;
		}
		final long duration = (time - start) / 1_000_000;
		LOGGER.info("Time to {}: {} ms", name, duration);
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

	private static String resolveCaller(final Object caller) {
		final String callerClass = caller.getClass().getName();
		for (final StackTraceElement element : Thread.currentThread().getStackTrace()) {
			if (element.getClassName().equals(callerClass)) {
				final String methodName = element.getMethodName();
				return String.format("%s.%s(%s:%d)", element.getClassName(), methodName, element.getFileName(), element.getLineNumber());
			}
		}
		return caller.getClass().getSimpleName() + "#unknownMethod";
	}

	public static void stamp() {
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (final StackTraceElement element : stackTrace) {
			if (!element.getClassName().equals(CTimer.class.getName()) && !element.getClassName().startsWith("java.lang.Thread")) {
				// get shorter class name
				String simpleClassName = element.getClassName();
				if (simpleClassName.contains(".")) {
					simpleClassName = simpleClassName.substring(simpleClassName.lastIndexOf(".") + 1);
				}
				final String label = String.format("%s (%s:%d)", element.getMethodName(), element.getFileName(), element.getLineNumber());
				stamps.put(label, System.nanoTime());
				lastStampLabel = label;
				break;
			}
		}
	}

	public static void stamp(final Object caller) {
		final String label = resolveCaller(caller);
		stamps.put(label, System.nanoTime());
		lastStampLabel = label;
	}

	public static void stamp(final String name) {
		stamps.put(name, System.nanoTime());
		lastStampLabel = name;
	}
}
