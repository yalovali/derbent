package tech.derbent.abstracts.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.utils.CColorUtils;

/** CEntityLabel - Base label component for displaying entities with both icons and colors.
 * <p>
 * This component provides a standardized way to display entities across the application with appropriate icons and color coding. It automatically
 * detects entity capabilities and renders accordingly: - Icons for all entity types based on their class - Background colors for entities that have
 * color properties - Automatic text contrast for readability
 * </p>
 * <p>
 * The class follows the project's coding guidelines by providing a reusable component that ensures consistent entity visualization across all UI
 * components.
 * </p>
 * <p>
 * <strong>Usage Examples:</strong>
 * </p>
 *
 * <pre>{@code
 * // Simple usage
 * CEntityLabel userLabel = new CEntityLabel(userEntity);
 * // With custom styling
 * CEntityLabel statusLabel = new CEntityLabel(statusEntity, "8px 12px", true, true);
 * }</pre>
 *
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.utils.CColorUtils */
public class CEntityLabel extends HorizontalLayout {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityLabel.class);
	// Default styling configuration
	private static final String DEFAULT_PADDING = "4px 8px";
	private static final String DEFAULT_BORDER_RADIUS = "4px";
	private static final String DEFAULT_ICON_SIZE = "16px";
	private static final String DEFAULT_ICON_MARGIN = "6px";
	private final CEntityNamed<?> entity;
	private final String padding;
	private final boolean autoContrast;
	private final boolean roundedCorners;

	/** Constructor with entity and default styling.
	 * @param entity the entity to display */
	public CEntityLabel(final CEntityNamed<?> entity) {
		this(entity, DEFAULT_PADDING, true, true);
	}

	/** Constructor with entity and custom styling options.
	 * @param entity         the entity to display
	 * @param padding        the padding to apply
	 * @param autoContrast   whether to automatically calculate text contrast
	 * @param roundedCorners whether to apply rounded corners */
	public CEntityLabel(final CEntityNamed<?> entity, final String padding, final boolean autoContrast, final boolean roundedCorners) {
		super();
		this.entity = entity;
		this.padding = padding != null ? padding : DEFAULT_PADDING;
		this.autoContrast = autoContrast;
		this.roundedCorners = roundedCorners;
		initializeLabel();
	}

	/** Applies color styling to the label based on entity color properties. */
	private void applyColorStyling() {
		try {
			// Get entity color or fallback to default
			final String backgroundColor = CColorUtils.getColorFromEntity(entity);
			// Apply background color
			getStyle().set("background-color", backgroundColor);
			// Apply automatic text contrast
			if (autoContrast) {
				final String textColor = CColorUtils.getContrastTextColor(backgroundColor);
				getStyle().set("color", textColor);
				// Also apply color to any child icons for consistency
				getChildren().forEach(component -> {
					if (component instanceof Icon) {
						component.getElement().getStyle().set("color", textColor);
					}
				});
			}
			// Apply layout styling
			getStyle().set("padding", padding);
			getStyle().set("display", "inline-flex");
			getStyle().set("align-items", "center");
			getStyle().set("white-space", "nowrap");
			if (roundedCorners) {
				getStyle().set("border-radius", DEFAULT_BORDER_RADIUS);
			}
		} catch (final Exception e) {
			LOGGER.warn("Error applying color styling to entity label: {}", e.getMessage());
		}
	}

	/** Gets the entity being displayed by this label.
	 * @return the entity object */
	public Object getEntity() { return entity; }

	/** Initializes the label with appropriate icon and styling. */
	private void initializeLabel() {
		// Configure layout properties
		setSpacing(false);
		setPadding(false);
		setAlignItems(Alignment.CENTER);
		setWidthFull();
		if (entity == null) {
			add(new Span("N/A"));
			return;
		}
		try {
			// Get entity display text
			final String displayText = entity.getName();
			// Create icon if available
			final Icon icon = CColorUtils.getIconForEntity(entity);
			// Create text span
			final Span textSpan = new Span(displayText);
			// Add components based on icon availability
			if (icon != null) {
				// Style the icon
				styleIcon(icon);
				add(icon, textSpan);
			} else {
				add(textSpan);
				LOGGER.debug("Created entity label without icon for: {}", displayText);
			}
			// Apply color styling
			applyColorStyling();
		} catch (final Exception e) {
			LOGGER.warn("Error creating entity label for {}: {}", entity.getClass().getSimpleName(), e.getMessage());
			add(new Span("Error: " + entity.getClass().getSimpleName()));
		}
	}

	/** Refreshes the label display (useful if entity properties have changed). */
	public void refresh() {
		removeAll();
		initializeLabel();
	}

	/** Applies icon styling with consistent sizing and spacing.
	 * @param icon the icon to style */
	private void styleIcon(final Icon icon) {
		icon.getStyle().set("margin-right", DEFAULT_ICON_MARGIN);
		icon.getStyle().set("width", DEFAULT_ICON_SIZE);
		icon.getStyle().set("height", DEFAULT_ICON_SIZE);
		icon.getStyle().set("flex-shrink", "0"); // Prevent icon from shrinking
	}
}
