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

	/** CDE Yellow - Alert/warning yellow (#FFEAAA) - LIGHT, use DARK version for icons */
	public static final String CDE_YELLOW = "#FFEAAA";

	/** CDE Yellow Dark - Darker amber for better visibility (#D4A84B) */
	public static final String CDE_YELLOW_DARK = "#D4A84B";

	// ===========================================
	// X11 Solaris Named Colors
	// From /usr/openwin/lib/X11/rgb.txt
	// ===========================================

	/** Papayawhip - Light peachy cream (#FFEFD5) - LIGHT */
	public static final String X11_PAPAYAWHIP = "#FFEFD5";

	/** Navajowhite - Warm tan (#FFDEAD) - LIGHT */
	public static final String X11_NAVAJOWHITE = "#FFDEAD";

	/** Wheat - Light wheat color (#F5DEB3) - LIGHT */
	public static final String X11_WHEAT = "#F5DEB3";

	/** Tan - Classic tan (#D2B48C) */
	public static final String X11_TAN = "#D2B48C";

	/** AntiqueWhite - Warm white (#FAEBD7) - LIGHT */
	public static final String X11_ANTIQUEWHITE = "#FAEBD7";

	/** Burlywood - Wood-like tan (#DEB887) */
	public static final String X11_BURLYWOOD = "#DEB887";

	// ===========================================
	// Darker Alternatives for Menu/Icon Visibility
	// These colors provide better contrast on gray backgrounds
	// ===========================================

	/** Peru - Darker brown for financial entities (#CD853F) */
	public static final String X11_PERU = "#CD853F";

	/** Sienna - Darker reddish-brown for expenses (#A0522D) */
	public static final String X11_SIENNA = "#A0522D";

	/** SaddleBrown - Dark brown for budgets (#8B4513) */
	public static final String X11_SADDLEBROWN = "#8B4513";

	/** Goldenrod - Medium gold/yellow for meetings (#DAA520) */
	public static final String X11_GOLDENROD = "#DAA520";

	/** DarkGoldenrod - Darker gold for comments (#B8860B) */
	public static final String X11_DARKGOLDENROD = "#B8860B";

	/** Olive - Darker olive for components (#808000) */
	public static final String X11_OLIVE = "#808000";

	/** OliveDrab - Military olive for products (#6B8E23) */
	public static final String X11_OLIVEDRAB = "#6B8E23";

	/** RosyBrown - Muted brownish-pink for deliverables (#BC8F8F) */
	public static final String X11_ROSYBROWN = "#BC8F8F";

	/** DarkKhaki - Muted khaki for pages (#BDB76B) */
	public static final String X11_DARKKHAKI = "#BDB76B";

	/** SlateGray - Blue-gray for assets (#708090) */
	public static final String X11_SLATEGRAY = "#708090";

	/** DimGray - Dark neutral gray for providers (#696969) */
	public static final String X11_DIMGRAY = "#696969";

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

	// Finances (Darker tans/browns - money/value)
	/** Budgets - SaddleBrown for financial planning - darker for visibility */
	public static final String ENTITY_BUDGET = X11_SADDLEBROWN;

	/** Orders - Peru for purchase orders - darker for visibility */
	public static final String ENTITY_ORDER = X11_PERU;

	/** Expenses - Sienna for outgoing money - darker for visibility */
	public static final String ENTITY_EXPENSE = X11_SIENNA;

	/** Income - DarkGoldenrod for incoming money - darker for visibility */
	public static final String ENTITY_INCOME = X11_DARKGOLDENROD;

	/** Currency - Peru for monetary units - darker for visibility */
	public static final String ENTITY_CURRENCY = X11_PERU;

	// Meetings & Calendar (Darker yellows/golds - alerts/events)
	/** Meetings - Goldenrod for calendar events - darker for visibility */
	public static final String ENTITY_MEETING = X11_GOLDENROD;

	/** Comments - DarkGoldenrod for discussion - darker for visibility */
	public static final String ENTITY_COMMENT = X11_DARKGOLDENROD;

	// Risks & Warnings (OpenWindows Border Dark - caution)
	/** Risks - Border Dark for risk items */
	public static final String ENTITY_RISK = OPENWIN_BORDER_DARK;

	/** Risk Levels - Darker variant for severity */
	public static final String ENTITY_RISK_LEVEL = OPENWIN_BORDER_DARKER;

	// Products & Components (Darker olives/browns - constructive)
	/** Products - OliveDrab for product entities - darker for visibility */
	public static final String ENTITY_PRODUCT = X11_OLIVEDRAB;

	/** Components - Olive for component parts - darker for visibility */
	public static final String ENTITY_COMPONENT = X11_OLIVE;

	/** Deliverables - RosyBrown for deliverable items - darker for visibility */
	public static final String ENTITY_DELIVERABLE = X11_ROSYBROWN;

	// Assets & Resources (Darker gray tones for visibility)
	/** Assets - SlateGray for owned items - darker for visibility */
	public static final String ENTITY_ASSET = X11_SLATEGRAY;

	/** Providers - DimGray for external providers - darker for visibility */
	public static final String ENTITY_PROVIDER = X11_DIMGRAY;

	// Companies & Organizations (Darker tones for visibility)
	/** Companies - Border Dark for top-level organization - darker for visibility */
	public static final String ENTITY_COMPANY = OPENWIN_BORDER_DARK;

	/** Pages - DarkKhaki for navigation pages - darker for visibility */
	public static final String ENTITY_PAGE = X11_DARKKHAKI;

	// Dashboard & Views (CDE Purple tones)
	/** Dashboard - CDE Purple for main dashboard */
	public static final String ENTITY_DASHBOARD = CDE_PURPLE;

	/** Gantt - CDE Titlebar Purple for timeline views */
	public static final String ENTITY_GANTT = CDE_TITLEBAR_PURPLE;

	// System & Settings (Darker elements for visibility)
	/** System Settings - Border Dark for configuration - darker for visibility */
	public static final String ENTITY_SYSTEM_SETTINGS = OPENWIN_BORDER_DARK;

	/** Grid Entity - Border Darker for grid configuration - darker for visibility */
	public static final String ENTITY_GRID = OPENWIN_BORDER_DARKER;

	/** Detail Section - Olive for form sections - darker for visibility */
	public static final String ENTITY_DETAIL_SECTION = X11_OLIVE;

	// Private constructor to prevent instantiation
	private CSolarisColorPalette() {
		// Utility class - no instantiation
	}
}
