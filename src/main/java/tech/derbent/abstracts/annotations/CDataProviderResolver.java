package tech.derbent.abstracts.annotations;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.utils.CPageableUtils;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** CDataProviderResolver - Service for automatically resolving data providers for ComboBox components based on AMetaData annotations. This service
 * integrates with Spring's application context to find appropriate service beans and call their data retrieval methods.
 * <p>
 * The resolver supports multiple ways to specify data providers:
 * </p>
 * <ul>
 * <li><strong>Bean Name:</strong> Uses {@code @AMetaData(dataProviderBean = "serviceName")}</li>
 * <li><strong>Bean Type:</strong> Uses {@code @AMetaData(dataProviderClass = ServiceClass.class)}</li>
 * <li><strong>Automatic:</strong> Automatically finds service by entity type naming convention</li>
 * </ul>
 * <p>
 * <strong>Method Resolution:</strong> The resolver tries to find methods in the following order:
 * </p>
 * <ol>
 * <li>Custom method specified in {@code dataProviderMethod}</li>
 * <li>Standard method "list(Pageable)" for paginated results</li>
 * <li>Standard method "list()" for simple lists</li>
 * <li>Standard method "findAll()" for JPA repositories</li>
 * </ol>
 * <p>
 * <strong>Caching:</strong> The resolver caches method lookups for performance but not the actual data, ensuring fresh data is always retrieved from
 * services.
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
 * 	&#64;AMetaData (displayName = "Activity Type", dataProviderBean = "activityTypeService")
 * 	private CActivityType activityType;
 * 	// Using bean class
 * 	&#64;AMetaData (displayName = "Project", dataProviderClass = CProjectService.class)
 * 	private CProject project;
 * 	// Using custom method
 * 	@AMetaData (displayName = "Active Users", dataProviderBean = "userService", dataProviderMethod = "findAllActive")
 * 	private CUser assignedUser;
 * }
 * </pre>
 *
 * Layer: Service (MVC)
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.annotations.AMetaData
 * @see tech.derbent.abstracts.annotations.CEntityFormBuilder */
