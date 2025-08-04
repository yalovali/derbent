package tech.derbent.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.utils.CValidationUtils;
import tech.derbent.meetings.domain.CMeetingStatus;

/**
 * Demo class showing the usage of enhanced binder with better error logging.
 * 
 * This example demonstrates:
 * - Creating enhanced binders
 * - Detailed field-level error reporting
 * - Integration with existing form builder
 * - Minimal impact on existing code
 */
public class CEnhancedBinderDemo extends VerticalLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(CEnhancedBinderDemo.class);
    
    private CEnhancedBinder<CMeetingStatus> enhancedBinder;
    private Pre logOutput;
    
    public CEnhancedBinderDemo() {
        setSpacing(true);
        setPadding(true);
        
        add(new H3("Enhanced Binder Demo - Better Error Logging"));
        
        createDemo();
    }
    
    private void createDemo() {
        // Demo 1: Standard vs Enhanced Binder Creation
        add(createStandardVsEnhancedDemo());
        
        // Demo 2: Enhanced Form with Detailed Error Reporting
        add(createEnhancedFormDemo());
        
        // Demo 3: Validation Error Handling
        add(createValidationErrorDemo());
        
        // Log output area
        logOutput = new Pre();
        logOutput.getStyle()
            .set("background-color", "#f5f5f5")
            .set("padding", "10px")
            .set("border", "1px solid #ddd")
            .set("max-height", "300px")
            .set("overflow-y", "auto");
        add(new H3("Log Output"), logOutput);
    }
    
    private Div createStandardVsEnhancedDemo() {
        Div demoSection = new Div();
        demoSection.add(new H3("Demo 1: Standard vs Enhanced Binder Creation"));
        
        Button standardButton = new Button("Create Standard Binder", e -> {
            logToOutput("=== Creating Standard Binder ===");
            var standardBinder = CBinderFactory.createStandardBinder(CMeetingStatus.class);
            logToOutput("Created: " + standardBinder.getClass().getSimpleName());
            logToOutput("Enhanced features: " + (CBinderFactory.isEnhancedBinder(standardBinder) ? "Available" : "Not Available"));
        });
        
        Button enhancedButton = new Button("Create Enhanced Binder", e -> {
            logToOutput("=== Creating Enhanced Binder ===");
            enhancedBinder = CBinderFactory.createEnhancedBinder(CMeetingStatus.class);
            logToOutput("Created: " + enhancedBinder.getClass().getSimpleName());
            logToOutput("Enhanced features: Available");
            logToOutput("Detailed logging: " + (enhancedBinder.isDetailedLoggingEnabled() ? "Enabled" : "Disabled"));
        });
        
        demoSection.add(standardButton, enhancedButton);
        return demoSection;
    }
    
    private Div createEnhancedFormDemo() {
        Div demoSection = new Div();
        demoSection.add(new H3("Demo 2: Enhanced Form Building"));
        
        Button buildFormButton = new Button("Build Enhanced Form", e -> {
            logToOutput("=== Building Enhanced Form ===");
            
            // Create enhanced binder
            enhancedBinder = CEntityFormBuilder.createEnhancedBinder(CMeetingStatus.class);
            logToOutput("Enhanced binder created for: " + CMeetingStatus.class.getSimpleName());
            
            // Build form using enhanced binder
            Div formLayout = CEntityFormBuilder.buildForm(CMeetingStatus.class, enhancedBinder);
            logToOutput("Form built with enhanced binder");
            logToOutput("Form components created: " + formLayout.getChildren().count());
            
            // Alternative: Build form directly with enhanced features
            Div enhancedForm = CEntityFormBuilder.buildEnhancedForm(CMeetingStatus.class);
            logToOutput("Direct enhanced form created with components: " + enhancedForm.getChildren().count());
        });
        
        demoSection.add(buildFormButton);
        return demoSection;
    }
    
    private Div createValidationErrorDemo() {
        Div demoSection = new Div();
        demoSection.add(new H3("Demo 3: Detailed Validation Error Reporting"));
        
        Button triggerErrorsButton = new Button("Trigger Validation Errors", e -> {
            if (enhancedBinder == null) {
                logToOutput("Please create enhanced binder first (Demo 1)");
                return;
            }
            
            logToOutput("=== Triggering Validation Errors ===");
            
            // Create invalid entity to trigger validation errors
            CMeetingStatus invalidEntity = new CMeetingStatus();
            // Leave required fields empty to trigger validation errors
            
            try {
                enhancedBinder.writeBean(invalidEntity);
                logToOutput("No validation errors found");
            } catch (ValidationException ex) {
                logToOutput("ValidationException caught: " + ex.getMessage());
                
                // Show enhanced error reporting
                if (enhancedBinder.hasValidationErrors()) {
                    logToOutput("=== Enhanced Error Details ===");
                    logToOutput("Fields with errors: " + enhancedBinder.getFieldsWithErrors());
                    logToOutput("Error summary:");
                    logToOutput(enhancedBinder.getFormattedErrorSummary());
                    
                    // Individual field errors
                    enhancedBinder.getLastValidationErrors().forEach((field, error) -> {
                        logToOutput("Field '" + field + "': " + error);
                    });
                }
            }
        });
        
        Button testValidationUtilsButton = new Button("Test Validation Utils", e -> {
            if (enhancedBinder == null) {
                logToOutput("Please create enhanced binder first (Demo 1)");
                return;
            }
            
            logToOutput("=== Testing Validation Utils ===");
            
            CMeetingStatus testEntity = new CMeetingStatus();
            
            // Test validation without exception
            boolean isValid = CValidationUtils.validateBean(enhancedBinder, testEntity);
            logToOutput("Bean validation result: " + (isValid ? "Valid" : "Invalid"));
            
            if (!isValid) {
                // Use validation utils for error handling
                CValidationUtils.logDetailedErrors(enhancedBinder, "CMeetingStatus");
                
                String errorSummary = CValidationUtils.getValidationErrorSummary(enhancedBinder);
                logToOutput("Validation utils error summary:");
                logToOutput(errorSummary);
            }
        });
        
        demoSection.add(triggerErrorsButton, testValidationUtilsButton);
        return demoSection;
    }
    
    /**
     * Example method showing how to integrate enhanced binder into existing save operations
     * with minimal code changes.
     */
    public void demonstrateIntegrationInSaveOperation() {
        logToOutput("=== Integration Example: Save Operation ===");
        
        if (enhancedBinder == null) {
            enhancedBinder = CEntityFormBuilder.createEnhancedBinder(CMeetingStatus.class);
        }
        
        CMeetingStatus entity = new CMeetingStatus();
        
        try {
            // Standard save operation - no changes needed
            enhancedBinder.writeBean(entity);
            logToOutput("Save successful");
            
        } catch (ValidationException exception) {
            // Enhanced error handling - replaces standard catch block
            CValidationUtils.handleValidationException(enhancedBinder, exception, "Meeting Status");
            
            // Log shows exactly which fields failed and why
            logToOutput("Enhanced error handling provided detailed field information");
        }
    }
    
    /**
     * Example showing how existing code can be minimally modified to use enhanced features.
     */
    public void demonstrateMinimalCodeChanges() {
        logToOutput("=== Minimal Code Changes Example ===");
        
        // OLD CODE (commented):
        // BeanValidationBinder<CMeetingStatus> binder = new BeanValidationBinder<>(CMeetingStatus.class);
        
        // NEW CODE (minimal change):
        var binder = CBinderFactory.createBinder(CMeetingStatus.class); // Factory method
        // OR for explicit enhanced features:
        // var binder = CBinderFactory.createEnhancedBinder(CMeetingStatus.class);
        
        logToOutput("Binder created with factory method");
        logToOutput("Is enhanced: " + CBinderFactory.isEnhancedBinder(binder));
        
        // Rest of the code remains exactly the same!
        Div form = CEntityFormBuilder.buildForm(CMeetingStatus.class, binder);
        logToOutput("Form built - no other code changes needed");
    }
    
    private void logToOutput(String message) {
        LOGGER.info(message);
        String currentText = logOutput.getText();
        logOutput.setText(currentText + message + "\n");
        
        // Auto-scroll to bottom
        getUI().ifPresent(ui -> ui.getPage().executeJs(
            "arguments[0].scrollTop = arguments[0].scrollHeight", 
            logOutput.getElement()));
    }
}