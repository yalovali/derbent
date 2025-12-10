package tech.derbent.api.interfaces;
public interface IHasColor {

	String getColor();
	void setColor(String color);
	
	/**
	 * Returns a string representation of this object including color information.
	 * This method can be used by implementing classes to build their toString() output.
	 * 
	 * @return a string representation including color information
	 */
	default String toColorString() {
		return String.format("color=%s", getColor());
	}
}
