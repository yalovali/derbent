package tech.derbent.abstracts.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.utils.CColorUtils;

/**
 * CGridCellStatus - Specialized grid cell component for status entities with color-aware rendering.
 * 
 * <p>
 * This class extends CGridCell to provide automatic color rendering for status entities.
 * It detects status entities and renders them with colored backgrounds based on their color properties.
 * </p>
 * 
 * <p>
 * The class follows the project's coding guidelines by extending the base CGridCell class
 * and incorporating the existing color utility methods for consistent color handling.
 * </p>
 * 
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 * 
 * <pre>{@code
 * CGridCellStatus statusCell = new CGridCellStatus(statusEntity);
 * // Color will be automatically applied based on the status entity's color property
 * }</pre>
 * 
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.components.CGridCell
 * @see tech.derbent.abstracts.utils.CColorUtils
 * @see tech.derbent.abstracts.views.CGrid
 */
public class CGridCellStatus extends CGridCell {

    private static final long serialVersionUID = 1L;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CGridCellStatus.class);
    
    // Status-specific styling configuration
    private boolean autoContrast = true;
    
    /**
     * Default constructor for CGridCellStatus.
     */
    public CGridCellStatus() {
        super();
        LOGGER.debug("CGridCellStatus default constructor called");
        initializeStatusCell();
    }
    
    /**
     * Constructor for CGridCellStatus with status entity.
     * 
     * @param statusEntity the status entity to display with color
     */
    public <S extends CEntityDB<S>> CGridCellStatus(final S statusEntity) {
        super();
        LOGGER.debug("CGridCellStatus constructor called with status entity: {}", statusEntity);
        setStatusValue(statusEntity);
        initializeStatusCell();
    }
    
    /**
     * Initialize the status cell with status-specific configuration.
     */
    private void initializeStatusCell() {
        // Enable rounded corners for status cells by default
        setRoundedCorners(true);
        // Set font weight for better visibility
        setFontWeight("500");
        LOGGER.debug("CGridCellStatus initialized with status-specific styling");
    }
    
    /**
     * Set status entity value with color-aware rendering.
     * 
     * @param statusEntity the status entity to display
     */
    public <S extends CEntityDB<S>> void setStatusValue(final S statusEntity) {
        if (statusEntity == null) {
            setText("No Status");
            applyDefaultStatusStyling();
            LOGGER.debug("Applied default styling for null status");
            return;
        }
        
        try {
            // Set the text content using utility method
            final String displayText = CColorUtils.getDisplayTextFromEntity(statusEntity);
            setText(displayText);
            
            // Apply color-aware styling
            applyStatusColorStyling(statusEntity);
            
            LOGGER.debug("Applied color styling for status: {}", displayText);
            
        } catch (final Exception e) {
            LOGGER.warn("Error applying color to status cell: {}", e.getMessage());
            // Fallback to basic entity display
            setEntityValue(statusEntity);
            applyDefaultStatusStyling();
        }
    }
    
    /**
     * Apply color styling based on the status entity.
     * 
     * @param statusEntity the status entity to extract color from
     */
    private void applyStatusColorStyling(final CEntityDB<?> statusEntity) {
        try {
            final String color = CColorUtils.getColorWithFallback(statusEntity, CColorUtils.DEFAULT_COLOR);
            
            // Apply background color
            getStyle().set("background-color", color);
            
            // Apply contrasting text color if auto-contrast is enabled
            if (autoContrast) {
                final String textColor = CColorUtils.getContrastTextColor(color);
                getStyle().set("color", textColor);
            }
            
            LOGGER.debug("Applied color {} with contrast text to status cell", color);
            
        } catch (final Exception e) {
            LOGGER.warn("Error applying status color styling: {}", e.getMessage());
            applyDefaultStatusStyling();
        }
    }
    
    /**
     * Apply default styling for status cells when color is not available.
     */
    private void applyDefaultStatusStyling() {
        getStyle().set("background-color", "#f8f9fa");
        getStyle().set("color", "#495057");
        getStyle().set("border", "1px solid #dee2e6");
        LOGGER.debug("Applied default status styling");
    }
    
    /**
     * Update the color styling for the current status.
     * This method can be called to refresh the color if the status entity's color has changed.
     */
    public void refreshColorStyling() {
        // Re-apply styling based on current text content
        final String currentText = getText();
        if (currentText != null && !currentText.equals("No Status") && !currentText.equals("N/A")) {
            // Note: We don't have the original entity here, so we can only refresh basic styling
            applyDefaultStyling();
            LOGGER.debug("Refreshed color styling for status cell");
        }
    }
    
    /**
     * Set custom color for the status cell.
     * This method allows manual color override for special cases.
     * 
     * @param backgroundColor the background color
     * @param textColor the text color (optional, will use auto-contrast if null)
     */
    public void setStatusColor(final String backgroundColor, final String textColor) {
        if (backgroundColor != null && !backgroundColor.trim().isEmpty()) {
            getStyle().set("background-color", backgroundColor);
            
            if (textColor != null && !textColor.trim().isEmpty()) {
                getStyle().set("color", textColor);
            } else if (autoContrast) {
                // Calculate contrasting text color
                final String contrastColor = CColorUtils.getContrastTextColor(backgroundColor);
                getStyle().set("color", contrastColor);
            }
            
            LOGGER.debug("Applied custom status color - background: {}, text: {}", backgroundColor, textColor);
        }
    }
    
    /**
     * Check if auto-contrast is enabled.
     * 
     * @return true if auto-contrast is enabled
     */
    public boolean isAutoContrast() {
        return autoContrast;
    }
    
    /**
     * Enable or disable auto-contrast for text color.
     * 
     * @param autoContrast true to enable auto-contrast
     */
    public void setAutoContrast(final boolean autoContrast) {
        this.autoContrast = autoContrast;
        LOGGER.debug("Auto-contrast set to: {}", autoContrast);
    }
}