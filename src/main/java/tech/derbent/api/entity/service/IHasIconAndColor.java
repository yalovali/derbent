package tech.derbent.api.entity.service;
public interface IHasIconAndColor {
	public String getColor();

	public void setColor(final String color);
	
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
