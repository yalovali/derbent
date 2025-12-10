package tech.derbent.api.interfaces;

import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.utils.CColorUtils;

public interface IHasIcon extends IHasColor {

	default Icon getIcon() { return CColorUtils.getIconFromString(getIconString()); }

	String getIconString();
	
	/**
	 * Returns a string representation of this object including icon and color information.
	 * This method can be used by implementing classes to build their toString() output.
	 * 
	 * @return a string representation including icon and color information
	 */
	@Override
	default String toColorString() {
		return String.format("icon=%s, color=%s", getIconString(), getColor());
	}
}
