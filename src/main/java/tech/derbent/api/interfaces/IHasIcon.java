package tech.derbent.api.interfaces;

import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.utils.CColorUtils;

public interface IHasIcon extends IHasColor {

	default Icon getIcon() { return CColorUtils.getIconFromString(getIconString()); }

	String getIconString();
}
