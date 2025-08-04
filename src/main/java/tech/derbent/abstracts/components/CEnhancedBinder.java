package tech.derbent.abstracts.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;

/**
 * Enhanced binder that provides detailed field-level error logging and reporting
 * for better debugging of binding and validation issues.
 * 
 * This class extends BeanValidationBinder to add:
 * - Detailed field-specific error logging
 * - Enhanced error reporting with field names and validation messages
 * - Easy integration with existing code with minimal changes
 * - Backward compatibility with standard BeanValidationBinder usage
 * 
 * @param <BEAN> the bean type
 */
public class CEnhancedBinder<BEAN> extends BeanValidationBinder<BEAN> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CEnhancedBinder.class);
    
    private final Class<BEAN> beanType;
    private boolean detailedLoggingEnabled = true;
    private final Map<String, String> lastValidationErrors = new HashMap<>();
    
    /**
     * Creates an enhanced binder for the given bean type.
     * 
     * @param beanType the bean class, not null
     */
    public CEnhancedBinder(Class<BEAN> beanType) {
        super(beanType);
        this.beanType = beanType;
        LOGGER.debug("Created CEnhancedBinder for bean type: {}", beanType.getSimpleName());
    }
    
    /**
     * Creates an enhanced binder for the given bean type with detailed logging option.
     * 
     * @param beanType the bean class, not null
     * @param detailedLoggingEnabled whether to enable detailed field-level logging
     */
    public CEnhancedBinder(Class<BEAN> beanType, boolean detailedLoggingEnabled) {
        super(beanType);
        this.beanType = beanType;
        this.detailedLoggingEnabled = detailedLoggingEnabled;
        LOGGER.debug("Created CEnhancedBinder for bean type: {} with detailed logging: {}", 
            beanType.getSimpleName(), detailedLoggingEnabled);
    }
    
    @Override
    public void writeBean(BEAN bean) throws ValidationException {
        clearLastValidationErrors();
        
        if (detailedLoggingEnabled) {
            LOGGER.debug("Starting writeBean operation for bean type: {}", beanType.getSimpleName());
        }
        
        try {
            // Validate all bindings first to collect detailed error information
            List<BindingValidationStatus<?>> validationStatuses = validate().getFieldValidationStatuses();
            
            // Check for validation errors and log detailed information
            List<BindingValidationStatus<?>> errorStatuses = validationStatuses.stream()
                .filter(status -> status.isError())
                .collect(Collectors.toList());
                
            if (!errorStatuses.isEmpty()) {
                logDetailedValidationErrors(errorStatuses);
                collectValidationErrors(errorStatuses);
            }
            
            // Call the parent writeBean method
            super.writeBean(bean);
            
            if (detailedLoggingEnabled) {
                LOGGER.info("Successfully completed writeBean operation for bean type: {}", 
                    beanType.getSimpleName());
            }
            
        } catch (ValidationException e) {
            if (detailedLoggingEnabled) {
                LOGGER.error("ValidationException during writeBean for bean type: {}. Error details: {}", 
                    beanType.getSimpleName(), e.getMessage());
                logDetailedBindingErrors();
            }
            throw e;
        } catch (Exception e) {
            if (detailedLoggingEnabled) {
                LOGGER.error("Unexpected error during writeBean for bean type: {}", 
                    beanType.getSimpleName(), e);
            }
            throw e;
        }
    }
    
    @Override
    public void readBean(BEAN bean) {
        clearLastValidationErrors();
        
        if (detailedLoggingEnabled) {
            LOGGER.debug("Starting readBean operation for bean type: {}", beanType.getSimpleName());
        }
        
        try {
            super.readBean(bean);
            
            if (detailedLoggingEnabled) {
                LOGGER.debug("Successfully completed readBean operation for bean type: {}", 
                    beanType.getSimpleName());
            }
            
        } catch (Exception e) {
            if (detailedLoggingEnabled) {
                LOGGER.error("Error during readBean for bean type: {}", beanType.getSimpleName(), e);
            }
            throw e;
        }
    }
    
    /**
     * Gets detailed validation error information from the last validation attempt.
     * 
     * @return map of field names to error messages
     */
    public Map<String, String> getLastValidationErrors() {
        return new HashMap<>(lastValidationErrors);
    }
    
    /**
     * Gets a formatted error summary for display purposes.
     * 
     * @return formatted string with all field validation errors
     */
    public String getFormattedErrorSummary() {
        if (lastValidationErrors.isEmpty()) {
            return "No validation errors found.";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Validation errors found in ").append(lastValidationErrors.size()).append(" field(s):\n");
        
        lastValidationErrors.forEach((field, error) -> 
            summary.append("• ").append(field).append(": ").append(error).append("\n"));
            
        return summary.toString();
    }
    
    /**
     * Checks if there are any field validation errors.
     * 
     * @return true if there are validation errors
     */
    public boolean hasValidationErrors() {
        return !lastValidationErrors.isEmpty();
    }
    
    /**
     * Enables or disables detailed logging.
     * 
     * @param enabled true to enable detailed logging
     */
    public void setDetailedLoggingEnabled(boolean enabled) {
        this.detailedLoggingEnabled = enabled;
        LOGGER.debug("Detailed logging {} for bean type: {}", 
            enabled ? "enabled" : "disabled", beanType.getSimpleName());
    }
    
    /**
     * Checks if detailed logging is enabled.
     * 
     * @return true if detailed logging is enabled
     */
    public boolean isDetailedLoggingEnabled() {
        return detailedLoggingEnabled;
    }
    
    /**
     * Gets a list of field names that have validation errors.
     * 
     * @return list of field names with errors
     */
    public List<String> getFieldsWithErrors() {
        return new ArrayList<>(lastValidationErrors.keySet());
    }
    
    /**
     * Gets the validation error for a specific field.
     * 
     * @param fieldName the field name
     * @return the error message for the field, or null if no error
     */
    public String getFieldError(String fieldName) {
        return lastValidationErrors.get(fieldName);
    }
    
    private void logDetailedValidationErrors(List<BindingValidationStatus<?>> errorStatuses) {
        LOGGER.error("Found {} validation error(s) for bean type: {}", 
            errorStatuses.size(), beanType.getSimpleName());
            
        for (BindingValidationStatus<?> status : errorStatuses) {
            String fieldName = getFieldNameFromBinding(status);
            List<ValidationResult> results = status.getValidationResults();
            
            LOGGER.error("Field '{}' validation failed:", fieldName);
            for (ValidationResult result : results) {
                if (result.isError()) {
                    LOGGER.error("  → Error: {}", result.getErrorMessage());
                }
            }
        }
    }
    
    private void collectValidationErrors(List<BindingValidationStatus<?>> errorStatuses) {
        for (BindingValidationStatus<?> status : errorStatuses) {
            String fieldName = getFieldNameFromBinding(status);
            List<String> errorMessages = status.getValidationResults().stream()
                .filter(ValidationResult::isError)
                .map(ValidationResult::getErrorMessage)
                .collect(Collectors.toList());
                
            if (!errorMessages.isEmpty()) {
                lastValidationErrors.put(fieldName, String.join("; ", errorMessages));
            }
        }
    }
    
    private void logDetailedBindingErrors() {
        if (!lastValidationErrors.isEmpty()) {
            LOGGER.error("Detailed field validation errors for bean type: {}", beanType.getSimpleName());
            lastValidationErrors.forEach((field, error) -> 
                LOGGER.error("  Field '{}': {}", field, error));
        }
    }
    
    private String getFieldNameFromBinding(BindingValidationStatus<?> status) {
        try {
            // Try to extract field name from the binding
            if (status.getBinding() != null) {
                // Extract field name from binding toString or use reflection
                String bindingStr = status.getBinding().toString();
                // Try to extract property name from binding string representation
                if (bindingStr.contains("property=")) {
                    int start = bindingStr.indexOf("property=") + 9;
                    int end = bindingStr.indexOf(",", start);
                    if (end == -1) end = bindingStr.indexOf("}", start);
                    if (end > start) {
                        return bindingStr.substring(start, end).trim();
                    }
                }
                return bindingStr;
            }
        } catch (Exception e) {
            LOGGER.debug("Could not extract field name from binding: {}", e.getMessage());
        }
        return "unknown_field";
    }
    
    private void clearLastValidationErrors() {
        lastValidationErrors.clear();
    }
}