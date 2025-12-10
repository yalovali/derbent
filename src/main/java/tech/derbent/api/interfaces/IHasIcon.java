package tech.derbent.api.interfaces;

import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.utils.CColorUtils;

/** Interface for entities and components that have both icon and color properties.
 * <p>
 * This interface extends IHasColor and provides icon management functionality.
 * </p> */
public interface IHasIcon extends IHasColor {

	default Icon getIcon() { return CColorUtils.getIconFromString(getIconString()); }

	String getIconString();
	
	/** Returns a string representation of the icon and color for debugging.
	 * <p>
	 * Overrides IHasColor.toColorString() to include both icon and color information.
	 * </p>
	 * @return a string representation of icon and color, e.g., "icon=vaadin:user, color=#FF0000" */
	@Override
	default String toColorString() {
		return "icon=" + getIconString() + ", color=" + getColor();
	}
}
