package tech.derbent.abstracts.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.data.binder.BeanValidationBinder;

/**
 * Factory class for creating enhanced binders with minimal changes to existing code.
 * 
 * This factory provides static methods to create either standard or enhanced binders
 * based on configuration or specific requirements. It allows easy switching between
 * regular and enhanced binders without changing existing code structure.
 */
public class CBinderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CBinderFactory.class);
    
    // Configuration flag to enable enhanced binders by default
    private static boolean useEnhancedBinderByDefault = false;
    private static boolean globalDetailedLoggingEnabled = true;
    
    private CBinderFactory() {
        // Utility class - no instantiation
    }
    
    /**
     * Creates a binder for the given bean type. 
     * Returns enhanced binder if enabled, otherwise standard binder.
     * 
     * @param <BEAN> the bean type
     * @param beanType the bean class
     * @return a binder instance
     */
    public static <BEAN> BeanValidationBinder<BEAN> createBinder(Class<BEAN> beanType) {
        if (useEnhancedBinderByDefault) {
            LOGGER.debug("Creating enhanced binder for bean type: {}", beanType.getSimpleName());
            return new CEnhancedBinder<>(beanType, globalDetailedLoggingEnabled);
        } else {
            LOGGER.debug("Creating standard binder for bean type: {}", beanType.getSimpleName());
            return new BeanValidationBinder<>(beanType);
        }
    }
    
    /**
     * Creates a standard BeanValidationBinder.
     * 
     * @param <BEAN> the bean type
     * @param beanType the bean class
     * @return a standard BeanValidationBinder
     */
    public static <BEAN> BeanValidationBinder<BEAN> createStandardBinder(Class<BEAN> beanType) {
        LOGGER.debug("Creating standard binder for bean type: {}", beanType.getSimpleName());
        return new BeanValidationBinder<>(beanType);
    }
    
    /**
     * Creates an enhanced binder with detailed error logging.
     * 
     * @param <BEAN> the bean type
     * @param beanType the bean class
     * @return an enhanced binder with detailed logging enabled
     */
    public static <BEAN> CEnhancedBinder<BEAN> createEnhancedBinder(Class<BEAN> beanType) {
        LOGGER.debug("Creating enhanced binder for bean type: {}", beanType.getSimpleName());
        return new CEnhancedBinder<>(beanType, globalDetailedLoggingEnabled);
    }
    
    /**
     * Creates an enhanced binder with configurable logging.
     * 
     * @param <BEAN> the bean type
     * @param beanType the bean class
     * @param detailedLoggingEnabled whether to enable detailed logging
     * @return an enhanced binder with specified logging configuration
     */
    public static <BEAN> CEnhancedBinder<BEAN> createEnhancedBinder(Class<BEAN> beanType, 
            boolean detailedLoggingEnabled) {
        LOGGER.debug("Creating enhanced binder for bean type: {} with detailed logging: {}", 
            beanType.getSimpleName(), detailedLoggingEnabled);
        return new CEnhancedBinder<>(beanType, detailedLoggingEnabled);
    }
    
    /**
     * Sets whether to use enhanced binders by default.
     * 
     * @param enabled true to use enhanced binders by default
     */
    public static void setUseEnhancedBinderByDefault(boolean enabled) {
        useEnhancedBinderByDefault = enabled;
        LOGGER.info("Enhanced binder default usage set to: {}", enabled);
    }
    
    /**
     * Checks if enhanced binders are used by default.
     * 
     * @return true if enhanced binders are used by default
     */
    public static boolean isUseEnhancedBinderByDefault() {
        return useEnhancedBinderByDefault;
    }
    
    /**
     * Sets the global detailed logging configuration for enhanced binders.
     * 
     * @param enabled true to enable detailed logging globally
     */
    public static void setGlobalDetailedLoggingEnabled(boolean enabled) {
        globalDetailedLoggingEnabled = enabled;
        LOGGER.info("Global detailed logging for enhanced binders set to: {}", enabled);
    }
    
    /**
     * Checks if global detailed logging is enabled for enhanced binders.
     * 
     * @return true if global detailed logging is enabled
     */
    public static boolean isGlobalDetailedLoggingEnabled() {
        return globalDetailedLoggingEnabled;
    }
    
    /**
     * Utility method to check if a binder is an enhanced binder.
     * 
     * @param binder the binder to check
     * @return true if the binder is an enhanced binder
     */
    public static boolean isEnhancedBinder(BeanValidationBinder<?> binder) {
        return binder instanceof CEnhancedBinder;
    }
    
    /**
     * Utility method to safely cast a binder to enhanced binder if possible.
     * 
     * @param <BEAN> the bean type
     * @param binder the binder to cast
     * @return the enhanced binder, or null if not an enhanced binder
     */
    @SuppressWarnings("unchecked")
    public static <BEAN> CEnhancedBinder<BEAN> asEnhancedBinder(BeanValidationBinder<BEAN> binder) {
        if (isEnhancedBinder(binder)) {
            return (CEnhancedBinder<BEAN>) binder;
        }
        return null;
    }
}