package tech.derbent.abstracts.domains;

import org.slf4j.Logger;

public interface IDisplayView {

	public static String getDisplayNameStatic() { return "Unnamed"; }

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
		LOGGER.warn("getIconFilename() not implemented, returning default icon.");
		return "vaadin:calendar-clock";
	}
}
