package tech.derbent.api.grid.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.domain.CStatus;
import tech.derbent.api.interfaces.IHasColorAndIcon;
import tech.derbent.api.ui.component.basic.CH2;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;

/** CLabelEntity - Unified label component for displaying entities or text values.
 * <p>
 * This component provides a standardized way to display entities across the application with appropriate icons, colors, and formatting based on
 * entity type. It automatically detects entity capabilities (IHasColorAndIcon, CStatus, CTypeEntity, CUser) and renders accordingly.
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
 * @author Derbent Framework
 * @since 1.0 */
public class CLabelEntity extends Div {

	private static final String DEFAULT_BORDER_RADIUS = "4px";
	private static final String DEFAULT_PADDING = "4px 8px";
	private static final Logger LOGGER = LoggerFactory.getLogger(CLabelEntity.class);
	private static final long serialVersionUID = 1L;
	// ==================== STATIC FACTORY METHODS ====================

	/** Applies color styling to an icon based on entity color.
	 * @param icon   the icon to style
	 * @param entity the entity providing the color */
	private static void applyIconColor(final Icon icon, final CEntityDB<?> entity) {
		if (icon == null) {
			return;
		}
		Check.instanceOf(entity, IHasColorAndIcon.class, "Entity must implement IHasColorAndIcon to apply icon color");
		final String color = getColorForEntity(entity);
		icon.getStyle().set("width", "24px").set("height", "24px").set("flex-shrink", "0");
		if (color != null && !color.isBlank()) {
			icon.getStyle().set("color", color);
		}
	}

	/** Creates an H2 header label for an entity with icon and color.
	 * @param entity the entity to display
	 * @return a Div containing an H2 with entity display */
	public static CLabelEntity createH2Label(final CEntityDB<?> entity) {
		final CLabelEntity container = new CLabelEntity();
		final String displayText = CColorUtils.getDisplayTextFromEntity(entity);
		final CH2 header = new CH2(displayText);
		header.getStyle().set("margin", "0");
		if (entity instanceof IHasColorAndIcon) {
			final Icon icon = getIconForEntity(entity);
			if (icon != null) {
				applyIconColor(icon, entity);
				container.add(icon);
			}
		}
		container.add(header);
		return container;
	}

	/** Creates an H2 header label for text.
	 * @param text the text to display
	 * @return a Div containing an H2 */
	public static CLabelEntity createH2Label(final String text) {
		final CLabelEntity container = new CLabelEntity();
		final CH2 header = new CH2(text != null ? text : "");
		header.getStyle().set("margin", "0");
		container.add(header);
		return container;
	}

	/** Creates an H3 header label for an entity with icon and color.
	 * @param entity the entity to display
	 * @return a Div containing an H3 with entity display */
	public static Div createH3Label(final CEntityDB<?> entity) {
		final CLabelEntity container = new CLabelEntity();
		final String displayText = CColorUtils.getDisplayTextFromEntity(entity);
		final CH3 header = new CH3(displayText);
		header.getStyle().set("margin", "0");
		if (entity instanceof IHasColorAndIcon) {
			final Icon icon = getIconForEntity(entity);
			if (icon != null) {
				applyIconColor(icon, entity);
				container.add(icon);
			}
		}
		container.add(header);
		return container;
	}

	/** Creates an H3 header label for text.
	 * @param text the text to display
	 * @return a Div containing an H3 */
	public static Div createH3Label(final String text) {
		final CLabelEntity container = new CLabelEntity();
		container.getStyle().set("display", "flex").set("align-items", "center");
		final CH3 header = new CH3(text != null ? text : "");
		header.getStyle().set("margin", "0");
		container.add(header);
		return container;
	}

	/** Creates a plain label for an entity without any decoration (no icon, no color).
	 * @param entity the entity to display (can be null)
	 * @return a new CLabelEntity instance */
	public static CLabelEntity createPlainLabel(final CEntityDB<?> entity) {
		final CLabelEntity label = new CLabelEntity();
		label.setValue(entity, false);
		return label;
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
		setValue(entity, true);
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

	/** Resets styling to default state. */
	private void resetStyles() {
		getStyle().remove("background-color");
		getStyle().remove("color");
		getStyle().remove("font-style");
		getStyle().remove("border-radius");
	}

	/** Sets a text value.
	 * @param text the text to display */
	public void setTextValue(final String text) {
		removeAll();
		resetStyles();
		setText(text != null ? text : "");
	}

	public void setValue(final CEntityDB<?> entity, final boolean showIconColor) {
		setValue(entity, CColorUtils.getDisplayTextFromEntity(entity), showIconColor);
	}

	public void setValue(final CEntityDB<?> entity, String displayText, final boolean showIconColor) {
		try {
			removeAll();
			resetStyles();
			if (entity == null) {
				setText("N/A");
				getStyle().set("color", "#666666");
				getStyle().set("font-style", "italic");
				return;
			}
			if (entity instanceof IHasColorAndIcon && showIconColor) {
				final String color = getColorForEntity(entity);
				getStyle().set("background-color", color);
				getStyle().set("color", CColorUtils.getContrastTextColor(color));
				getStyle().set("border-radius", DEFAULT_BORDER_RADIUS);
				final Icon icon = getIconForEntity(entity);
				CColorUtils.styleIcon(icon);
				icon.getStyle().set("color", CColorUtils.getContrastTextColor(color));
				add(icon);
			}
			// Add text content
			final Span textSpan = new Span(displayText);
			add(textSpan);
		} catch (final Exception e) {
			LOGGER.error("Error setting value for CLabelEntity: {}", e.getMessage());
			throw e;
		}
	}
}
