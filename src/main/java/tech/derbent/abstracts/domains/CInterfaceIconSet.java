package tech.derbent.abstracts.domains;

import org.slf4j.Logger;

public interface CInterfaceIconSet {
	public static String getIconColorCode() {
		final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CInterfaceIconSet.class);
		LOGGER.warn("getIconColorCode() not implemented, returning default color.");
		return "#007bff";
	}

	public static String getIconFilename() {
		final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CInterfaceIconSet.class);
		LOGGER.warn("getIconFilename() not implemented, returning default icon.");
		return "vaadin:calendar-clock";
	}
}
