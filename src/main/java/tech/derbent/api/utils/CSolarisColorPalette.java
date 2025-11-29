package tech.derbent.api.utils;

/**
 * Solaris/CDE Color Palette Constants.
 * Based on classic Sun Solaris and CDE (Common Desktop Environment) color schemes.
 * 
 * This palette provides a consistent retro-computing aesthetic inspired by:
 * - OpenWindows (Solaris 1.x - 2.5.1) warm beige/tan colors
 * - CDE (Solaris 2.6 - Solaris 10) purple, gray, and teal-green scheme
 * - X11 Solaris named colors from /usr/openwin/lib/X11/rgb.txt
 */
public final class CSolarisColorPalette {

	// ===========================================
	// OpenWindows Base Colors (Warm Beige/Tan)
	// ===========================================

	/** Base background - Light cream/beige (#EDE8D1) */
	public static final String OPENWIN_BASE_BACKGROUND = "#EDE8D1";

	/** 3D Light Edge - Very light cream (#FFFBEA) */
	public static final String OPENWIN_3D_LIGHT = "#FFFBEA";

	/** 3D Shadow Edge - Tan/khaki shadow (#C3B79F) */
	public static final String OPENWIN_3D_SHADOW = "#C3B79F";

	/** Window Border Dark - Brown/olive border (#91856C) */
	public static final String OPENWIN_BORDER_DARK = "#91856C";

	/** Menu Background - Light tan (#F0E5C0) */
	public static final String OPENWIN_MENU_BACKGROUND = "#F0E5C0";

	/** Button Face - Beige button color (#E4D9B4) */
	public static final String OPENWIN_BUTTON_FACE = "#E4D9B4";

	// ===========================================
	// OpenWindows Accent Colors
	// ===========================================

	/** Selection Blue - Classic selection highlight (#4966B0) */
	public static final String OPENWIN_SELECTION_BLUE = "#4966B0";

	/** Selection Blue Dark - Darker blue for secondary items (#3A5791) */
	public static final String OPENWIN_SELECTION_BLUE_DARK = "#3A5791";

	/** Text Black - Standard text color (#000000) */
	public static final String OPENWIN_TEXT_BLACK = "#000000";

	/** Disabled Gray - Grayed out elements (#A9A08B) */
	public static final String OPENWIN_DISABLED_GRAY = "#A9A08B";

	/** Highlight Yellow - Very Solaris yellow (#F5E8A2) */
	public static final String OPENWIN_HIGHLIGHT_YELLOW = "#F5E8A2";

	/** Border Darker - Darker olive border variant (#7A6E58) */
	public static final String OPENWIN_BORDER_DARKER = "#7A6E58";

	// ===========================================
	// CDE Primary Colors (Gray Scale)
	// ===========================================

	/** CDE Background Gray - Main gray (#B5B5B5) */
	public static final String CDE_BACKGROUND_GRAY = "#B5B5B5";

	/** CDE Light Gray - Lighter elements (#D9D9D9) */
	public static final String CDE_LIGHT_GRAY = "#D9D9D9";

	/** CDE Dark Gray - Darker elements (#8E8E8E) */
	public static final String CDE_DARK_GRAY = "#8E8E8E";

	/** CDE Border Black - Border color (#000000) */
	public static final String CDE_BORDER_BLACK = "#000000";

	// ===========================================
	// CDE Signature Purples (Iconic CDE colors)
	// ===========================================

	/** CDE Purple - Main purple (#6B5FA7) */
	public static final String CDE_PURPLE = "#6B5FA7";

	/** CDE Titlebar Purple - Darker titlebar purple (#4B4382) */
	public static final String CDE_TITLEBAR_PURPLE = "#4B4382";

	/** CDE Active Purple - Active/selected purple (#8377C5) */
	public static final String CDE_ACTIVE_PURPLE = "#8377C5";

	// ===========================================
	// CDE Greens (Toolbox buttons)
	// ===========================================

	/** CDE Green - Main teal green (#4B7F82) */
	public static final String CDE_GREEN = "#4B7F82";

	/** CDE Light Green - Lighter teal (#6CAFB0) */
	public static final String CDE_LIGHT_GREEN = "#6CAFB0";

	// ===========================================
	// CDE Yellows (Alerts)
	// ===========================================

	/** CDE Yellow - Alert/warning yellow (#FFEAAA) */
	public static final String CDE_YELLOW = "#FFEAAA";

	// ===========================================
	// X11 Solaris Named Colors
	// From /usr/openwin/lib/X11/rgb.txt
	// ===========================================

	/** Papayawhip - Light peachy cream (#FFEFD5) */
	public static final String X11_PAPAYAWHIP = "#FFEFD5";

	/** Navajowhite - Warm tan (#FFDEAD) */
	public static final String X11_NAVAJOWHITE = "#FFDEAD";

	/** Wheat - Light wheat color (#F5DEB3) */
	public static final String X11_WHEAT = "#F5DEB3";

	/** Tan - Classic tan (#D2B48C) */
	public static final String X11_TAN = "#D2B48C";

	/** AntiqueWhite - Warm white (#FAEBD7) */
	public static final String X11_ANTIQUEWHITE = "#FAEBD7";

	/** Burlywood - Wood-like tan (#DEB887) */
	public static final String X11_BURLYWOOD = "#DEB887";

