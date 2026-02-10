package tech.derbent.api.menu;

import java.util.Arrays;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;

/**
 * Menu entry that preserves hierarchical ordering as String array.
 * 
 * Replaces Vaadin's MenuEntry to avoid Double-based ordering limitations.
 * 
 * Example:
 * <pre>
 * MyMenuEntry entry = new MyMenuEntry(
 *     "activities/types",
 *     "Project.Activities.Type1",
 *     "10.20.30",  // ← Preserved as [10, 20, 30]
 *     "vaadin:tasks",
 *     CActivityTypeView.class,
 *     false
 * );
 * </pre>
 */
public final class MyMenuEntry {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MyMenuEntry.class);
	
	private final String path;
	private final String title;
	private final String orderString;
	private final int[] orderComponents;
	private final String icon;
	private final Class<? extends Component> menuClass;
	private final boolean showInQuickToolbar;
	private final String[] requiredProfiles;
	
	/**
	 * Constructor for MyMenuEntry.
	 * 
	 * @param path the navigation path
	 * @param title the menu title (may include dots for hierarchy)
	 * @param orderString the order string (e.g., "5.4.3")
	 * @param icon the icon identifier
	 * @param menuClass the view class
	 * @param showInQuickToolbar whether to show in quick toolbar
	 * @param requiredProfiles array of required Spring profiles (empty = no restriction)
	 */
	public MyMenuEntry(final String path, final String title, final String orderString, final String icon,
			final Class<? extends Component> menuClass, final boolean showInQuickToolbar, final String[] requiredProfiles) {
		this.path = Objects.requireNonNull(path, "Path cannot be null");
		this.title = Objects.requireNonNull(title, "Title cannot be null");
		this.orderString = orderString != null ? orderString : "999";
		this.icon = icon != null ? icon : "";
		this.menuClass = Objects.requireNonNull(menuClass, "Menu class cannot be null");
		this.showInQuickToolbar = showInQuickToolbar;
		this.requiredProfiles = requiredProfiles != null ? requiredProfiles : new String[0];
		
		// Parse orderString into integer components
		// "5.4.3" → [5, 4, 3] ✅ NO DATA LOSS!
		orderComponents = parseOrderString(this.orderString);
	}
	
	/**
	 * Parse order string into integer array.
	 * 
	 * Examples:
	 * - "5" → [5]
	 * - "4.1" → [4, 1]
	 * - "10.20.30" → [10, 20, 30]
	 * - "523.123" → [523, 123]
	 * 
	 * @param orderStr the order string
	 * @return array of integer order components
	 */
	private static int[] parseOrderString(final String orderStr) {
		if (orderStr == null || orderStr.trim().isEmpty()) {
			return new int[] { 999 };
		}
		
		try {
			final String[] parts = orderStr.trim().split("\\.");
			final int[] components = new int[parts.length];
			for (int i = 0; i < parts.length; i++) {
				components[i] = Integer.parseInt(parts[i].trim());
			}
			return components;
		} catch (final NumberFormatException e) {
			LOGGER.warn("Invalid menu order format: '{}'. Using default order 999. {}", orderStr, e.getMessage());
			return new int[] { 999 };
		}
	}
	
	/**
	 * Get order component for specific level.
	 * 
	 * @param level the hierarchy level (0-based)
	 * @return the order value at that level, or 999 if level doesn't exist
	 */
	public int getOrderComponent(final int level) {
		if (level < 0 || level >= orderComponents.length) {
			return 999;
		}
		return orderComponents[level];
	}
	
	/**
	 * Compare this entry to another for sorting.
	 * 
	 * Compares level by level:
	 * - "5.4.3" comes before "5.4.30" (3 < 30 at deepest level)
	 * - "5.4.3" comes before "5.14.3" (4 < 14 at child level)
	 * - "10.5.3" comes after "5.14.3" (10 > 5 at parent level)
	 * 
	 * @param other the other entry to compare
	 * @return negative if this < other, zero if equal, positive if this > other
	 */
	public int compareTo(final MyMenuEntry other) {
		final int maxLevels = Math.max(orderComponents.length, other.orderComponents.length);
		for (int level = 0; level < maxLevels; level++) {
			final int thisOrder = getOrderComponent(level);
			final int otherOrder = other.getOrderComponent(level);
			if (thisOrder != otherOrder) {
				return Integer.compare(thisOrder, otherOrder);
			}
		}
		return 0; // Equal at all levels
	}
	
	// Getters (using traditional JavaBean naming for Vaadin compatibility)
	public String getPath() {
		return path;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getOrderString() {
		return orderString;
	}
	
	public int[] getOrderComponents() {
		return orderComponents.clone();
	}
	
	public String getIcon() {
		return icon;
	}
	
	public Class<? extends Component> getMenuClass() {
		return menuClass;
	}
	
	public boolean getShowInQuickToolbar() {
		return showInQuickToolbar;
	}
	
	// Alternative record-style accessors for fluent API
	public String path() {
		return path;
	}
	
	public String title() {
		return title;
	}
	
	public String orderString() {
		return orderString;
	}
	
	public int[] orderComponents() {
		return orderComponents.clone();
	}
	
	public String icon() {
		return icon;
	}
	
	public Class<? extends Component> menuClass() {
		return menuClass;
	}
	
	public boolean showInQuickToolbar() {
		return showInQuickToolbar;
	}
	
	public String[] getRequiredProfiles() {
		return requiredProfiles.clone();
	}
	
	public String[] requiredProfiles() {
		return requiredProfiles.clone();
	}
	
	/**
	 * Check if this menu entry is available in the given active profiles.
	 * 
	 * @param activeProfiles array of currently active Spring profiles
	 * @return true if entry should be shown (no profile restriction OR at least one required profile is active)
	 */
	public boolean isAvailableInProfiles(final String[] activeProfiles) {
		// No profile restriction - available everywhere
		if (requiredProfiles.length == 0) {
			return true;
		}
		
		// Check if any required profile is active
		if (activeProfiles == null || activeProfiles.length == 0) {
			return false;
		}
		
		for (final String required : requiredProfiles) {
			for (final String active : activeProfiles) {
				if (required.equals(active)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "MyMenuEntry{title='%s', orderString='%s', orderComponents=%s, profiles=%s}".formatted(
			title, orderString, Arrays.toString(orderComponents), Arrays.toString(requiredProfiles));
	}
}
