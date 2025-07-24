package tech.derbent.abstracts.annotations;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import tech.derbent.abstracts.domains.CEntityDB;

/**
 * CDataProviderResolver - Service for automatically resolving data providers for ComboBox
 * components based on MetaData annotations. This service integrates with Spring's
 * application context to find appropriate service beans and call their data retrieval
 * methods.
 * <p>
 * The resolver supports multiple ways to specify data providers:
 * </p>
 * <ul>
 * <li><strong>Bean Name:</strong> Uses
 * {@code @MetaData(dataProviderBean = "serviceName")}</li>
 * <li><strong>Bean Type:</strong> Uses
 * {@code @MetaData(dataProviderClass = ServiceClass.class)}</li>
 * <li><strong>Automatic:</strong> Automatically finds service by entity type naming
 * convention</li>
 * </ul>
 * <p>
 * <strong>Method Resolution:</strong> The resolver tries to find methods in the following
 * order:
 * </p>
 * <ol>
 * <li>Custom method specified in {@code dataProviderMethod}</li>
 * <li>Standard method "list(Pageable)" for paginated results</li>
 * <li>Standard method "list()" for simple lists</li>
 * <li>Standard method "findAll()" for JPA repositories</li>
 * </ol>
 * <p>
 * <strong>Caching:</strong> The resolver caches method lookups for performance but not
 * the actual data, ensuring fresh data is always retrieved from services.
 * </p>
 * <p>
 * <strong>Usage Examples:</strong>
 * </p>
 *
 * <pre>
 * 
 * {
 * 	&#64;code
 * 	// Using bean name
 * 	&#64;MetaData(displayName = "Activity Type", dataProviderBean = "activityTypeService")
 * 	private CActivityType activityType;
 * 	// Using bean class
 * 	&#64;MetaData(displayName = "Project", dataProviderClass = CProjectService.class)
 * 	private CProject project;
 * 	// Using custom method
 * 	@MetaData(displayName = "Active Users", dataProviderBean = "userService",
 * 		dataProviderMethod = "findAllActive")
 * 	private CUser assignedUser;
 * }
 * </pre>
 *
 * Layer: Service (MVC)
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.annotations.MetaData
 * @see tech.derbent.abstracts.annotations.CEntityFormBuilder
 */
