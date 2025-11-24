package tech.derbent.api.interfaces;

/**
 * IHasColorAndIcon - Interface for entities that have color and icon fields.
 * Provides common methods for accessing color and icon properties.
 * Implementations should define DEFAULT_COLOR and DEFAULT_ICON constants.
 */
public interface IHasColorAndIcon {

	/**
	 * Get the color for this entity.
	 * @return the color code (e.g., hex color like "#28a745")
	 */
	String getColor();

	/**
	 * Set the color for this entity.
	 * @param color the color code to set
	 */
	void setColor(String color);

	/**
	 * Get the icon for this entity.
	 * @return the icon identifier (e.g., "vaadin:calendar-clock")
	 */
	String getIcon();
}
