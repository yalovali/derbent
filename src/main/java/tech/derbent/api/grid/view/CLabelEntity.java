package tech.derbent.api.grid.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.domain.CStatus;
import tech.derbent.api.interfaces.IHasColorAndIcon;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.base.users.domain.CUser;

/** CLabelEntity - Unified label component for displaying entities or text values.
 * <p>
 * This component provides a standardized way to display entities across the application with appropriate icons, colors,
 * and formatting based on entity type. It automatically detects entity capabilities (IHasColorAndIcon, CStatus,
 * CTypeEntity, CUser) and renders accordingly.
 * </p>
 * <p>
 * <strong>Factory Methods:</strong>
 * </p>
 * <ul>
 * <li>{@link #createLabel(CEntityDB)} - Creates a decorated label for an entity</li>
 * <li>{@link #createLabel(String)} - Creates a simple label for text</li>
 * <li>{@link #createPlainLabel(CEntityDB)} - Creates a plain label without decoration</li>
 * <li>{@link #createH2Label(CEntityDB)} - Creates an H2 header label for entity</li>
 * <li>{@link #createH2Label(String)} - Creates an H2 header label for text</li>
 * <li>{@link #createH3Label(CEntityDB)} - Creates an H3 header label for entity</li>
 * <li>{@link #createH3Label(String)} - Creates an H3 header label for text</li>
 * </ul>
 *
 * @author Derbent Framework
 * @since 1.0 */
public class CLabelEntity extends Div {

	private static final String DEFAULT_BORDER_RADIUS = "4px";
	private static final String DEFAULT_PADDING = "4px 8px";
	private static final Logger LOGGER = LoggerFactory.getLogger(CLabelEntity.class);
	private static final long serialVersionUID = 1L;

	// ==================== STATIC FACTORY METHODS ====================

	/** Creates a decorated label for an entity with icon and color styling.
	 * @param entity the entity to display (can be null)
	 * @return a new CLabelEntity instance */
	public static CLabelEntity createLabel(final CEntityDB<?> entity) {
		final CLabelEntity label = new CLabelEntity();
		label.setValue(entity, true, true);
		return label;
	}

	/** Creates a label for text content.
	 * @param text the text to display
	 * @return a new CLabelEntity instance */
	public static CLabelEntity createLabel(final String text) {
		final CLabelEntity label = new CLabelEntity();
		label.setTextValue(text);
		return label;
	}

	/** Creates a plain label for an entity without any decoration (no icon, no color).
	 * @param entity the entity to display (can be null)
	 * @return a new CLabelEntity instance */
	public static CLabelEntity createPlainLabel(final CEntityDB<?> entity) {
		final CLabelEntity label = new CLabelEntity();
		label.setValue(entity, false, false);
		return label;
	}

