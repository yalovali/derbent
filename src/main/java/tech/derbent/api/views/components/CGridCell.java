package tech.derbent.api.views.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.utils.CColorUtils;

/** CGridCell - Enhanced grid cell component that supports both regular text and color-aware rendering.
 * <p>
 * This class provides a unified foundation for all grid cell components, supporting: - Regular text display - Color-aware status rendering with
 * background colors - Optional icon display for entities - Consistent styling and behavior
 * </p>
 * <p>
 * The class follows the project's coding guidelines by consolidating all cell functionality into a single, reusable component that uses Div for
 * proper layout control.
 * </p>
 * <p>
 * <strong>Usage Examples:</strong>
 * </p>
 *
 * <pre>{@code
 * // Basic text cell
 * CGridCell cell = new CGridCell("Sample Text");
 * // Color-aware status cell
 * CGridCell statusCell = new CGridCell();
 * statusCell.setStatusValue(statusEntity);
 * // Entity cell with color metadata
 * CGridCell entityCell = new CGridCell(entity);
 * }</pre>
 *
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.api.views.grids.CGrid
 * @see tech.derbent.api.utils.CColorUtils */
public class CGridCell extends Div {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGridCell.class);
	private static final long serialVersionUID = 1L;
	private Boolean autoContrast = Boolean.TRUE;
	// Simplified styling configuration - only essential properties
	private Boolean showIcon = Boolean.FALSE;

	/** Default constructor for CGridCell. */
	public CGridCell() {
		super();
		initializeCell();
	}

	/** Constructor for CGridCell with entity value.
	 * @param entity the entity to display in the cell */
	public CGridCell(final CEntityDB<?> entity) {
		super();
		setEntityValue(entity);
		initializeCell();
	}

	/** Constructor for CGridCell with text content.
	 * @param text the text content for the cell */
	public CGridCell(final String text) {
		super();
		setText(text);
		initializeCell();
	}

	/** Apply color-aware styling for entities with colors.
	 * @param entity      the entity providing the color
	 * @param color       the background color to apply
	 * @param displayText the text to display
	 * @throws Exception */
	private void applyColorStyling(final CEntityDB<?> entity, final String color, final String displayText) throws Exception {
		// Apply background color
		getStyle().set("background-color", color);
		// Apply contrasting text color if auto-contrast is enabled
		if (Boolean.TRUE.equals(autoContrast)) {
			final String textColor = CColorUtils.getContrastTextColor(color);
			getStyle().set("color", textColor);
		}
		// Create content with icon if enabled
		if (Boolean.TRUE.equals(showIcon)) {
			final Icon icon = CColorUtils.getIconForEntity(entity);
			if (icon != null) {
				// Configure icon styling
				icon.setSize("16px");
				icon.getStyle().set("margin-right", "6px");
				icon.getStyle().set("flex-shrink", "0");
				add(icon);
			}
		}
		// Add text content
		add(displayText);
	}

	/** Apply default styling for status cells when color is not available. */
	private void applyDefaultStatusStyling() {
		getStyle().set("background-color", "#f8f9fa");
		getStyle().set("color", "#495057");
		getStyle().set("border", "1px solid #dee2e6");
	}

	/** Apply default styling to the cell. */
	private void applyDefaultStyling() {
		getStyle().set("width", "100%");
		getStyle().set("height", "100%");
		getStyle().set("display", "flex");
		getStyle().set("align-items", "center");
		getStyle().set("padding", "4px 8px");
		getStyle().set("box-sizing", "border-box");
	}

	/** Initialize the cell with default configuration. */
	private void initializeCell() {
		applyDefaultStyling();
	}

	/** Check if auto-contrast is enabled for color-aware cells.
	 * @return true if auto-contrast is enabled */
	public boolean isAutoContrast() { return autoContrast; }

	/** Check if icon display is enabled.
	 * @return true if icons are displayed alongside text */
	public boolean isShowIcon() { return showIcon; }

	/** Enable or disable auto-contrast for text color in color-aware cells.
	 * @param autoContrast true to enable auto-contrast */
	public void setAutoContrast(final Boolean autoContrast) {
		this.autoContrast = autoContrast != null ? autoContrast : Boolean.TRUE;
	}

	/** Set boolean value with custom styling for true/false states. This method provides consistent styling for boolean-based grid cells.
	 * @param value      the boolean value to display
	 * @param trueText   text to display when value is true
	 * @param falseText  text to display when value is false (can be empty)
	 * @param trueColor  background color for true state
	 * @param falseColor background color for false state (can be null for no background) */
	public void setBooleanValue(final boolean value, final String trueText, final String falseText, final String trueColor, final String falseColor) {
		removeAll(); // Clear any existing content
		final String displayText = value ? trueText : falseText;
		// Only show content if there's text to display
		if ((displayText != null) && !displayText.isBlank()) {
			setText(displayText);
			// Apply boolean-specific styling
			getStyle().set("padding", "4px 8px");
			getStyle().set("border-radius", "12px");
			getStyle().set("font-size", "12px");
			getStyle().set("font-weight", "bold");
			getStyle().set("text-align", "center");
			// Apply colors based on the boolean value
			if (value && (trueColor != null)) {
				getStyle().set("background-color", trueColor);
				if (Boolean.TRUE.equals(autoContrast)) {
					final String textColor = CColorUtils.getContrastTextColor(trueColor);
					getStyle().set("color", textColor);
				}
			} else if (!value && (falseColor != null)) {
				getStyle().set("background-color", falseColor);
				if (Boolean.TRUE.equals(autoContrast)) {
					final String textColor = CColorUtils.getContrastTextColor(falseColor);
					getStyle().set("color", textColor);
				}
			}
		}
	}

	/** Set boolean value with predefined Default styling. This is a convenience method for default status indicators.
	 * @param isDefault true if this is the default item */
	public void setDefaultValue(final boolean isDefault) {
		if (isDefault) {
			setBooleanValue(true, "Default", "", "#e3f2fd", null);
			getStyle().set("color", "#1976d2");
		} else {
			// For non-default, show nothing
			removeAll();
			setText("");
		}
	}

	/** Set entity value and display text with optional color rendering.
	 * @param entity the entity to display */
	public void setEntityValue(final CEntityDB<?> entity) {
		removeAll(); // Clear any existing content
		if (entity == null) {
			setText("N/A");
			getStyle().set("color", "#666666");
			getStyle().set("font-style", "italic");
			return;
		}
		try {
			// Get display text
			final String displayText = CColorUtils.getDisplayTextFromEntity(entity);
			// Check if entity has color for background rendering
			final String color = CColorUtils.getColorFromEntity(entity);
			if ((color != null) && !color.isBlank()) {
				// Color-aware rendering
				applyColorStyling(entity, color, displayText);
			} else {
				// Regular text rendering
				setText(displayText);
				// Reset any color styling
				getStyle().remove("background-color");
				getStyle().remove("color");
				getStyle().remove("font-style");
			}
		} catch (final Exception e) {
			LOGGER.error("Error setting entity value: {}", e.getMessage());
			setText("Error");
			getStyle().set("color", "#dc3545");
		}
	}

	/** Set boolean value with predefined Final/Active styling. This is a convenience method for common Final/Active status display.
	 * @param isFinal true if the status is final, false if active */
	public void setFinalActiveValue(final boolean isFinal) {
		setBooleanValue(isFinal, "Final", "Active", "#ffebee", "#e8f5e8");
		// Apply specific text colors for Final/Active
		if (isFinal) {
			getStyle().set("color", "#c62828");
		} else {
			getStyle().set("color", "#2e7d32");
		}
	}

	/** Enable or disable icon display.
	 * @param showIcon true to show icons alongside text */
	public void setShowIcon(final Boolean showIcon) {
		this.showIcon = showIcon != null ? showIcon : Boolean.FALSE;
	}

	/** Set status entity value with color-aware rendering and optional icon display. This method provides the same functionality as the removed
	 * CGridCellStatus.
	 * @param statusEntity the status entity to display */
	public void setStatusValue(final CEntityDB<?> statusEntity) {
		removeAll(); // Clear any existing content
		if (statusEntity == null) {
			setText("No Status");
			applyDefaultStatusStyling();
			return;
		}
		try {
			final String displayText = CColorUtils.getDisplayTextFromEntity(statusEntity);
			final String color = CColorUtils.getColorFromEntity(statusEntity);
			// Apply color-aware styling with status-specific enhancements
			applyColorStyling(statusEntity, color, displayText);
			// Add status-specific styling
			getStyle().set("border-radius", "4px");
			getStyle().set("font-weight", "500");
		} catch (final Exception e) {
			LOGGER.error("Error applying color to status cell: {}", e.getMessage());
			setEntityValue(statusEntity);
			applyDefaultStatusStyling();
		}
	}
}
