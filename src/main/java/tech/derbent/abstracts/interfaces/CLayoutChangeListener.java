package tech.derbent.abstracts.interfaces;

import tech.derbent.session.service.CLayoutService.LayoutMode;

/** Interface for components that want to be notified when the layout mode changes between horizontal and vertical orientations. */
public interface CLayoutChangeListener {

	/** Called when the layout mode changes.
	 * @param newMode The new layout mode (HORIZONTAL or VERTICAL) */
	void onLayoutModeChanged(LayoutMode newMode);
}