	// ===========================================
	// Entity-Specific Solaris Colors
	// Semantic colors for different entity types
	// ===========================================

	// Projects & Planning (CDE Purples - authoritative/organizational)
	/** Projects - CDE Purple for organizational entities */
	public static final String ENTITY_PROJECT = CDE_PURPLE;

	/** Sprints - CDE Active Purple for time-boxed work */
	public static final String ENTITY_SPRINT = CDE_ACTIVE_PURPLE;

	/** Milestones - CDE Titlebar Purple for key achievements */
	public static final String ENTITY_MILESTONE = CDE_TITLEBAR_PURPLE;

	// Teams & Users (CDE Greens - collaborative/people)
	/** Teams - CDE Green for group entities */
	public static final String ENTITY_TEAM = CDE_GREEN;

	/** Users - CDE Light Green for individual people */
	public static final String ENTITY_USER = CDE_LIGHT_GREEN;

	/** Roles - CDE Dark Gray for organizational roles */
	public static final String ENTITY_ROLE = CDE_DARK_GRAY;

	// Tasks & Activities (OpenWindows Selection Blue - actionable items)
	/** Activities - Selection Blue for work items */
	public static final String ENTITY_ACTIVITY = OPENWIN_SELECTION_BLUE;

	/** Tickets - Slightly darker blue for support items */
	public static final String ENTITY_TICKET = OPENWIN_SELECTION_BLUE_DARK;

	/** Decisions - Border Dark for authoritative decisions */
	public static final String ENTITY_DECISION = OPENWIN_BORDER_DARK;

	// Workflows & Status (OpenWindows Grays - process/state)
	/** Workflows - 3D Shadow for process flows */
	public static final String ENTITY_WORKFLOW = OPENWIN_3D_SHADOW;

	/** Status - Disabled Gray for state indicators */
	public static final String ENTITY_STATUS = OPENWIN_DISABLED_GRAY;

	// Finances (X11 Tan/Burlywood - money/value)
	/** Budgets - Burlywood for financial planning */
	public static final String ENTITY_BUDGET = X11_BURLYWOOD;

	/** Orders - Tan for purchase orders */
	public static final String ENTITY_ORDER = X11_TAN;

	/** Expenses - Navajowhite for outgoing money */
	public static final String ENTITY_EXPENSE = X11_NAVAJOWHITE;

	/** Income - Wheat for incoming money */
	public static final String ENTITY_INCOME = X11_WHEAT;

	/** Currency - Papayawhip for monetary units */
	public static final String ENTITY_CURRENCY = X11_PAPAYAWHIP;

	// Meetings & Calendar (CDE Yellow - alerts/events)
	/** Meetings - CDE Yellow for calendar events */
	public static final String ENTITY_MEETING = CDE_YELLOW;

	/** Comments - Highlight Yellow for discussion */
	public static final String ENTITY_COMMENT = OPENWIN_HIGHLIGHT_YELLOW;

	// Risks & Warnings (OpenWindows Border Dark - caution)
	/** Risks - Border Dark for risk items */
	public static final String ENTITY_RISK = OPENWIN_BORDER_DARK;

	/** Risk Levels - Darker variant for severity */
	public static final String ENTITY_RISK_LEVEL = OPENWIN_BORDER_DARKER;

	// Products & Components (OpenWindows Button Face - constructive)
	/** Products - Button Face for product entities */
	public static final String ENTITY_PRODUCT = OPENWIN_BUTTON_FACE;

	/** Components - Menu Background for component parts */
	public static final String ENTITY_COMPONENT = OPENWIN_MENU_BACKGROUND;

	/** Deliverables - 3D Light for deliverable items */
	public static final String ENTITY_DELIVERABLE = OPENWIN_3D_LIGHT;

	// Assets & Resources (CDE Gray tones)
	/** Assets - CDE Light Gray for owned items */
	public static final String ENTITY_ASSET = CDE_LIGHT_GRAY;

	/** Providers - CDE Background Gray for external providers */
	public static final String ENTITY_PROVIDER = CDE_BACKGROUND_GRAY;

	// Companies & Organizations (CDE/OpenWindows blend)
	/** Companies - Base Background for top-level organization */
	public static final String ENTITY_COMPANY = OPENWIN_BASE_BACKGROUND;

	/** Pages - AntiqueWhite for navigation pages */
	public static final String ENTITY_PAGE = X11_ANTIQUEWHITE;

	// Dashboard & Views (CDE Purple tones)
	/** Dashboard - CDE Purple for main dashboard */
	public static final String ENTITY_DASHBOARD = CDE_PURPLE;

	/** Gantt - CDE Titlebar Purple for timeline views */
	public static final String ENTITY_GANTT = CDE_TITLEBAR_PURPLE;

	// System & Settings (OpenWindows 3D elements)
	/** System Settings - 3D Shadow for configuration */
	public static final String ENTITY_SYSTEM_SETTINGS = OPENWIN_3D_SHADOW;

	/** Grid Entity - Border Dark for grid configuration */
	public static final String ENTITY_GRID = OPENWIN_BORDER_DARK;

	/** Detail Section - Menu Background for form sections */
	public static final String ENTITY_DETAIL_SECTION = OPENWIN_MENU_BACKGROUND;

	// Private constructor to prevent instantiation
	private CSolarisColorPalette() {
		// Utility class - no instantiation
	}
}
