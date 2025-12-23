package tech.derbent.api.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Factory class for creating enhanced binders with minimal changes to existing code. This factory provides static methods to create either standard
 * or enhanced binders based on configuration or specific requirements. It allows easy switching between regular and enhanced binders without changing
 * existing code structure. */
public class CBinderFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBinderFactory.class);
	// Configuration flag to enable enhanced binders by default
	private static boolean useEnhancedBinderByDefault = true;

	/** Utility method to safely cast a binder to enhanced binder if possible.
	 * @param <BEAN> the bean type
	 * @param binder the binder to cast
	 * @return the enhanced binder, or null if not an enhanced binder */
	public static <BEAN> CEnhancedBinder<BEAN> asEnhancedBinder(final CEnhancedBinder<BEAN> binder) {
		if (isEnhancedBinder(binder)) {
			return binder;
		}
		return null;
	}

	/** Creates a binder for the given bean type. Returns enhanced binder if enabled, otherwise standard binder.
	 * @param <BEAN>   the bean type
	 * @param beanType the bean class
	 * @return a binder instance */
	public static <BEAN> CEnhancedBinder<BEAN> createBinder(final Class<BEAN> beanType) {
		if (useEnhancedBinderByDefault) {
			LOGGER.debug("Creating enhanced binder for bean type: {}", beanType.getSimpleName());
			return new CEnhancedBinder<>(beanType);
		}
		LOGGER.debug("Creating standard binder for bean type: {}", beanType.getSimpleName());
		return new CEnhancedBinder<>(beanType);
	}

	/** Creates an enhanced binder with detailed error logging.
	 * @param <BEAN>   the bean type
	 * @param beanType the bean class
	 * @return an enhanced binder with detailed logging enabled */
	public static <BEAN> CEnhancedBinder<BEAN> createEnhancedBinder(final Class<BEAN> beanType) {
		LOGGER.debug("Creating enhanced binder for bean type: {}", beanType.getSimpleName());
		return new CEnhancedBinder<>(beanType);
	}

	/** Creates a standard BeanValidationBinder.
	 * @param <BEAN>   the bean type
	 * @param beanType the bean class
	 * @return a standard BeanValidationBinder */
	public static <BEAN> CEnhancedBinder<BEAN> createStandardBinder(final Class<BEAN> beanType) {
		LOGGER.debug("Creating standard binder for bean type: {}", beanType.getSimpleName());
		return new CEnhancedBinder<>(beanType);
	}

	/** Utility method to check if a binder is an enhanced binder.
	 * @param binder the binder to check
	 * @return true if the binder is an enhanced binder */
	public static boolean isEnhancedBinder(final CEnhancedBinder<?> binder) {
		return binder != null;
	}

	/** Checks if enhanced binders are used by default.
	 * @return true if enhanced binders are used by default */
	public static boolean isUseEnhancedBinderByDefault() {
		return useEnhancedBinderByDefault;
	}

	/** Sets whether to use enhanced binders by default.
	 * @param enabled true to use enhanced binders by default */
	public static void setUseEnhancedBinderByDefault(final boolean enabled) {
		useEnhancedBinderByDefault = enabled;
		LOGGER.info("Enhanced binder default usage set to: {}", enabled);
	}

	private CBinderFactory() {
		// Utility class - no instantiation
	}
}