	/** Creates an H2 header label for an entity with icon and color.
	 * @param entity the entity to display
	 * @return a Div containing an H2 with entity display */
	public static Div createH2Label(final CEntityDB<?> entity) {
		final Div container = new Div();
		container.getStyle().set("display", "flex").set("align-items", "center").set("gap", "8px");
		final String displayText = getDisplayText(entity);
		final H2 header = new H2(displayText);
		header.getStyle().set("margin", "0");
		try {
			final Icon icon = getIconForEntity(entity);
			if (icon != null) {
				icon.getStyle().set("width", "24px").set("height", "24px").set("flex-shrink", "0");
				applyIconColor(icon, entity);
				container.add(icon);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not create icon for H2 label: {}", e.getMessage());
		}
		container.add(header);
		return container;
	}

	/** Creates an H2 header label for text.
	 * @param text the text to display
	 * @return a Div containing an H2 */
	public static Div createH2Label(final String text) {
		final Div container = new Div();
		container.getStyle().set("display", "flex").set("align-items", "center");
		final H2 header = new H2(text != null ? text : "");
		header.getStyle().set("margin", "0");
		container.add(header);
		return container;
	}

	/** Creates an H3 header label for an entity with icon and color.
	 * @param entity the entity to display
	 * @return a Div containing an H3 with entity display */
	public static Div createH3Label(final CEntityDB<?> entity) {
		final Div container = new Div();
		container.getStyle().set("display", "flex").set("align-items", "center").set("gap", "6px");
		final String displayText = getDisplayText(entity);
		final H3 header = new H3(displayText);
		header.getStyle().set("margin", "0");
		try {
			final Icon icon = getIconForEntity(entity);
			if (icon != null) {
				icon.getStyle().set("width", "20px").set("height", "20px").set("flex-shrink", "0");
				applyIconColor(icon, entity);
				container.add(icon);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not create icon for H3 label: {}", e.getMessage());
		}
		container.add(header);
		return container;
	}

	/** Creates an H3 header label for text.
	 * @param text the text to display
	 * @return a Div containing an H3 */
	public static Div createH3Label(final String text) {
		final Div container = new Div();
		container.getStyle().set("display", "flex").set("align-items", "center");
		final H3 header = new H3(text != null ? text : "");
		header.getStyle().set("margin", "0");
		container.add(header);
		return container;
	}

	// ==================== PRIVATE STATIC HELPERS ====================

	/** Gets the appropriate display text for an entity based on its type.
	 * @param entity the entity
	 * @return the display text */
	private static String getDisplayText(final CEntityDB<?> entity) {
		if (entity == null) {
			return "N/A";
		}
		// Special handling for CUser - show full name
		if (entity instanceof CUser) {
			final CUser user = (CUser) entity;
			final String firstName = user.getName();
			final String lastName = user.getLastname();
			if (firstName != null && !firstName.isEmpty()) {
				if (lastName != null && !lastName.isEmpty()) {
					return firstName + " " + lastName;
				}
				return firstName;
			}
			if (lastName != null && !lastName.isEmpty()) {
				return lastName;
			}
			return "User #" + user.getId();
		}
		// Use CColorUtils for standard entity name extraction
		return CColorUtils.getDisplayTextFromEntity(entity);
	}

	/** Gets the icon for an entity based on its type.
	 * @param entity the entity
	 * @return the icon or null */
	private static Icon getIconForEntity(final CEntityDB<?> entity) {
		if (entity == null) {
			return null;
		}
		try {
			// Use CColorUtils infrastructure for icon resolution
			return CColorUtils.getIconForEntity(entity);
		} catch (final Exception e) {
			LOGGER.debug("Could not get icon for entity {}: {}", entity.getClass().getSimpleName(), e.getMessage());
			return null;
		}
	}

	/** Gets the color for an entity based on its type and interfaces.
	 * @param entity the entity
	 * @return the color hex code or null */
	private static String getColorForEntity(final CEntityDB<?> entity) {
		if (entity == null) {
			return null;
		}
		try {
			// Check IHasColorAndIcon interface first
			if (entity instanceof IHasColorAndIcon) {
				final String color = ((IHasColorAndIcon) entity).getColor();
				if (color != null && !color.isBlank()) {
					return color;
				}
			}
			// Check CStatus entities
			if (entity instanceof CStatus) {
				final String color = ((CStatus<?>) entity).getColor();
				if (color != null && !color.isBlank()) {
					return color;
				}
			}
			// Check CTypeEntity
			if (entity instanceof CTypeEntity) {
				final String color = ((CTypeEntity<?>) entity).getColor();
				if (color != null && !color.isBlank()) {
					return color;
				}
			}
			// Fall back to CColorUtils which handles reflection-based color extraction
			return CColorUtils.getColorFromEntity(entity);
		} catch (final Exception e) {
			LOGGER.debug("Could not get color for entity {}: {}", entity.getClass().getSimpleName(), e.getMessage());
			return null;
		}
	}

	/** Applies color styling to an icon based on entity color.
	 * @param icon   the icon to style
	 * @param entity the entity providing the color */
	private static void applyIconColor(final Icon icon, final CEntityDB<?> entity) {
		if (icon == null) {
			return;
		}
		final String color = getColorForEntity(entity);
		if (color != null && !color.isBlank()) {
			icon.getStyle().set("color", color);
		}
	}

	// ==================== INSTANCE MEMBERS ====================

	private boolean autoContrast = true;
	private boolean showIcon = true;

	/** Default constructor. */
	public CLabelEntity() {
		super();
		initializeComponent();
	}

	/** Constructor with entity value.
	 * @param entity the entity to display */
	public CLabelEntity(final CEntityDB<?> entity) {
		super();
		initializeComponent();
		setValue(entity, true, true);
	}

	/** Constructor with text content.
	 * @param text the text content */
	public CLabelEntity(final String text) {
		super();
		initializeComponent();
		setTextValue(text);
	}

	/** Initializes the component with default styling. */
	private void initializeComponent() {
		getStyle().set("display", "flex");
		getStyle().set("align-items", "center");
		getStyle().set("padding", DEFAULT_PADDING);
		getStyle().set("box-sizing", "border-box");
	}

	/** Sets the entity value with configurable icon and color display.
	 * @param entity    the entity to display
	 * @param showIcon  whether to show the entity icon
	 * @param showColor whether to apply color styling */
	public void setValue(final CEntityDB<?> entity, final boolean showIcon, final boolean showColor) {
		removeAll();
		resetStyles();
		if (entity == null) {
			setText("N/A");
			getStyle().set("color", "#666666");
			getStyle().set("font-style", "italic");
			return;
		}
		final String displayText = getDisplayText(entity);
		String color = null;
		if (showColor) {
			color = getColorForEntity(entity);
		}
		// Apply color-aware styling if color is available
		if (color != null && !color.isBlank()) {
			getStyle().set("background-color", color);
			if (autoContrast) {
				final String textColor = CColorUtils.getContrastTextColor(color);
				getStyle().set("color", textColor);
			}
			getStyle().set("border-radius", DEFAULT_BORDER_RADIUS);
		}
		// Add icon if enabled
		if (showIcon) {
			try {
				final Icon icon = getIconForEntity(entity);
				if (icon != null) {
					CColorUtils.styleIcon(icon);
					// Apply contrasting color to icon if background color is set
					if (color != null && !color.isBlank() && autoContrast) {
						final String textColor = CColorUtils.getContrastTextColor(color);
						icon.getStyle().set("color", textColor);
					}
					add(icon);
				}
			} catch (final Exception e) {
				LOGGER.debug("Could not add icon: {}", e.getMessage());
			}
		}
		// Add text content
		final Span textSpan = new Span(displayText);
		add(textSpan);
	}

	/** Sets a text value.
	 * @param text the text to display */
	public void setTextValue(final String text) {
		removeAll();
		resetStyles();
		setText(text != null ? text : "");
	}

	/** Resets styling to default state. */
	private void resetStyles() {
		getStyle().remove("background-color");
		getStyle().remove("color");
		getStyle().remove("font-style");
		getStyle().remove("border-radius");
	}

	/** Sets whether to use auto-contrast for text color.
	 * @param autoContrast true to enable auto-contrast */
	public void setAutoContrast(final boolean autoContrast) {
		this.autoContrast = autoContrast;
	}

	/** Sets whether to show icon.
	 * @param showIcon true to show icon */
	public void setShowIcon(final boolean showIcon) {
		this.showIcon = showIcon;
	}

	/** Checks if auto-contrast is enabled.
	 * @return true if auto-contrast is enabled */
	public boolean isAutoContrast() {
		return autoContrast;
	}

	/** Checks if icon display is enabled.
	 * @return true if icons are displayed */
	public boolean isShowIcon() {
		return showIcon;
	}

	// ==================== BACKWARD COMPATIBILITY METHODS ====================

	/** Sets entity value with default icon and color display.
	 * @param entity the entity to display
	 * @deprecated Use {@link #setValue(CEntityDB, boolean, boolean)} or {@link #createLabel(CEntityDB)} instead */
	@Deprecated
	public void setEntityValue(final CEntityDB<?> entity) {
		setValue(entity, showIcon, true);
	}

	/** Sets status entity value with color and optional icon.
	 * @param entity the status entity to display
	 * @deprecated Use {@link #setValue(CEntityDB, boolean, boolean)} or {@link #createLabel(CEntityDB)} instead */
	@Deprecated
	public void setStatusValue(final CEntityDB<?> entity) {
		setValue(entity, showIcon, true);
		// Add status-specific styling
		getStyle().set("border-radius", DEFAULT_BORDER_RADIUS);
		getStyle().set("font-weight", "500");
	}
}
