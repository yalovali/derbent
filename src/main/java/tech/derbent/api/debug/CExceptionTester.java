package tech.derbent.api.debug;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * 🔥 DEBUGGING UTILITY 🔥
 * This component provides methods to trigger exceptions for testing breakpoints.
 * 
 * Usage: Inject this into any view and call triggerTestException()
 */
@Component
public class CExceptionTester {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CExceptionTester.class);
    
    /**
     * 🔴 Call this method from any view to test exception breakpoints 🔴
     * This will trigger an exception that should hit your breakpoints
     */
    public void triggerTestException() {
        LOGGER.info("🔥 Triggering test exception...");
        
        try {
            // This will throw an exception
            throw new RuntimeException("🔥 TEST EXCEPTION - This should trigger your breakpoints!");
            
        } catch (Exception e) {
            // This should hit the CNotificationService breakpoint
            CNotificationService.showException("Test exception triggered for debugging", e);
        }
    }
    
    /**
     * 🔴 Call this to trigger an uncaught exception (should hit global handler) 🔴
     */
    public void triggerUncaughtException() {
        LOGGER.info("🔥 Triggering uncaught exception...");
        
        // This will be caught by the global exception handler
        throw new RuntimeException("🔥 UNCAUGHT TEST EXCEPTION - Should hit CGlobalExceptionHandler!");
    }
    
    /**
     * 🔴 Call this to trigger a validation exception 🔴
     */
    public void triggerValidationException() {
        LOGGER.info("🔥 Triggering validation exception...");
        
        try {
            throw new IllegalArgumentException("🔥 VALIDATION TEST EXCEPTION");
        } catch (Exception e) {
            CNotificationService.showException("Validation exception test", e);
        }
    }
}