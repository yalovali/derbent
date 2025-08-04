package tech.derbent.abstracts.utils;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;

import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.base.ui.dialogs.CWarningDialog;

/**
 * Utility class for handling enhanced validation errors and providing
 * detailed error reporting for form binding operations.
 */
public class CValidationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CValidationUtils.class);
    
    private CValidationUtils() {
        // Utility class - no instantiation
    }
    
    /**
     * Handles validation exceptions with enhanced error reporting.
     * Shows detailed field-level errors if using an enhanced binder.
     * 
     * @param binder the binder that threw the exception
     * @param exception the validation exception
     * @param entityTypeName the name of the entity type for logging
     */
    public static void handleValidationException(BeanValidationBinder<?> binder, 
            ValidationException exception, String entityTypeName) {
        
        LOGGER.error("Validation error for {}: {}", entityTypeName, exception.getMessage());
        
        CEnhancedBinder<?> enhancedBinder = CBinderFactory.asEnhancedBinder(binder);
        if (enhancedBinder != null && enhancedBinder.hasValidationErrors()) {
            // Enhanced error reporting
            showEnhancedValidationDialog(enhancedBinder, entityTypeName);
            logDetailedErrors(enhancedBinder, entityTypeName);
        } else {
            // Standard error reporting
            showStandardValidationDialog();
        }
    }
    
    /**
     * Shows a detailed validation error notification with field-specific information.
     * 
     * @param enhancedBinder the enhanced binder with error details
     * @param entityTypeName the entity type name for context
     */
    public static void showEnhancedValidationDialog(CEnhancedBinder<?> enhancedBinder, String entityTypeName) {
        if (enhancedBinder == null || !enhancedBinder.hasValidationErrors()) {
            return;
        }
        
        StringBuilder message = new StringBuilder();
        message.append("Validation failed for ").append(entityTypeName).append(":\n\n");
        
        Map<String, String> errors = enhancedBinder.getLastValidationErrors();
        errors.forEach((field, error) -> {
            message.append("• ").append(formatFieldName(field)).append(": ").append(error).append("\n");
        });
        
        message.append("\nPlease correct the errors and try again.");
        
        new CWarningDialog(message.toString()).open();
    }
    
    /**
     * Shows enhanced validation errors as a notification.
     * 
     * @param enhancedBinder the enhanced binder with error details
     * @param entityTypeName the entity type name for context
     */
    public static void showEnhancedValidationNotification(CEnhancedBinder<?> enhancedBinder, String entityTypeName) {
        if (enhancedBinder == null || !enhancedBinder.hasValidationErrors()) {
            return;
        }
        
        List<String> fieldsWithErrors = enhancedBinder.getFieldsWithErrors();
        String message = String.format("Validation failed for %s in %d field(s): %s", 
            entityTypeName, fieldsWithErrors.size(), String.join(", ", fieldsWithErrors));
            
        Notification notification = Notification.show(message, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
    
    /**
     * Logs detailed validation errors to the console.
     * 
     * @param enhancedBinder the enhanced binder with error details
     * @param entityTypeName the entity type name for context
     */
    public static void logDetailedErrors(CEnhancedBinder<?> enhancedBinder, String entityTypeName) {
        if (enhancedBinder == null || !enhancedBinder.hasValidationErrors()) {
            return;
        }
        
        LOGGER.error("Detailed validation errors for {}:", entityTypeName);
        LOGGER.error(enhancedBinder.getFormattedErrorSummary());
        
        // Log individual field errors
        Map<String, String> errors = enhancedBinder.getLastValidationErrors();
        errors.forEach((field, error) -> 
            LOGGER.error("Field validation error - {}: {}", formatFieldName(field), error));
    }
    
    /**
     * Shows the standard validation error dialog.
     */
    public static void showStandardValidationDialog() {
        new CWarningDialog(
            "Failed to save the data. Please check that all required fields are filled and values are valid."
        ).open();
    }
    
    /**
     * Validates a bean using an enhanced binder and returns detailed error information.
     * 
     * @param <T> the bean type
     * @param enhancedBinder the enhanced binder
     * @param bean the bean to validate
     * @return true if validation passes, false otherwise
     */
    public static <T> boolean validateBean(CEnhancedBinder<T> enhancedBinder, T bean) {
        try {
            enhancedBinder.writeBean(bean);
            return true;
        } catch (ValidationException e) {
            LOGGER.debug("Validation failed for bean: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets a summary of validation errors from an enhanced binder.
     * 
     * @param enhancedBinder the enhanced binder
     * @return formatted error summary, or empty string if no errors
     */
    public static String getValidationErrorSummary(CEnhancedBinder<?> enhancedBinder) {
        if (enhancedBinder == null || !enhancedBinder.hasValidationErrors()) {
            return "";
        }
        return enhancedBinder.getFormattedErrorSummary();
    }
    
    /**
     * Checks if a binder has validation errors (works with both standard and enhanced binders).
     * 
     * @param binder the binder to check
     * @return true if there are validation errors
     */
    public static boolean hasValidationErrors(BeanValidationBinder<?> binder) {
        if (binder == null) {
            return false;
        }
        
        CEnhancedBinder<?> enhancedBinder = CBinderFactory.asEnhancedBinder(binder);
        if (enhancedBinder != null) {
            return enhancedBinder.hasValidationErrors();
        }
        
        // For standard binders, check validation status
        return !binder.validate().isOk();
    }
    
    /**
     * Formats a field name for display (removes prefixes, camelCase to readable format).
     * 
     * @param fieldName the raw field name
     * @return formatted field name for display
     */
    private static String formatFieldName(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            return "Unknown Field";
        }
        
        // Remove binding prefixes if present
        String cleaned = fieldName;
        if (cleaned.contains(".")) {
            cleaned = cleaned.substring(cleaned.lastIndexOf(".") + 1);
        }
        
        // Convert camelCase to readable format
        return cleaned.replaceAll("([a-z])([A-Z])", "$1 $2")
                     .toLowerCase()
                     .replace("_", " ");
    }
}