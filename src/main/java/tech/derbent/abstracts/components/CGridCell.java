package tech.derbent.abstracts.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.utils.CColorUtils;

/**
 * CGridCell - Enhanced grid cell component that supports both regular text and color-aware rendering.
 * <p>
 * This class provides a unified foundation for all grid cell components, supporting:
 * - Regular text display
 * - Color-aware status rendering with background colors
 * - Optional icon display for entities
 * - Consistent styling and behavior
 * </p>
 * <p>
 * The class follows the project's coding guidelines by consolidating all cell functionality
 * into a single, reusable component that uses Div for proper layout control.
 * </p>
 * <p>
 * <strong>Usage Examples:</strong>
 * </p>
 *
 * <pre>{@code
 * // Basic text cell
 * CGridCell cell = new CGridCell("Sample Text");
 * 
 * // Color-aware status cell
 * CGridCell statusCell = new CGridCell();
 * statusCell.setStatusValue(statusEntity);
 * 
 * // Entity cell with color metadata
 * CGridCell entityCell = new CGridCell(entity);
 * }</pre>
 *
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.views.CGrid
 * @see tech.derbent.abstracts.utils.CColorUtils
 */
public class CGridCell extends Div {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CGridCell.class);

	// Simplified styling configuration - only essential properties
	private boolean showIcon = false;
	private boolean autoContrast = true;

	/**
	 * Default constructor for CGridCell.
	 */
	public CGridCell() {
		super();
		initializeCell();
	}

	/**
	 * Constructor for CGridCell with text content.
	 * @param text the text content for the cell
	 */
	public CGridCell(final String text) {
		super();
		setText(text);
		initializeCell();
	}

	/**
	 * Constructor for CGridCell with entity value.
	 * @param entity the entity to display in the cell
	 */
	public CGridCell(final CEntityDB<?> entity) {
		super();
		setEntityValue(entity);
		initializeCell();
	}

	/**
	 * Apply default styling to the cell.
	 */
	private void applyDefaultStyling() {
		getStyle().set("width", "100%");
		getStyle().set("height", "100%");
		getStyle().set("display", "flex");
		getStyle().set("align-items", "center");
		getStyle().set("padding", "4px 8px");
		getStyle().set("box-sizing", "border-box");
	}

	/**
	 * Initialize the cell with default configuration.
	 */
	private void initializeCell() {
		applyDefaultStyling();
	}

	/**
	 * Check if auto-contrast is enabled for color-aware cells.
	 * @return true if auto-contrast is enabled
	 */
	public boolean isAutoContrast() { 
		return autoContrast; 
	}

	/**
	 * Check if icon display is enabled.
	 * @return true if icons are displayed alongside text
	 */
	public boolean isShowIcon() { 
		return showIcon; 
	}

	/**
	 * Enable or disable auto-contrast for text color in color-aware cells.
	 * @param autoContrast true to enable auto-contrast
	 */
	public void setAutoContrast(final boolean autoContrast) {
		this.autoContrast = autoContrast;
	}

	/**
	 * Set entity value and display text with optional color rendering.
	 * @param entity the entity to display
	 */
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
			
			if (color != null && !color.isBlank()) {
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
			LOGGER.warn("Error setting entity value: {}", e.getMessage());
			setText("Error");
			getStyle().set("color", "#dc3545");
		}
	}

	/**
	 * Enable or disable icon display.
	 * @param showIcon true to show icons alongside text
	 */
	public void setShowIcon(final boolean showIcon) { 
		this.showIcon = showIcon; 
	}

	/**
	 * Set status entity value with color-aware rendering and optional icon display.
	 * This method provides the same functionality as the removed CGridCellStatus.
	 * @param statusEntity the status entity to display
	 */
	public void setStatusValue(final CEntityDB<?> statusEntity) {
		removeAll(); // Clear any existing content
		
		if (statusEntity == null) {
			setText("No Status");
			applyDefaultStatusStyling();
			return;
		}

		try {
			final String displayText = CColorUtils.getDisplayTextFromEntity(statusEntity);
			final String color = CColorUtils.getColorWithFallback(statusEntity, CColorUtils.DEFAULT_COLOR);
			
			// Apply color-aware styling with status-specific enhancements
			applyColorStyling(statusEntity, color, displayText);
			
			// Add status-specific styling
			getStyle().set("border-radius", "4px");
			getStyle().set("font-weight", "500");
			
		} catch (final Exception e) {
			LOGGER.warn("Error applying color to status cell: {}", e.getMessage());
			setEntityValue(statusEntity);
			applyDefaultStatusStyling();
		}
	}

	/**
	 * Apply color-aware styling for entities with colors.
	 * @param entity the entity providing the color
	 * @param color the background color to apply
	 * @param displayText the text to display
	 */
	private void applyColorStyling(final CEntityDB<?> entity, final String color, final String displayText) {
		// Apply background color
		getStyle().set("background-color", color);
		
		// Apply contrasting text color if auto-contrast is enabled
		if (autoContrast) {
			final String textColor = CColorUtils.getContrastTextColor(color);
			getStyle().set("color", textColor);
		}
		
		// Create content with icon if enabled
		if (showIcon && CColorUtils.shouldDisplayIcon(entity)) {
			final Icon icon = CColorUtils.createIconForEntity(entity);
			
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

	/**
	 * Apply default styling for status cells when color is not available.
	 */
	private void applyDefaultStatusStyling() {
		getStyle().set("background-color", "#f8f9fa");
		getStyle().set("color", "#495057");
		getStyle().set("border", "1px solid #dee2e6");
	}
}