@Service
public final class CDataProviderResolver {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CDataProviderResolver.class);
	/**
	 * Default page size for paginated queries when no specific size is provided.
	 */
	private static final int DEFAULT_PAGE_SIZE = 1000;
	/**
	 * Cache for resolved methods to improve performance. Key format:
	 * "beanName:methodName:entityType"
	 */
	private final Map<String, Method> methodCache = new ConcurrentHashMap<>();
	/**
	 * Cache for resolved bean instances to improve performance. Key format: "beanName" or
	 * "className"
	 */
	private final Map<String, Object> beanCache = new ConcurrentHashMap<>();
	private final ApplicationContext applicationContext;

	/**
	 * Constructor with Spring's application context for bean resolution.
	 * @param applicationContext the Spring application context for bean lookup
	 */
	@Autowired
	public CDataProviderResolver(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		LOGGER.info("CDataProviderResolver initialized with application context");
	}

	/**
	 * Calls the appropriate data retrieval method on a service bean.
	 * <p>
	 * This method attempts to call different method signatures in order of preference:
	 * </p>
	 * <ol>
	 * <li>Specified method name with Pageable parameter</li>
	 * <li>Specified method name without parameters</li>
	 * <li>Standard "list" method with Pageable</li>
	 * <li>Standard "list" method without parameters</li>
	 * <li>Standard "findAll" method</li>
	 * </ol>
	 * @param <T>         the entity type
	 * @param serviceBean the service bean instance
	 * @param methodName  the preferred method name to call
	 * @param entityType  the entity class for logging purposes
	 * @return list of entities from the method call
	 */
	@SuppressWarnings("unchecked")
	private <T extends CEntityDB> List<T> callDataMethod(final Object serviceBean,
		final String methodName, final Class<T> entityType) {
		if (serviceBean == null) {
			LOGGER.error("Service bean is null - cannot call data method");
			return Collections.emptyList();
		}
		final String beanClassName = serviceBean.getClass().getSimpleName();
		LOGGER.debug(
			"Calling data method on bean '{}' with preferred method: '{}' for entity type: {}",
			beanClassName, methodName, entityType.getSimpleName());
		// Define method names to try in order of preference
		final String[] methodsToTry = {
			methodName, "list", "findAll" };
		for (final String currentMethodName : methodsToTry) {
			if ((currentMethodName == null) || currentMethodName.trim().isEmpty()) {
				continue;
			}
			try {
				// Try with Pageable parameter first
				final List<T> result =
					tryMethodWithPageable(serviceBean, currentMethodName, entityType);
				if (result != null) {
					LOGGER.debug(
						"Successfully called method '{}' with Pageable on bean '{}' - returned {} items",
						currentMethodName, beanClassName, result.size());
					return result;
				}
				// Try without parameters
				final List<T> resultNoParams =
					tryMethodWithoutParams(serviceBean, currentMethodName, entityType);
				if (resultNoParams != null) {
					LOGGER.debug(
						"Successfully called method '{}' without parameters on bean '{}' - returned {} items",
						currentMethodName, beanClassName, resultNoParams.size());
					return resultNoParams;
				}
			} catch (final Exception e) {
				LOGGER.debug("Method '{}' failed on bean '{}': {} - trying next method",
					currentMethodName, beanClassName, e.getMessage());
			}
		}
		LOGGER.error(
			"No suitable data retrieval method found on bean '{}' for entity type: {}",
			beanClassName, entityType.getSimpleName());
		return Collections.emptyList();
	}

	/**
	 * Clears all caches. Useful for testing or when bean configuration changes.
	 */
	public void clearCaches() {
		methodCache.clear();
		beanCache.clear();
		LOGGER.info("CDataProviderResolver caches cleared");
	}

	/**
	 * Retrieves a bean from cache or computes it using the supplier.
	 * @param cacheKey     the cache key
	 * @param beanSupplier supplier to compute the bean if not cached
	 * @return the bean or null if not found
	 */
	private Object getBeanFromCache(final String cacheKey,
		final java.util.function.Supplier<Object> beanSupplier) {
		return beanCache.computeIfAbsent(cacheKey, k -> {
			final Object bean = beanSupplier.get();
			if (bean != null) {
				LOGGER.debug("Cached bean for key: {}", cacheKey);
			}
			return bean;
		});
	}

	/**
	 * Gets cache statistics for monitoring purposes.
	 * @return string representation of cache sizes
	 */
	public String getCacheStats() {
		return String.format(
			"CDataProviderResolver - Method cache: %d entries, Bean cache: %d entries",
			methodCache.size(), beanCache.size());
	}

	/**
	 * Retrieves a method from cache or computes it using the supplier.
	 * @param cacheKey       the cache key
	 * @param methodSupplier supplier to compute the method if not cached
	 * @return the method or null if not found
	 */
	private Method getMethodFromCache(final String cacheKey,
		final java.util.function.Supplier<Method> methodSupplier) {
		return methodCache.computeIfAbsent(cacheKey, k -> {
			final Method method = methodSupplier.get();
			if (method != null) {
				LOGGER.debug("Cached method for key: {}", cacheKey);
			}
			return method;
		});
	}

	/**
	 * Resolves and retrieves data for a ComboBox field based on its MetaData annotation.
	 * <p>
	 * This method analyzes the MetaData annotation to determine the appropriate data
	 * source and method to call. It supports various configuration options and fallback
	 * mechanisms.
	 * </p>
	 * @param <T>        the entity type for the ComboBox items
	 * @param entityType the class type of entities to retrieve
	 * @param metaData   the MetaData annotation containing provider configuration
	 * @return list of entities for the ComboBox, never null but may be empty
	 * @throws IllegalArgumentException if parameters are null
	 */
	public <T extends CEntityDB> List<T> resolveData(final Class<T> entityType,
		final MetaData metaData) {
		// Enhanced null pointer checking with detailed logging
		if (entityType == null) {
			LOGGER.error("Entity type parameter is null - cannot resolve data");
			throw new IllegalArgumentException("Entity type cannot be null");
		}
		if (metaData == null) {
			LOGGER.error(
				"MetaData parameter is null for entity type: {} - cannot resolve data",
				entityType.getSimpleName());
			throw new IllegalArgumentException("MetaData cannot be null");
		}
		LOGGER.debug(
			"Resolving data provider for entity type: {} with MetaData configuration",
			entityType.getSimpleName());
		try {
			// Strategy 1: Use specified bean name
			if ((metaData.dataProviderBean() != null)
				&& !metaData.dataProviderBean().trim().isEmpty()) {
				LOGGER.debug("Using specified bean name: '{}' for entity type: {}",
					metaData.dataProviderBean(), entityType.getSimpleName());
				return resolveDataFromBean(entityType, metaData.dataProviderBean(),
					metaData.dataProviderMethod());
			}
			// Strategy 2: Use specified bean class
			if ((metaData.dataProviderClass() != null)
				&& (metaData.dataProviderClass() != Object.class)) {
				LOGGER.debug("Using specified bean class: '{}' for entity type: {}",
					metaData.dataProviderClass().getSimpleName(),
					entityType.getSimpleName());
				return resolveDataFromClass(entityType, metaData.dataProviderClass(),
					metaData.dataProviderMethod());
			}
			// Strategy 3: Automatic resolution by naming convention
			LOGGER.debug("Attempting automatic resolution for entity type: {}",
				entityType.getSimpleName());
			return resolveDataAutomatically(entityType, metaData.dataProviderMethod());
		} catch (final Exception e) {
			LOGGER.error(
				"Failed to resolve data for entity type: {} - returning empty list. Error: {}",
				entityType.getSimpleName(), e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	/**
	 * Attempts automatic resolution using naming conventions.
	 * <p>
	 * This method tries to find a service bean using common naming patterns:
	 * </p>
	 * <ul>
	 * <li>EntityNameService (e.g., CActivityTypeService for CActivityType)</li>
	 * <li>entityNameService (camelCase version)</li>
	 * </ul>
	 * @param <T>        the entity type
	 * @param entityType the class type of entities to retrieve
	 * @param methodName the method name to call on the bean
	 * @return list of entities from automatically resolved service
	 */
	@SuppressWarnings("unchecked")
	private <T extends CEntityDB> List<T>
		resolveDataAutomatically(final Class<T> entityType, final String methodName) {
		LOGGER.debug(
			"Attempting automatic resolution for entity type: {} using method: {}",
			entityType.getSimpleName(), methodName);
		final String entityName = entityType.getSimpleName();
		// Try different naming conventions for service beans
		final String[] possibleBeanNames = {
			entityName + "Service", Character.toLowerCase(entityName.charAt(0))
				+ entityName.substring(1) + "Service",
			entityName.toLowerCase() + "Service" };
		for (final String beanName : possibleBeanNames) {
			LOGGER.debug("Trying bean name: '{}' for entity type: {}", beanName,
				entityName);
			try {
				if (applicationContext.containsBean(beanName)) {
					LOGGER.debug("Found bean '{}' for entity type: {}", beanName,
						entityName);
					return resolveDataFromBean(entityType, beanName, methodName);
				}
			} catch (final Exception e) {
				LOGGER.debug(
					"Failed to use bean '{}' for entity type: {} - trying next option",
					beanName, entityName);
			}
		}
		LOGGER.warn(
			"No suitable service bean found for entity type: {} using automatic resolution",
			entityName);
		return Collections.emptyList();
	}

	/**
	 * Resolves data using a specific Spring bean name.
	 * @param <T>        the entity type
	 * @param entityType the class type of entities to retrieve
	 * @param beanName   the Spring bean name to use
	 * @param methodName the method name to call on the bean
	 * @return list of entities from the specified bean
	 */
	@SuppressWarnings("unchecked")
	private <T extends CEntityDB> List<T> resolveDataFromBean(final Class<T> entityType,
		final String beanName, final String methodName) {
		LOGGER.debug(
			"Resolving data from bean '{}' using method '{}' for entity type: {}",
			beanName, methodName, entityType.getSimpleName());
		try {
			// Get bean from Spring context with caching
			final Object serviceBean = getBeanFromCache(beanName, () -> {
				if (applicationContext.containsBean(beanName)) {
					return applicationContext.getBean(beanName);
				}
				else {
					LOGGER.warn("Bean '{}' not found in application context", beanName);
					return null;
				}
			});
			if (serviceBean == null) {
				LOGGER.error("Failed to retrieve bean '{}' from Spring context",
					beanName);
				return Collections.emptyList();
			}
			LOGGER.debug("Successfully retrieved bean '{}' of type: {}", beanName,
				serviceBean.getClass().getSimpleName());
			return callDataMethod(serviceBean, methodName, entityType);
		} catch (final Exception e) {
			LOGGER.error("Error resolving data from bean '{}': {}", beanName,
				e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	/**
	 * Resolves data using a specific Spring bean class type.
	 * @param <T>          the entity type
	 * @param entityType   the class type of entities to retrieve
	 * @param serviceClass the Spring bean class type to use
	 * @param methodName   the method name to call on the bean
	 * @return list of entities from the specified bean type
	 */
	@SuppressWarnings("unchecked")
	private <T extends CEntityDB> List<T> resolveDataFromClass(final Class<T> entityType,
		final Class<?> serviceClass, final String methodName) {
		LOGGER.debug(
			"Resolving data from bean class '{}' using method '{}' for entity type: {}",
			serviceClass.getSimpleName(), methodName, entityType.getSimpleName());
		try {
			// Get bean by type from Spring context with caching
			final String cacheKey = serviceClass.getName();
			final Object serviceBean = getBeanFromCache(cacheKey, () -> {
				try {
					return applicationContext.getBean(serviceClass);
				} catch (final Exception e) {
					LOGGER.warn("Bean of type '{}' not found in application context: {}",
						serviceClass.getSimpleName(), e.getMessage());
					return null;
				}
			});
			if (serviceBean == null) {
				LOGGER.error("Failed to retrieve bean of type '{}' from Spring context",
					serviceClass.getSimpleName());
				return Collections.emptyList();
			}
			LOGGER.debug("Successfully retrieved bean of type: {}",
				serviceClass.getSimpleName());
			return callDataMethod(serviceBean, methodName, entityType);
		} catch (final Exception e) {
			LOGGER.error("Error resolving data from bean class '{}': {}",
				serviceClass.getSimpleName(), e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	/**
	 * Attempts to call a method without parameters.
	 * @param <T>         the entity type
	 * @param serviceBean the service bean
	 * @param methodName  the method name to call
	 * @param entityType  the entity type for caching
	 * @return list of entities or null if method not found/failed
	 */
	@SuppressWarnings("unchecked")
	private <T extends CEntityDB> List<T> tryMethodWithoutParams(final Object serviceBean,
		final String methodName, final Class<T> entityType) {
		final String cacheKey = serviceBean.getClass().getName() + ":" + methodName
			+ ":noparams:" + entityType.getSimpleName();
		try {
			final Method method = getMethodFromCache(cacheKey, () -> {
				try {
					return serviceBean.getClass().getMethod(methodName);
				} catch (final NoSuchMethodException e) {
					return null;
				}
			});
			if (method != null) {
				final Object result = method.invoke(serviceBean);
				if (result instanceof List) {
					return (List<T>) result;
				}
			}
		} catch (final Exception e) {
			LOGGER.debug("Failed to call method '{}' without parameters: {}", methodName,
				e.getMessage());
		}
		return null;
	}

	/**
	 * Attempts to call a method with Pageable parameter.
	 * @param <T>         the entity type
	 * @param serviceBean the service bean
	 * @param methodName  the method name to call
	 * @param entityType  the entity type for caching
	 * @return list of entities or null if method not found/failed
	 */
	@SuppressWarnings("unchecked")
	private <T extends CEntityDB> List<T> tryMethodWithPageable(final Object serviceBean,
		final String methodName, final Class<T> entityType) {
		final String cacheKey = serviceBean.getClass().getName() + ":" + methodName
			+ ":pageable:" + entityType.getSimpleName();
		try {
			final Method method = getMethodFromCache(cacheKey, () -> {
				try {
					return serviceBean.getClass().getMethod(methodName, Pageable.class);
				} catch (final NoSuchMethodException e) {
					return null;
				}
			});
			if (method != null) {
				final Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
				final Object result = method.invoke(serviceBean, pageable);
				if (result instanceof org.springframework.data.domain.Page) {
					return ((org.springframework.data.domain.Page<T>) result)
						.getContent();
				}
				else if (result instanceof List) {
					return (List<T>) result;
				}
			}
		} catch (final Exception e) {
			LOGGER.debug("Failed to call method '{}' with Pageable: {}", methodName,
				e.getMessage());
		}
		return null;
	}
}