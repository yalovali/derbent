package tech.derbent.plm.gnnt.gnntviewentity.view.components;

public final class CGnntBoardMetrics {

	public static final int GRID_HEADER_ROW_HEIGHT_PX = 76;
	public static final int GRID_ROW_HEIGHT_PX = 32;
	public static final int TIMELINE_HEADER_CONTROL_HEIGHT_PX = 30;
	public static final int TIMELINE_HEADER_ROW_HEIGHT_PX = 20;
	public static final int TIMELINE_MAX_WIDTH_PX = 1600;
	public static final int TIMELINE_MIN_WIDTH_PX = 520;
	public static final int TIMELINE_ROW_HEIGHT_PX = 28;
	public static final int TIMELINE_WIDTH_PX = 880;
	public static final int WIDTH_NAME_PX = 280;
	public static final int WIDTH_REFERENCE_PX = 180;
	public static final int WIDTH_TYPE_PX = 140;

	private CGnntBoardMetrics() {
	}

	public static String px(final int value) {
		return value + "px";
	}
}
