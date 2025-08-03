package tech.derbent.abstracts.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.function.ValueProvider;

import tech.derbent.abstracts.annotations.ColorAwareGrid;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.utils.CColorUtils;
import tech.derbent.abstracts.views.CGrid;

/**
 * CColorAwareGrid - Specialized Grid superclass for entities with color-aware status column rendering.
 * 
 * <p>
 * This class extends the CGrid base class to provide automatic color rendering for status columns.
 * It detects status entities and renders them with colored backgrounds based on their color properties.
 * </p>
 * 
 * <p>
 * The class follows the project's coding guidelines by providing a reusable superclass for all
 * color-aware Grid components, ensuring consistent styling and behavior across the application.
 * </p>
 * 
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 * 
 * <pre>{@code
 * CColorAwareGrid<CDecision> grid = new CColorAwareGrid<>(CDecision.class);
 * grid.addColorAwareStatusColumn(CDecision::getStatus, "Status", "status");
 * }</pre>
 * 
 * @param <T> the entity type that extends CEntityDB
 * 
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.annotations.ColorAwareGrid
 * @see tech.derbent.abstracts.utils.CColorUtils
 * @see tech.derbent.abstracts.views.CGrid
 */
public class CColorAwareGrid<T extends CEntityDB<T>> extends CGrid<T> {
    
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CColorAwareGrid.class);
    
    // Default styling configuration
    private boolean roundedCorners = true;
    private String padding = "4px 8px";
    private boolean autoContrast = true;
    private String minWidth = "80px";
    private boolean centerAlign = true;
    private String fontWeight = "500";
    
    /**
     * Constructor for CColorAwareGrid with entity class.
     * 
     * @param beanType the entity class for the grid
     */
    public CColorAwareGrid(final Class<T> beanType) {
        super(beanType);
        LOGGER.info("CColorAwareGrid constructor called for entity type: {}", beanType.getSimpleName());
    }
    
    /**
     * Constructor for CColorAwareGrid with entity class and automatic column creation.
     * 
     * @param beanType the entity class for the grid
     * @param autoCreateColumns whether to automatically create columns
     */
    public CColorAwareGrid(final Class<T> beanType, final boolean autoCreateColumns) {
        super(beanType, autoCreateColumns);
        LOGGER.info("CColorAwareGrid constructor called for entity type: {} with autoCreateColumns: {}", 
                    beanType.getSimpleName(), autoCreateColumns);
    }
    
    /**
     * Adds a color-aware status column with enhanced rendering.
     * This method creates a column that displays status entities with their associated colors as background.
     * 
     * @param <S> the status entity type
     * @param statusProvider Provider that returns the status entity
     * @param header Column header text
     * @param key Column key for identification
     * @return The created column
     */
    public <S extends CEntityDB<S>> Column<T> addColorAwareStatusColumn(final ValueProvider<T, S> statusProvider, 
            final String header, final String key) {
        return addColorAwareStatusColumn(statusProvider, header, key, null);
    }
    
    /**
     * Adds a color-aware status column with annotation-based configuration.
     * This method creates a column that displays status entities with their associated colors as background.
     * 
     * @param <S> the status entity type
     * @param statusProvider Provider that returns the status entity
     * @param header Column header text
     * @param key Column key for identification
     * @param annotation Optional annotation for styling configuration
     * @return The created column
     */
    public <S extends CEntityDB<S>> Column<T> addColorAwareStatusColumn(final ValueProvider<T, S> statusProvider, 
            final String header, final String key, final ColorAwareGrid annotation) {
        LOGGER.info("Adding color-aware status column: {} with enhanced color rendering", header);
        
        // Configure styling from annotation if provided
        if (annotation != null) {
            this.roundedCorners = annotation.roundedCorners();
            this.padding = annotation.padding();
            this.autoContrast = annotation.autoContrast();
            this.minWidth = annotation.minWidth();
            this.centerAlign = annotation.centerAlign();
            this.fontWeight = annotation.fontWeight();
        }
        
        final Column<T> column = addComponentColumn(entity -> {
            final S status = statusProvider.apply(entity);
            final Span span = new Span();
            
            if (status == null) {
                span.setText("No Status");
                span.getStyle().set("color", "#666666");
                span.getStyle().set("font-style", "italic");
                return span;
            }
            
            // Set the text content
            final String displayText = CColorUtils.getDisplayTextFromEntity(status);
            span.setText(displayText);
            
            // Apply background color styling
            applyColorStyling(span, status);
            
            return span;
        }).setHeader(header).setWidth(WIDTH_REFERENCE).setFlexGrow(0).setSortable(true);
        
        if (key != null) {
            column.setKey(key);
        }
        
        return column;
    }
    
    /**
     * Applies color styling to a span component based on the status entity.
     * 
     * @param span the span component to style
     * @param status the status entity to extract color from
     */
    private void applyColorStyling(final Span span, final CEntityDB<?> status) {
        try {
            final String color = CColorUtils.getColorWithFallback(status, CColorUtils.DEFAULT_COLOR);
            
            span.getStyle().set("background-color", color);
            
            if (autoContrast) {
                span.getStyle().set("color", CColorUtils.getContrastTextColor(color));
            }
            
            span.getStyle().set("padding", padding);
            span.getStyle().set("display", "inline-block");
            span.getStyle().set("min-width", minWidth);
            
            if (centerAlign) {
                span.getStyle().set("text-align", "center");
            }
            
            if (fontWeight != null && !fontWeight.trim().isEmpty()) {
                span.getStyle().set("font-weight", fontWeight);
            }
            
            if (roundedCorners) {
                span.getStyle().set("border-radius", "4px");
            }
            
            LOGGER.debug("Applied color {} to grid status cell: {}", color, span.getText());
            
        } catch (final Exception e) {
            LOGGER.warn("Error applying color to grid status cell: {}", e.getMessage());
            // Apply default styling for status without color
            applyDefaultStyling(span);
        }
    }
    
    /**
     * Applies default styling to a span component when color is not available.
     * 
     * @param span the span component to style
     */
    private void applyDefaultStyling(final Span span) {
        span.getStyle().set("background-color", "#f8f9fa");
        span.getStyle().set("color", "#495057");
        span.getStyle().set("padding", padding);
        span.getStyle().set("display", "inline-block");
        span.getStyle().set("min-width", minWidth);
        span.getStyle().set("border", "1px solid #dee2e6");
        
        if (centerAlign) {
            span.getStyle().set("text-align", "center");
        }
        
        if (roundedCorners) {
            span.getStyle().set("border-radius", "4px");
        }
    }
    
    /**
     * Legacy method for backward compatibility.
     * Delegates to the new addColorAwareStatusColumn method.
     * 
     * @param <S> the status entity type
     * @param statusProvider Provider that returns the status entity
     * @param header Column header text
     * @param key Column key for identification
     * @return The created column
     */
    @Override
    public <S extends CEntityDB<S>> Column<T> addStatusColumn(final ValueProvider<T, S> statusProvider, 
            final String header, final String key) {
        LOGGER.debug("Legacy addStatusColumn called, delegating to addColorAwareStatusColumn");
        return addColorAwareStatusColumn(statusProvider, header, key);
    }
    
    // Getter and setter methods for styling configuration
    
    public boolean isRoundedCorners() {
        return roundedCorners;
    }
    
    public void setRoundedCorners(final boolean roundedCorners) {
        this.roundedCorners = roundedCorners;
    }
    
    public String getPadding() {
        return padding;
    }
    
    public void setPadding(final String padding) {
        this.padding = padding;
    }
    
    public boolean isAutoContrast() {
        return autoContrast;
    }
    
    public void setAutoContrast(final boolean autoContrast) {
        this.autoContrast = autoContrast;
    }
    
    public String getMinWidth() {
        return minWidth;
    }
    
    public void setMinWidth(final String minWidth) {
        this.minWidth = minWidth;
    }
    
    public boolean isCenterAlign() {
        return centerAlign;
    }
    
    public void setCenterAlign(final boolean centerAlign) {
        this.centerAlign = centerAlign;
    }
    
    public String getFontWeight() {
        return fontWeight;
    }
    
    public void setFontWeight(final String fontWeight) {
        this.fontWeight = fontWeight;
    }
}