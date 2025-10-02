package tech.derbent.api.annotations;

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
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.api.utils.Check;
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
 * @see tech.derbent.api.annotations.AMetaData
 * @see tech.derbent.api.annotations.CFormBuilder */
@Service
public final class CDataProviderResolver {

	/** Default page size for paginated queries when no specific size is provided. */
	private static final int DEFAULT_PAGE_SIZE = 1000;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDataProviderResolver.class);
	private final ApplicationContext applicationContext;
	/** Cache for resolved bean instances to improve performance. Key format: "beanName" or "className" */
	private final Map<String, Object> beanCache = new ConcurrentHashMap<>();
	/** Cache for resolved methods to improve performance. Key format: "beanName:methodName:entityType" */
	private final Map<String, Method> methodCache = new ConcurrentHashMap<>();

	/** Constructor with Spring's application context for bean resolution.
	 * @param applicationContext the Spring application context for bean lookup */
	@Autowired
	public CDataProviderResolver(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	private <T extends CEntityDB<T>> List<T> callDataMethod(final Object serviceBean, final String methodName, final Class<T> entityType)
			throws Exception {
		return callDataMethod(serviceBean, methodName, entityType, null);
	}

	private <T extends CEntityDB<T>> List<T> callDataMethod(final Object serviceBean, final String methodName, final Class<T> entityType,
			final String paramMethodName) throws Exception {
		Check.notNull(serviceBean, "Service bean cannot be null");
		Check.notNull(methodName, "Method name cannot be null");
		Check.notNull(entityType, "Entity type cannot be null");
		final String beanClassName = serviceBean.getClass().getSimpleName();
		LOGGER.debug("Calling data method on bean '{}' with preferred method: '{}' for entity type: {}", beanClassName, methodName,
				entityType.getSimpleName());
		// Define method names to try in order of preference
		String param = null;
		if ((paramMethodName != null) && !paramMethodName.trim().isEmpty()) {
			param = CAuxillaries.invokeMethodOfString(serviceBean, paramMethodName);
			Check.notNull(param, "Parameter from method '" + paramMethodName + "' cannot be null");
		}
		// Try with Pageable parameter first
		final List<T> result = tryMethodWithPageable(serviceBean, methodName, entityType, param);
		Check.notNull(result, "Result returned null for method: " + methodName + " in bean: " + beanClassName);
		return result;
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
	 * @throws Exception
	 * @throws IllegalArgumentException if parameters are null */
	public <T extends CEntityDB<T>> List<T> resolveData(IContentOwner contentOwner, final Class<T> entityType, final AMetaData metaData)
			throws Exception {
		Check.notNull(entityType, "Entity");
		Check.notNull(metaData, "AMetaData");
		LOGGER.debug("Resolving data provider for entity type: {} with AMetaData configuration", entityType.getSimpleName());
		// Strategy 1: Use content owner if specified
		if ("content".equals(metaData.dataProviderOwner())
				|| (metaData.dataProviderOwner() != null && !metaData.dataProviderOwner().trim().isEmpty())) {
			return resolveDataFromContentOwner(contentOwner, entityType, metaData.dataProviderMethod(), metaData.dataProviderParamMethod());
		}
		// Strategy 2: Use specified bean name
		if ((metaData.dataProviderBean() != null) && !metaData.dataProviderBean().trim().isEmpty()) {
			return resolveDataFromBean(entityType, metaData.dataProviderBean(), metaData.dataProviderMethod(), metaData.dataProviderParamMethod());
		}
		// Strategy 3: Use specified bean class
		if ((metaData.dataProviderClass() != null) && (metaData.dataProviderClass() != Object.class)) {
			return resolveDataFromClass(entityType, metaData.dataProviderClass(), metaData.dataProviderMethod());
		}
		// Strategy 4: Automatic resolution by naming convention
		LOGGER.debug("Attempting automatic resolution for entity type: {}", entityType.getSimpleName());
		return resolveDataAutomatically(entityType, metaData.dataProviderMethod());
	}

	@SuppressWarnings ("unchecked")
	public <T extends CEntityDB<T>> List<T> resolveData(IContentOwner contentOwner, final EntityFieldInfo fieldInfo) throws Exception {
		Check.notNull(fieldInfo, "Field info cannot be null");
		// Strategy 1: Use content owner if specified
		if ("content".equals(fieldInfo.getDataProviderOwner())
				|| (fieldInfo.getDataProviderOwner() != null && !fieldInfo.getDataProviderOwner().trim().isEmpty())) {
			return resolveDataFromContentOwner(contentOwner, (Class<T>) fieldInfo.getFieldTypeClass(), fieldInfo.getDataProviderMethod(),
					fieldInfo.getDataProviderParamMethod());
		}
		// Strategy 2: Use specified bean name
		if ((fieldInfo.getDataProviderBean() != null) && !fieldInfo.getDataProviderBean().trim().isEmpty()) {
			return resolveDataFromBean((Class<T>) fieldInfo.getFieldTypeClass(), fieldInfo.getDataProviderBean(), fieldInfo.getDataProviderMethod(),
					fieldInfo.getDataProviderParamMethod());
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

	/** Resolves data using a specific Spring bean name (overload without param method).
	 * @param <T>        the entity type
	 * @param entityType the class type of entities to retrieve
	 * @param beanName   the Spring bean name to use
	 * @param methodName the method name to call on the bean
	 * @return list of entities from the specified bean */
	private <T extends CEntityDB<T>> List<T> resolveDataFromBean(final Class<T> entityType, final String beanName, final String methodName) {
		return resolveDataFromBean(entityType, beanName, methodName, null);
	}

	/** Resolves data using a specific Spring bean name.
	 * @param <T>        the entity type
	 * @param entityType the class type of entities to retrieve
	 * @param beanName   the Spring bean name to use
	 * @param methodName the method name to call on the bean
	 * @return list of entities from the specified bean */
	private <T extends CEntityDB<T>> List<T> resolveDataFromBean(final Class<T> entityType, final String beanName, final String methodName,
			final String paramMethodName) {
		LOGGER.debug("Resolving data from bean '{}' using method '{}' for entity type: {}", beanName, methodName, entityType.getSimpleName());
		Check.notBlank(beanName, "Bean name cannot be empty");
		// Get bean from Spring context with caching
		final Object serviceBean = getBeanFromCache(beanName, () -> {
			Check.isTrue(applicationContext.containsBean(beanName),
					"Bean '" + beanName + "' not found in application context of beans:" + getAvailableServiceBeans());
			return applicationContext.getBean(beanName);
		});
		Check.notNull(serviceBean, "Service bean cannot be null for bean name: " + beanName);
		try {
			return callDataMethod(serviceBean, methodName, entityType, paramMethodName);
		} catch (final Exception e) {
			LOGGER.error("Error resolving data from bean '{}': {}", beanName, e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	/** Resolves data using a specific Spring bean class type.
	 * @param <T>          the entity type
	 * @param entityType   the class type of entities to retrieve
	 * @param serviceClass the Spring bean class type to use
	 * @param methodName   the method name to call on the bean
	 * @return list of entities from the specified bean type
	 * @throws Exception */
	private <T extends CEntityDB<T>> List<T> resolveDataFromClass(final Class<T> entityType, final Class<?> serviceClass, final String methodName)
			throws Exception {
		LOGGER.debug("Resolving data from bean class '{}' using method '{}' for entity type: {}", serviceClass.getSimpleName(), methodName,
				entityType.getSimpleName());
		try {
			// Get bean by type from Spring context with caching
			final String cacheKey = serviceClass.getName();
			final Object serviceBean = getBeanFromCache(cacheKey, () -> {
				return applicationContext.getBean(serviceClass);
			});
			Check.notNull(serviceBean, "Service bean cannot be null for class: " + serviceClass.getSimpleName());
			LOGGER.debug("Successfully retrieved bean of type: {}", serviceClass.getSimpleName());
			return callDataMethod(serviceBean, methodName, entityType);
		} catch (final Exception e) {
			LOGGER.error("Error resolving data from bean class '{}': {}", serviceClass.getSimpleName(), e.getMessage(), e);
			throw e;
		}
	}

	/** Resolves data from the content owner (current page/context) instead of a service bean.
	 * @param <T>             the entity type
	 * @param entityType      the class type of entities to retrieve
	 * @param methodName      the method name to call on the content owner
	 * @param paramMethodName optional parameter method name
	 * @return list of entities from the content owner method
	 * @throws Exception */
	private <T extends CEntityDB<T>> List<T> resolveDataFromContentOwner(IContentOwner contentOwner, final Class<T> entityType,
			final String methodName, final String paramMethodName) throws Exception {
		Check.notNull(entityType, "Entity type cannot be null");
		LOGGER.debug("Resolving data from content owner {} using method '{}' for entity type: {}", contentOwner, methodName,
				entityType.getSimpleName());
		// Handle null content owner gracefully by returning empty list
		if (contentOwner == null) {
			LOGGER.error("Content owner is null, returning empty list for entity type: {}", entityType.getSimpleName());
			return Collections.emptyList();
		}
		try {
			return callDataMethod(contentOwner, methodName, entityType, paramMethodName);
		} catch (final Exception e) {
			LOGGER.error("Error resolving data from content owner: {}", e.getMessage(), e);
			throw e;
		}
	}

	/** Attempts to call a method with Pageable parameter.
	 * @param <T>         the entity type
	 * @param serviceBean the service bean
	 * @param methodName  the method name to call
	 * @param entityType  the entity type for caching
	 * @param param       optional parameter to pass to the method
	 * @return list of entities or null if method not found/failed
	 * @throws Exception */
	@SuppressWarnings ("unchecked")
	private <T extends CEntityDB<T>> List<T> tryMethodWithPageable(final Object serviceBean, final String methodName, final Class<T> entityType,
			final String param) throws Exception {
		final String cacheKey = serviceBean.getClass().getName() + ":" + methodName + ":pageable:" + entityType.getSimpleName();
		// If param is available, try method with param and Pageable first
		if (param != null && !param.trim().isEmpty()) {
			try {
				final Method methodWithParamAndPageable = getMethodFromCache(cacheKey + ":withparam", () -> {
					try {
						return serviceBean.getClass().getMethod(methodName, String.class, Pageable.class);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}
				});
				if (methodWithParamAndPageable != null) {
					final Pageable pageable = CPageableUtils.createSafe(0, DEFAULT_PAGE_SIZE);
					final Object result = methodWithParamAndPageable.invoke(serviceBean, param, pageable);
					if (result instanceof Page) {
						return ((Page<T>) result).getContent();
					} else if (result instanceof List) {
						return (List<T>) result;
					}
				}
			} catch (final Exception e) {
				LOGGER.debug("Failed to call method '{}' with param and Pageable: {}", methodName, e.getMessage());
			}
			// Try method with param only
			try {
				final Method methodWithParam = getMethodFromCache(cacheKey + ":paramonly", () -> {
					try {
						return serviceBean.getClass().getMethod(methodName, String.class);
					} catch (final NoSuchMethodException e) {
						return null;
					}
				});
				if (methodWithParam != null) {
					final Object result = methodWithParam.invoke(serviceBean, param);
					if (result instanceof List) {
						return (List<T>) result;
					}
				}
			} catch (final Exception e) {
				LOGGER.debug("Failed to call method '{}' with param only: {}", methodName, e.getMessage());
			}
		}
		// Try with Pageable only (original behavior)
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
		// Try with no parameters (fallback for simple methods like getAvailableProjects())
		try {
			final Method methodNoParams = getMethodFromCache(cacheKey + ":noparams", () -> {
				try {
					return serviceBean.getClass().getMethod(methodName);
				} catch (final NoSuchMethodException e) {
					return null;
				}
			});
			if (methodNoParams != null) {
				final Object result = methodNoParams.invoke(serviceBean);
				Check.notNull(result, "Result from method with no parameters cannot be null");
				Check.isTrue(result instanceof List, "Result from method with no parameters must be a List");
				return (List<T>) result;
			}
		} catch (final Exception e) {
			LOGGER.debug("Failed to call method '{}' with no parameters: {}", methodName, e.getMessage());
			throw e;
		}
		return null;
	}
}
