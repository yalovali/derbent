package tech.derbent.api.interfaces;

/** Interface for entities and components that have a color property.
 * <p>
 * This interface provides color management functionality and a helper method
 * for including color information in toString() implementations.
 * </p> */
public interface IHasColor {

	String getColor();
	void setColor(String color);
	
	/** Returns a string representation of the color for debugging.
	 * <p>
	 * This default method provides a helper for implementing classes to include
	 * color information in their toString() methods.
	 * </p>
	 * @return a string representation of the color, e.g., "color=#FF0000" */
	default String toColorString() {
		return "color=" + getColor();
	}
}
