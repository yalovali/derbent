package tech.derbent.abstracts.domains;

import org.slf4j.Logger;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;

public interface IDisplayEntity {

	public static String getClassDisplayNameStatic() { return "N/A"; }

	public static String getDisplayNameStatic() { return "N/A"; }

	public static String getStaticEntityColorCode() {
		final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(IDisplayEntity.class);
		LOGGER.warn("getStaticIconColorCode() not implemented, returning default color.");
		return "#007bff";
	}

	public static String getStaticIconColorCode() {
		final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(IDisplayEntity.class);
		LOGGER.warn("getStaticIconColorCode() not implemented, returning default color.");
		return "#007bff";
	}

	public static String getStaticIconFilename() {
		final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(IDisplayEntity.class);
		LOGGER.warn("getStaticIconFilename() not implemented, returning default icon.");
		return "vaadin:calendar-clock";
	}

	public static Class<?> getViewClassStatic() { return null; }

	public String getDisplayName();
	public Class<? extends CAbstractEntityDBPage<?>> getViewClass();
}
