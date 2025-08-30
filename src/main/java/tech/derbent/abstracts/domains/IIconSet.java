package tech.derbent.abstracts.domains;

import java.lang.reflect.Method;
import org.slf4j.Logger;

public interface IIconSet {
	public static String getEntityColorCode() {
		final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(IIconSet.class);
		LOGGER.warn("getIconColorCode() not implemented, returning default color.");
		return "#007bff";
	}

	public static String getIconColorCode() {
		final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(IIconSet.class);
		LOGGER.warn("getIconColorCode() not implemented, returning default color.");
		return "#007bff";
	}

	public static String getIconFilename() {
		final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(IIconSet.class);
		LOGGER.warn("getIconFilename() not implemented, returning default icon.");
		return "vaadin:calendar-clock";
	}

	public static String resolveIconFilename(final Class<?> entityClass) {
		try {
			// look for a static method named getIconFilename with no args
			final Method method = entityClass.getMethod("getIconFilename");
			// invoke it with null because it's static
			final Object result = method.invoke(null);
			return result == null ? null : result.toString();
		} catch (final NoSuchMethodException e) {
			throw new IllegalArgumentException("Class " + entityClass.getName() + " does not define static getIconFilename()", e);
		} catch (final Exception e) {
			throw new RuntimeException("Error invoking getIconFilename() on " + entityClass.getName(), e);
		}
	}
}
