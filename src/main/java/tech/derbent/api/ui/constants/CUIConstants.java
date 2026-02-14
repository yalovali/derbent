package tech.derbent.api.ui.constants;
/** CUIConstants - Centralized UI styling constants for Derbent application. Layer: View (MVC) Profile: api (Common framework) Contains all magic
 * numbers and styling values used across UI components. Following AGENTS.md Section 3.1: All custom classes start with 'C' prefix. Usage: - Use these
 * constants instead of hardcoded values - Single point of maintenance for UI consistency - Easy theme adjustments
 * @since 2026-02-12 */
public final class CUIConstants {
	// ==========================================
	// SPACING & GAPS
	// ==========================================

	/** Large border radius: 12px */
	public static final String BORDER_RADIUS_LARGE = "12px";
	/** Medium border radius: 8px */
	public static final String BORDER_RADIUS_MEDIUM = "8px";
	/** Small border radius: 2px */
	public static final String BORDER_RADIUS_SMALL = "2px";
	/** Standard border radius: 4px */
	public static final String BORDER_RADIUS_STANDARD = "4px";
	/** Dashed border style */
	public static final String BORDER_STYLE_DASHED = "dashed";
	/** Standard border style: solid */
	public static final String BORDER_STYLE_SOLID = "solid";
	// ==========================================
	// PADDING
	// ==========================================
	/** Accent border width (left): 4px */
	public static final String BORDER_WIDTH_ACCENT = "4px";
	/** Standard border width: 1px */
	public static final String BORDER_WIDTH_STANDARD = "1px";
	/** Thick border width: 2px */
	public static final String BORDER_WIDTH_THICK = "2px";
	/** Desktop breakpoint: 1280px */
	public static final String BREAKPOINT_DESKTOP = "1280px";
	/** Mobile breakpoint: 768px */
	public static final String BREAKPOINT_MOBILE = "768px";
	/** Tablet breakpoint: 1024px */
	public static final String BREAKPOINT_TABLET = "1024px";
	// ==========================================
	// MARGINS
	// ==========================================
	/** Error background color: #ffebee */
	public static final String COLOR_ERROR_BG = "#ffebee";
	/** Error border color: #f44336 */
	public static final String COLOR_ERROR_BORDER = "#f44336";
	/** Error text color: #c62828 */
	public static final String COLOR_ERROR_TEXT = "#c62828";
	/** Dark gray text: #333333 */
	public static final String COLOR_GRAY_DARK = "#333333";
	/** Light gray background: #f5f5f5 */
	public static final String COLOR_GRAY_LIGHT = "#f5f5f5";
	// ==========================================
	// DIALOG DIMENSIONS
	// ==========================================
	/** Medium gray background: #e0e0e0 */
	public static final String COLOR_GRAY_MEDIUM = "#e0e0e0";
	/** Very light gray background: #fafafa */
	public static final String COLOR_GRAY_VERY_LIGHT = "#fafafa";
	/** Info background color: #e3f2fd */
	public static final String COLOR_INFO_BG = "#e3f2fd";
	/** Info border color: #2196f3 */
	public static final String COLOR_INFO_BORDER = "#2196f3";
	/** Info text color: #1976d2 */
	public static final String COLOR_INFO_TEXT = "#1976d2";
	/** Purple accent color: #7b1fa2 */
	public static final String COLOR_PURPLE_ACCENT = "#7b1fa2";
	/** Success background color: #e8f5e9 */
	public static final String COLOR_SUCCESS_BG = "#e8f5e9";
	/** Success border color: #4caf50 */
	public static final String COLOR_SUCCESS_BORDER = "#4caf50";
	// ==========================================
	// COMPONENT DIMENSIONS
	// ==========================================
	/** Success text color: #2e7d32 */
	public static final String COLOR_SUCCESS_TEXT = "#2e7d32";
	/** Warning background color: #fff3e0 */
	public static final String COLOR_WARNING_BG = "#fff3e0";
	/** Warning border color: #ff9800 */
	public static final String COLOR_WARNING_BORDER = "#ff9800";
	/** Warning text color: #f57c00 */
	public static final String COLOR_WARNING_TEXT = "#f57c00";
	/** White background: #ffffff */
	public static final String COLOR_WHITE = "#ffffff";
	/** Auto-trigger delay for dialogs: 300ms */
	public static final int DELAY_AUTO_TRIGGER = 300;
	/** Auto-trigger delay for email dialogs: 500ms */
	public static final int DELAY_AUTO_TRIGGER_EMAIL = 500;
	/** Dialog height for compact content: 60vh */
	public static final String DIALOG_HEIGHT_COMPACT = "60vh";
	/** Dialog height for full content: 90vh */
	public static final String DIALOG_HEIGHT_FULL = "90vh";
	/** Dialog height for medium content: 550px */
	public static final String DIALOG_HEIGHT_MEDIUM = "550px";
	/** Standard dialog height (auto with max): 80vh */
	public static final String DIALOG_MAX_HEIGHT = "80vh";
	public static final String DIALOG_WIDTH_EXTRA_WIDE = "1000px";
	public static final String DIALOG_WIDTH_NARROW = "400px";
	public static final String DIALOG_WIDTH_STANDARD = "600px";
	public static final String DIALOG_WIDTH_WIDE = "800px";
	public static final String FIELD_WIDTH_NARROW = "100px";
	public static final String FIELD_WIDTH_STANDARD = "150px";
	public static final String FIELD_WIDTH_WIDE = "600px";
	public static final String FONT_SIZE_LARGE = "1.2em";
	public static final String FONT_SIZE_SMALL = "0.9em";
	public static final String FONT_SIZE_STANDARD = "1em";
	public static final String FONT_SIZE_TINY = "0.8em";
	public static final String FONT_SIZE_XLARGE = "1.5em";
	public static final String FONT_WEIGHT_BOLD = "700";
	public static final String FONT_WEIGHT_MEDIUM = "500";
	public static final String FONT_WEIGHT_NORMAL = "400";
	public static final String FONT_WEIGHT_SEMIBOLD = "600";
	public static final String FORM_FIELD_FIXED_WIDTH_BOOLEAN = FIELD_WIDTH_NARROW;
	public static final String FORM_FIELD_FIXED_WIDTH_MULTI_ITEM = FIELD_WIDTH_WIDE;
	public static final String FORM_FIELD_FIXED_WIDTH_NUMERIC_DATE = FIELD_WIDTH_STANDARD;
	public static final String FORM_FIELD_FIXED_WIDTH_STRING = FIELD_WIDTH_WIDE;
	public static final String FORM_FIELD_FIXED_WIDTH_TEXTAREA = FIELD_WIDTH_WIDE;
	/** Extra tiny gap for very compact layouts like grid items: 2px */
	public static final String GAP_EXTRA_TINY = "2px";
	// ==========================================
	// COLORS - Neutral
	// ==========================================
	/** Large gap for major sections: 24px */
	public static final String GAP_LARGE = "24px";
	/** Small gap between related components: 6px */
	public static final String GAP_SMALL = "6px";
	/** Standard gap between components: 12px */
	public static final String GAP_STANDARD = "12px";
	/** Tiny gap for compact layouts: 4px */
	public static final String GAP_TINY = "4px";
	/** Extra large gap for major section separation: 32px */
	public static final String GAP_XLARGE = "32px";
	/** Error gradient background */
	public static final String GRADIENT_ERROR = "linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%)";
	// ==========================================
	// BORDER STYLES
	// ==========================================
	/** Info gradient background */
	public static final String GRADIENT_INFO = "linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%)";
	/** Purple gradient for search/filter sections */
	public static final String GRADIENT_PURPLE = "linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%)";
	/** Success gradient background */
	public static final String GRADIENT_SUCCESS = "linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%)";
	/** Light success gradient background for details */
	public static final String GRADIENT_SUCCESS_LIGHT = "linear-gradient(135deg, #f1f8e9 0%, #dcedc1 100%)";
	/** Warning gradient background */
	public static final String GRADIENT_WARNING = "linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%)";
	/** Grid short height: 300px */
	public static final String GRID_HEIGHT_SHORT = "300px";
	// ==========================================
	// FONT SIZES
	// ==========================================
	/** Grid standard height: 400px */
	public static final String GRID_HEIGHT_STANDARD = "400px";
	/** Grid tall height: 600px */
	public static final String GRID_HEIGHT_TALL = "600px";
	/** Minimum width for labels: 120px */
	public static final String LABEL_MIN_WIDTH = "120px";
	/** Minimum width for form labels: 150px */
	public static final String LABEL_MIN_WIDTH_FORM = "150px";
	/** Bottom margin small: 0 0 8px 0 */
	public static final String MARGIN_BOTTOM_SMALL = "0 0 8px 0";
	// ==========================================
	// FONT WEIGHTS
	// ==========================================
	/** Bottom margin only: 0 0 12px 0 */
	public static final String MARGIN_BOTTOM_STANDARD = "0 0 12px 0";
	/** No margin */
	public static final String MARGIN_NONE = "0";
	/** Small margin: 8px */
	public static final String MARGIN_SMALL = "8px";
	/** Standard margin: 16px */
	public static final String MARGIN_STANDARD = "16px";
	// ==========================================
	// GRADIENTS
	// ==========================================
	/** Extra padding for generous spacing: 2px 6px (used in labels) */
	public static final String PADDING_LABEL = "2px 6px";
	/** Large padding for major containers: 24px */
	public static final String PADDING_LARGE = "24px";
	/** Medium padding for dialogs/cards: 20px */
	public static final String PADDING_MEDIUM = "20px";
	/** Small padding for compact areas: 8px */
	public static final String PADDING_SMALL = "8px";
	/** Standard padding for containers: 16px */
	public static final String PADDING_STANDARD = "16px";
	// ==========================================
	// SHADOWS
	// ==========================================
	/** Tiny padding for minimal spacing: 4px */
	public static final String PADDING_TINY = "4px";
	/** Result area max height: 250px */
	public static final String RESULT_AREA_MAX_HEIGHT = "250px";
	/** Heavy shadow for dialogs */
	public static final String SHADOW_HEAVY = "0 8px 16px rgba(0, 0, 0, 0.24)";
	/** Light shadow for subtle elevation */
	public static final String SHADOW_LIGHT = "0 1px 3px rgba(0, 0, 0, 0.12)";
	// ==========================================
	// Z-INDEX LAYERS
	// ==========================================
	/** Medium shadow for elevated components */
	public static final String SHADOW_MEDIUM = "0 4px 8px rgba(0, 0, 0, 0.16)";
	/** Standard shadow for cards */
	public static final String SHADOW_STANDARD = "0 2px 4px rgba(0, 0, 0, 0.16)";
	/** TextArea short height: 100px */
	public static final String TEXTAREA_HEIGHT_SHORT = "100px";
	/** TextArea standard height: 200px */
	public static final String TEXTAREA_HEIGHT_STANDARD = "200px";
	/** TextArea tall height: 300px */
	public static final String TEXTAREA_HEIGHT_TALL = "300px";
	// ==========================================
	// TIMING & ANIMATION
	// ==========================================
	/** Fast transition: 0.15s */
	public static final String TRANSITION_FAST = "0.15s";
	/** Slow transition: 0.5s */
	public static final String TRANSITION_SLOW = "0.5s";
	/** Standard transition: 0.3s */
	public static final String TRANSITION_STANDARD = "0.3s";
	/** Base layer: 1 */
	public static final String Z_INDEX_BASE = "1";
	/** Dialog layer: 1000 */
	public static final String Z_INDEX_DIALOG = "1000";
	// ==========================================
	// BREAKPOINTS (Responsive)
	// ==========================================
	/** Elevated layer: 10 */
	public static final String Z_INDEX_ELEVATED = "10";
	/** Toast/notification layer: 10000 */
	public static final String Z_INDEX_NOTIFICATION = "10000";
	/** Overlay layer: 100 */
	public static final String Z_INDEX_OVERLAY = "100";
	// ==========================================
	// CONSTRUCTOR (Utility class)
	// ==========================================

	private CUIConstants() {
		// Utility class - no instantiation
		throw new AssertionError("CUIConstants is a utility class and cannot be instantiated");
	}
}