@Service
public final class CDataProviderResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDataProviderResolver.class);
	/** Default page size for paginated queries when no specific size is provided. */
	private static final int DEFAULT_PAGE_SIZE = 1000;
	/** Cache for resolved methods to improve performance. Key format: "beanName:methodName:entityType" */
	private final Map<String, Method> methodCache = new ConcurrentHashMap<>();
	/** Cache for resolved bean instances to improve performance. Key format: "beanName" or "className" */
	private final Map<String, Object> beanCache = new ConcurrentHashMap<>();
	private final ApplicationContext applicationContext;

	/** Constructor with Spring's application context for bean resolution.
	 * @param applicationContext the Spring application context for bean lookup */
	@Autowired
	public CDataProviderResolver(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/** Calls the appropriate data retrieval method on a service bean.
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
	 * @return list of entities from the method call */
	private <T extends CEntityDB<T>> List<T> callDataMethod(final Object serviceBean, final String methodName, final Class<T> entityType) {
		Check.notNull(serviceBean, "Service bean cannot be null");
		Check.notNull(methodName, "Method name cannot be null");
		Check.notNull(entityType, "Entity type cannot be null");
		final String beanClassName = serviceBean.getClass().getSimpleName();
		LOGGER.debug("Calling data method on bean '{}' with preferred method: '{}' for entity type: {}", beanClassName, methodName,
				entityType.getSimpleName());
		// Define method names to try in order of preference
		final String[] methodsToTry = {
				methodName, "list", "findAll"
		};
		for (final String currentMethodName : methodsToTry) {
			if ((currentMethodName == null) || currentMethodName.trim().isEmpty()) {
				continue;
			}
			try {
				// Try with Pageable parameter first
				final List<T> result = tryMethodWithPageable(serviceBean, currentMethodName, entityType);
				if (result != null) {
					LOGGER.debug("Successfully called method '{}' with Pageable on bean '{}' - returned {} items", currentMethodName, beanClassName,
							result.size());
					return result;
				}
				// Try without parameters
				final List<T> resultNoParams = tryMethodWithoutParams(serviceBean, currentMethodName, entityType);
				if (resultNoParams != null) {
					LOGGER.debug("Successfully called method '{}' without parameters on bean '{}' - returned {} items", currentMethodName,
							beanClassName, resultNoParams.size());
					return resultNoParams;
				}
			} catch (final Exception e) {
				LOGGER.debug("Method '{}' failed on bean '{}': {} - trying next method", currentMethodName, beanClassName, e.getMessage());
			}
		}
		LOGGER.error("No suitable data retrieval method found on bean '{}' for entity type: {} methodname:{}", beanClassName,
				entityType.getSimpleName(), methodName);
		return Collections.emptyList();
	}

	/** Clears all caches. Useful for testing or when bean configuration changes. */
	public void clearCaches() {
		methodCache.clear();
		beanCache.clear();
		LOGGER.info("CDataProviderResolver caches cleared");
	}

	/** Debug method to list all available service beans in the application context. Useful for troubleshooting data provider resolution issues.
	 * @return list of all bean names that end with "Service" */
	public List<String> getAvailableServiceBeans() {
		return java.util.Arrays.stream(applicationContext.getBeanDefinitionNames()).filter(name -> name.toLowerCase().contains("service")).sorted()
				.collect(java.util.stream.Collectors.toList());
	}

	/** Retrieves a bean from cache or computes it using the supplier.
	 * @param cacheKey     the cache key
	 * @param beanSupplier supplier to compute the bean if not cached
	 * @return the bean or null if not found */
	private Object getBeanFromCache(final String cacheKey, final Supplier<Object> beanSupplier) {
		return beanCache.computeIfAbsent(cacheKey, k -> {
			final Object bean = beanSupplier.get();
			if (bean != null) {
				LOGGER.debug("Cached bean for key: {}", cacheKey);
			}
			return bean;
		});
	}

	/** Gets cache statistics for monitoring purposes.
	 * @return string representation of cache sizes */
	public String getCacheStats() {
		return String.format("CDataProviderResolver - Method cache: %d entries, Bean cache: %d entries", methodCache.size(), beanCache.size());
	}

	/** Retrieves a method from cache or computes it using the supplier.
	 * @param cacheKey       the cache key
	 * @param methodSupplier supplier to compute the method if not cached
	 * @return the method or null if not found */
	private Method getMethodFromCache(final String cacheKey, final java.util.function.Supplier<Method> methodSupplier) {
		return methodCache.computeIfAbsent(cacheKey, k -> {
			final Method method = methodSupplier.get();
			if (method != null) {
				LOGGER.debug("Cached method for key: {}", cacheKey);
			}
			return method;
		});
	}

	/** Resolves and retrieves data for a ComboBox field based on its AMetaData annotation.
	 * <p>
	 * This method analyzes the AMetaData annotation to determine the appropriate data source and method to call. It supports various configuration
	 * options and fallback mechanisms.
	 * </p>
	 * @param <T>        the entity type for the ComboBox items
	 * @param entityType the class type of entities to retrieve
	 * @param metaData   the AMetaData annotation containing provider configuration
	 * @return list of entities for the ComboBox, never null but may be empty
	 * @throws IllegalArgumentException if parameters are null */
	public <T extends CEntityDB<T>> List<T> resolveData(final Class<T> entityType, final AMetaData metaData) {
		Check.notNull(entityType, "Entity");
		Check.notNull(metaData, "AMetaData");
		LOGGER.debug("Resolving data provider for entity type: {} with AMetaData configuration", entityType.getSimpleName());
		// Strategy 1: Use specified bean name
		if ((metaData.dataProviderBean() != null) && !metaData.dataProviderBean().trim().isEmpty()) {
			return resolveDataFromBean(entityType, metaData.dataProviderBean(), metaData.dataProviderMethod());
		}
		// Strategy 2: Use specified bean class
		if ((metaData.dataProviderClass() != null) && (metaData.dataProviderClass() != Object.class)) {
			return resolveDataFromClass(entityType, metaData.dataProviderClass(), metaData.dataProviderMethod());
		}
		// Strategy 3: Automatic resolution by naming convention
		LOGGER.debug("Attempting automatic resolution for entity type: {}", entityType.getSimpleName());
		return resolveDataAutomatically(entityType, metaData.dataProviderMethod());
	}

	@SuppressWarnings ("unchecked")
	public <T extends CEntityDB<T>> List<T> resolveData(final EntityFieldInfo fieldInfo) {
		Check.notNull(fieldInfo, "Field info cannot be null");
		// Strategy 1: Use specified bean name
		if ((fieldInfo.getDataProviderBean() != null) && !fieldInfo.getDataProviderBean().trim().isEmpty()) {
			return resolveDataFromBean((Class<T>) fieldInfo.getFieldTypeClass(), fieldInfo.getDataProviderBean(), fieldInfo.getDataProviderMethod());
		}
		// Strategy 3: Automatic resolution by naming convention
		LOGGER.debug("Attempting automatic resolution for entity type: {}", fieldInfo.getFieldName());
		return resolveDataAutomatically((Class<T>) fieldInfo.getFieldTypeClass(), fieldInfo.getDataProviderMethod());
	}

	/** Attempts automatic resolution using naming conventions.
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
	 * @return list of entities from automatically resolved service */
	private <T extends CEntityDB<T>> List<T> resolveDataAutomatically(final Class<T> entityType, final String methodName) {
		final String entityName = entityType.getSimpleName();
		// Try different naming conventions for service beans
		final String[] possibleBeanNames = {
				entityName + "Service", Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "Service",
				entityName.toLowerCase() + "Service"
		};
		for (final String beanName : possibleBeanNames) {
			if (applicationContext.containsBean(beanName)) {
				return resolveDataFromBean(entityType, beanName, methodName);
			}
		}
		LOGGER.warn("No suitable service bean found for entity type: {} using automatic resolution", entityName);
		return Collections.emptyList();
	}

	/** Resolves data using a specific Spring bean name.
	 * @param <T>        the entity type
	 * @param entityType the class type of entities to retrieve
	 * @param beanName   the Spring bean name to use
	 * @param methodName the method name to call on the bean
	 * @return list of entities from the specified bean */
	private <T extends CEntityDB<T>> List<T> resolveDataFromBean(final Class<T> entityType, final String beanName, final String methodName) {
		LOGGER.debug("Resolving data from bean '{}' using method '{}' for entity type: {}", beanName, methodName, entityType.getSimpleName());
		Check.notBlank(beanName, "Bean name cannot be empty");
		// Get bean from Spring context with caching
		final Object serviceBean = getBeanFromCache(beanName, () -> {
			Check.isTrue(applicationContext.containsBean(beanName),
					"Bean '" + beanName + "' not found in application context of beans:" + getAvailableServiceBeans());
			return applicationContext.getBean(beanName);
		});
		Check.notNull(serviceBean, "Service bean cannot be null for bean name: " + beanName);
		return callDataMethod(serviceBean, methodName, entityType);
	}

	/** Resolves data using a specific Spring bean class type.
	 * @param <T>          the entity type
	 * @param entityType   the class type of entities to retrieve
	 * @param serviceClass the Spring bean class type to use
	 * @param methodName   the method name to call on the bean
	 * @return list of entities from the specified bean type */
	private <T extends CEntityDB<T>> List<T> resolveDataFromClass(final Class<T> entityType, final Class<?> serviceClass, final String methodName) {
		LOGGER.debug("Resolving data from bean class '{}' using method '{}' for entity type: {}", serviceClass.getSimpleName(), methodName,
				entityType.getSimpleName());
		try {
			// Get bean by type from Spring context with caching
			final String cacheKey = serviceClass.getName();
			final Object serviceBean = getBeanFromCache(cacheKey, () -> {
				return applicationContext.getBean(serviceClass);
			});
			if (serviceBean == null) {
				LOGGER.error("Failed to retrieve bean of type '{}' from Spring context", serviceClass.getSimpleName());
				return Collections.emptyList();
			}
			LOGGER.debug("Successfully retrieved bean of type: {}", serviceClass.getSimpleName());
			return callDataMethod(serviceBean, methodName, entityType);
		} catch (final Exception e) {
			LOGGER.error("Error resolving data from bean class '{}': {}", serviceClass.getSimpleName(), e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	/** Attempts to call a method without parameters.
	 * @param <T>         the entity type
	 * @param serviceBean the service bean
	 * @param methodName  the method name to call
	 * @param entityType  the entity type for caching
	 * @return list of entities or null if method not found/failed */
	@SuppressWarnings ("unchecked")
	private <T extends CEntityDB<T>> List<T> tryMethodWithoutParams(final Object serviceBean, final String methodName, final Class<T> entityType) {
		final String cacheKey = serviceBean.getClass().getName() + ":" + methodName + ":noparams:" + entityType.getSimpleName();
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
			LOGGER.debug("Failed to call method '{}' without parameters: {}", methodName, e.getMessage());
		}
		return null;
	}

	/** Attempts to call a method with Pageable parameter.
	 * @param <T>         the entity type
	 * @param serviceBean the service bean
	 * @param methodName  the method name to call
	 * @param entityType  the entity type for caching
	 * @return list of entities or null if method not found/failed */
	@SuppressWarnings ("unchecked")
	private <T extends CEntityDB<T>> List<T> tryMethodWithPageable(final Object serviceBean, final String methodName, final Class<T> entityType) {
		final String cacheKey = serviceBean.getClass().getName() + ":" + methodName + ":pageable:" + entityType.getSimpleName();
		try {
			final Method method = getMethodFromCache(cacheKey, () -> {
				try {
					return serviceBean.getClass().getMethod(methodName, Pageable.class);
				} catch (final NoSuchMethodException e) {
					return null;
				}
			});
			if (method != null) {
				final Pageable pageable = CPageableUtils.createSafe(0, DEFAULT_PAGE_SIZE);
				final Object result = method.invoke(serviceBean, pageable);
				if (result instanceof Page) {
					return ((Page<T>) result).getContent();
				} else if (result instanceof List) {
					return (List<T>) result;
				}
			}
		} catch (final Exception e) {
			LOGGER.debug("Failed to call method '{}' with Pageable: {}", methodName, e.getMessage());
		}
		return null;
	}
}
