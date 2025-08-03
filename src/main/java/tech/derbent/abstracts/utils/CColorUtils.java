package tech.derbent.abstracts.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CTypeEntity;
import tech.derbent.base.domain.CStatus;
import tech.derbent.users.domain.CUser;

/**
 * CColorUtils - Utility class for color operations and status entity color management.
 * 
 * <p>
 * This utility class provides common methods for extracting colors from status entities,
 * calculating contrast text colors, and applying color styling to UI components.
 * </p>
 * 
 * <p>
 * The class follows the project's coding guidelines by centralizing color-related logic
 * and providing reusable methods for color-aware components.
 * </p>
 * 
 * @author Derbent Framework
 * @since 1.0
 */
public final class CColorUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CColorUtils.class);
    
    /** Default color for status entities without color */
    public static final String DEFAULT_COLOR = "#95a5a6";
    
    /** Default text color for light backgrounds */
    public static final String DEFAULT_DARK_TEXT = "black";
    
    /** Default text color for dark backgrounds */
    public static final String DEFAULT_LIGHT_TEXT = "white";
    
    /**
     * Private constructor to prevent instantiation.
     */
    private CColorUtils() {
        // Utility class - no instantiation
    }
    
    /**
     * Checks if an entity type is a status entity (extends CStatus or CTypeEntity).
     * Uses reflection to determine inheritance hierarchy.
     * 
     * @param entityType the entity class to check
     * @return true if the entity is a status entity, false otherwise
     */
    public static boolean isStatusEntity(final Class<?> entityType) {
        if (entityType == null) {
            return false;
        }
        
        try {
            // Check if the class extends CStatus
            if (CStatus.class.isAssignableFrom(entityType)) {
                return true;
            }
            
            // Check if the class extends CTypeEntity (which status entities inherit from)
            if (CTypeEntity.class.isAssignableFrom(entityType)) {
                return true;
            }
            
            // Also check by class name pattern for status entities
            final String className = entityType.getSimpleName();
            if (className.endsWith("Status") || className.contains("Status")) {
                return true;
            }
            
            return false;
        } catch (final Exception e) {
            LOGGER.warn("Error checking if entity type {} is a status entity: {}", 
                       entityType.getSimpleName(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Extracts color from an entity using reflection.
     * Supports entities that extend CTypeEntity (which have getColor() method).
     * 
     * @param entity the entity instance to extract color from
     * @return the color string or null if not available
     */
    public static String getColorFromEntity(final Object entity) {
        if (entity == null) {
            return null;
        }
        
        try {
            // If entity extends CTypeEntity, it should have getColor() method
            if (entity instanceof CTypeEntity) {
                final CTypeEntity<?> typeEntity = (CTypeEntity<?>) entity;
                return typeEntity.getColor();
            }
            
            // Fallback: try to get color using reflection
            try {
                final java.lang.reflect.Method getColorMethod = entity.getClass().getMethod("getColor");
                final Object colorValue = getColorMethod.invoke(entity);
                return colorValue != null ? colorValue.toString() : null;
            } catch (final NoSuchMethodException e) {
                // This is expected for entities that don't have getColor method
                LOGGER.debug("Entity type {} does not have getColor method", entity.getClass().getSimpleName());
                return null;
            }
        } catch (final Exception e) {
            LOGGER.warn("Error extracting color from entity {}: {}", 
                       entity.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets the display text for an entity, preferring getName() for CEntityNamed entities.
     * 
     * @param entity the entity to get display text for
     * @return the display text or "Unknown" if not available
     */
    public static String getDisplayTextFromEntity(final Object entity) {
        if (entity == null) {
            return "N/A";
        }
        
        try {
            if (entity instanceof CEntityNamed) {
                final CEntityNamed<?> namedEntity = (CEntityNamed<?>) entity;
                final String name = namedEntity.getName();
                return ((name != null) && !name.trim().isEmpty())
                        ? name
                        : "Unnamed " + entity.getClass().getSimpleName() + " #" + namedEntity.getId();
            } else {
                return entity.toString();
            }
        } catch (final Exception e) {
            LOGGER.warn("Error generating display text for entity: {}", e.getMessage());
            return "Error: " + entity.getClass().getSimpleName();
        }
    }
    
    /**
     * Determines appropriate text color based on background color.
     * Uses a simple brightness calculation to determine if white or black text would be more readable.
     * 
     * @param backgroundColor the background color in hex format (e.g., "#FF0000")
     * @return "white" for dark backgrounds, "black" for light backgrounds
     */
    public static String getContrastTextColor(final String backgroundColor) {
        if ((backgroundColor == null) || backgroundColor.trim().isEmpty()) {
            return DEFAULT_DARK_TEXT; // Default to black text
        }
        
        try {
            String color = backgroundColor.trim();
            // Remove # if present
            if (color.startsWith("#")) {
                color = color.substring(1);
            }
            
            // Parse RGB values
            if (color.length() == 6) {
                final int r = Integer.parseInt(color.substring(0, 2), 16);
                final int g = Integer.parseInt(color.substring(2, 4), 16);
                final int b = Integer.parseInt(color.substring(4, 6), 16);
                
                // Calculate brightness using relative luminance formula
                final double brightness = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
                
                // Return white text for dark backgrounds, black text for light backgrounds
                return brightness < 0.5 ? DEFAULT_LIGHT_TEXT : DEFAULT_DARK_TEXT;
            }
        } catch (final Exception e) {
            LOGGER.debug("Error calculating contrast color for background {}: {}", backgroundColor, e.getMessage());
        }
        
        return DEFAULT_DARK_TEXT; // Default fallback
    }
    
    /**
     * Normalizes a color string to ensure it starts with #.
     * 
     * @param color the color string to normalize
     * @return the normalized color string with # prefix, or null if input is invalid
     */
    public static String normalizeColor(final String color) {
        if ((color == null) || color.trim().isEmpty()) {
            return null;
        }
        
        String normalizedColor = color.trim();
        if (!normalizedColor.startsWith("#")) {
            normalizedColor = "#" + normalizedColor;
        }
        
        // Validate hex color format
        if (normalizedColor.length() == 7 && normalizedColor.matches("#[0-9A-Fa-f]{6}")) {
            return normalizedColor;
        }
        
        LOGGER.warn("Invalid color format: {}", color);
        return null;
    }
    
    /**
     * Gets a color from an entity, with fallback to default color.
     * 
     * @param entity the entity to extract color from
     * @param defaultColor the default color to use if no color is found
     * @return the color string, guaranteed to be non-null
     */
    public static String getColorWithFallback(final Object entity, final String defaultColor) {
        String color = getColorFromEntity(entity);
        if (color != null) {
            color = normalizeColor(color);
            if (color != null) {
                return color;
            }
        }
        return defaultColor != null ? defaultColor : DEFAULT_COLOR;
    }
    
    /**
     * Gets an appropriate icon for an entity based on its type.
     * Returns null if no suitable icon is found.
     * 
     * @param entity the entity to get an icon for
     * @return the VaadinIcon for the entity type, or null if no icon is appropriate
     */
    public static VaadinIcon getIconForEntity(final Object entity) {
        if (entity == null) {
            return null;
        }
        
        try {
            // Check for user entities
            if (entity instanceof CUser) {
                return VaadinIcon.USER;
            }
            
            // Check by class name patterns for entities that should have icons
            final String className = entity.getClass().getSimpleName();
            
            // User-related entities
            if (className.contains("User") || className.contains("user")) {
                return VaadinIcon.USER;
            }
            
            // Company-related entities
            if (className.contains("Company") || className.contains("company")) {
                return VaadinIcon.BUILDING;
            }
            
            // Project-related entities
            if (className.contains("Project") || className.contains("project")) {
                return VaadinIcon.FOLDER;
            }
            
            // Meeting-related entities
            if (className.contains("Meeting") || className.contains("meeting")) {
                return VaadinIcon.CALENDAR;
            }
            
            // Activity-related entities
            if (className.contains("Activity") || className.contains("activity")) {
                return VaadinIcon.TASKS;
            }
            
            // Decision-related entities
            if (className.contains("Decision") || className.contains("decision")) {
                return VaadinIcon.CHECK_CIRCLE;
            }
            
            // Comment-related entities
            if (className.contains("Comment") || className.contains("comment")) {
                return VaadinIcon.COMMENT;
            }
            
            // No suitable icon found
            return null;
            
        } catch (final Exception e) {
            LOGGER.warn("Error determining icon for entity {}: {}", 
                       entity.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Checks if an entity should have an icon displayed.
     * 
     * @param entity the entity to check
     * @return true if the entity should display an icon, false otherwise
     */
    public static boolean shouldDisplayIcon(final Object entity) {
        return getIconForEntity(entity) != null;
    }
    
    /**
     * Creates a Vaadin Icon component for an entity if appropriate.
     * 
     * @param entity the entity to create an icon for
     * @return the Icon component or null if no icon is appropriate
     */
    public static Icon createIconForEntity(final Object entity) {
        final VaadinIcon iconType = getIconForEntity(entity);
        return iconType != null ? iconType.create() : null;
    }

    /**
     * Applies color styling to a component using CSS styles.
     * This is a helper method that can be used with any component that has getStyle() method.
     * 
     * @param component the component to style (must have getStyle() method)
     * @param backgroundColor the background color
     * @param autoContrast whether to automatically calculate text color
     * @param padding the padding to apply
     * @param borderRadius the border radius to apply
     * @param minWidth the minimum width to apply
     */
    public static void applyColorStyling(final Object component, final String backgroundColor, 
            final boolean autoContrast, final String padding, final String borderRadius, final String minWidth) {
        if (component == null || backgroundColor == null) {
            return;
        }
        
        try {
            // Use reflection to get the style property
            final java.lang.reflect.Method getStyleMethod = component.getClass().getMethod("getStyle");
            final Object style = getStyleMethod.invoke(component);
            final java.lang.reflect.Method setMethod = style.getClass().getMethod("set", String.class, String.class);
            
            setMethod.invoke(style, "background-color", backgroundColor);
            
            if (autoContrast) {
                setMethod.invoke(style, "color", getContrastTextColor(backgroundColor));
            }
            
            if (padding != null && !padding.trim().isEmpty()) {
                setMethod.invoke(style, "padding", padding);
            }
            
            if (borderRadius != null && !borderRadius.trim().isEmpty()) {
                setMethod.invoke(style, "border-radius", borderRadius);
            }
            
            if (minWidth != null && !minWidth.trim().isEmpty()) {
                setMethod.invoke(style, "min-width", minWidth);
            }
            
            setMethod.invoke(style, "display", "inline-block");
            
        } catch (final Exception e) {
            LOGGER.warn("Error applying color styling to component: {}", e.getMessage());
        }
    }
}