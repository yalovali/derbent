package tech.derbent.api.interfaces;

public interface IHasIcon extends IHasColor {

	/** Returns the icon string identifier (e.g., "vaadin:user").
	 * @return icon string identifier */
	String getIcon();

	/** Returns the icon data as a byte array (e.g., profile picture, logo image).
	 * Returns null if no icon data is available.
	 * @return byte array containing image data, or null */
	byte[] getIconData();
}
