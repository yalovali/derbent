package tech.derbent.api.annotations;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.utils.CAuxillaries;
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

	/** Clears all caches. Useful for testing or when bean configuration changes. */
	public void clearCaches() {
		methodCache.clear();
		beanCache.clear();
		LOGGER.info("CDataProviderResolver caches cleared");
	}

	/** Debug method to list all available service beans in the application context. Useful for troubleshooting data provider resolution issues.
	 * @return list of all bean names that end with "Service" */
	public List<String> getAvailableServiceBeans() {
		return Arrays.stream(applicationContext.getBeanDefinitionNames()).filter(name -> name.toLowerCase().contains("service")).sorted()
				.collect(Collectors.toList());
	}

	/** Retrieves a bean from cache or computes it using the supplier.
	 * @param cacheKey     the cache key
	 * @param beanSupplier supplier to compute the bean if not cached
	 * @return the bean or null if not found */
	private Object getBeanFromCache(final String cacheKey, final Supplier<Object> beanSupplier) {
		return beanCache.computeIfAbsent(cacheKey, _ -> {
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

	Object resolveParamValue(IContentOwner contentOwner, final EntityFieldInfo fieldInfo) throws Exception {
		Check.notNull(fieldInfo, "Field info cannot be null");
		Object paramValue = null;
		Object paramBean = null;
		String bName = fieldInfo.getDataProviderParamBean();
		if (bName.isEmpty()) {
			bName = fieldInfo.getDataProviderBean();
		}
		final String paramBeanName = bName;
		Check.notBlank(paramBeanName, "Parameter bean name cannot be empty");
		// paramBeanName is ok now
		if ("context".equals(paramBeanName)) {
			// just the content owner
			paramBean = contentOwner;
		} else {
			// Get bean from Spring context with caching
			paramBean = getBeanFromCache(paramBeanName, () -> {
				Check.isTrue(applicationContext.containsBean(paramBeanName),
						"Parameter Bean '" + paramBeanName + "' not found in application context of beans:" + getAvailableServiceBeans());
				return applicationContext.getBean(paramBeanName);
			});
		}
		// param bean must be ok now
		Check.notNull(paramBean, "Parameter Service bean cannot be null for bean name: " + paramBeanName);
		paramValue = CAuxillaries.invokeMethod(contentOwner, fieldInfo.getDataProviderParamMethod());
		return paramValue;
	}

	@SuppressWarnings ("unchecked")
	public <T extends CEntityDB<T>> List<T> resolveData(IContentOwner contentOwner, final EntityFieldInfo fieldInfo) throws Exception {
		Check.notNull(fieldInfo, "Field info cannot be null");
		Object paramValue = null;
		if (!fieldInfo.getDataProviderParamMethod().isEmpty()) {
			paramValue = resolveParamValue(contentOwner, fieldInfo);
		}
		final String beanName = fieldInfo.getDataProviderBean();
		Check.notBlank(beanName, "Data provider owner or bean name cannot be empty");
		Object bean;
		// paramBeanName is ok now
		if ("context".equals(beanName)) {
			// just the content owner
			bean = contentOwner;
		} else {
			// Get bean from Spring context with caching
			bean = getBeanFromCache(beanName, () -> {
				Check.isTrue(applicationContext.containsBean(beanName),
						"Parameter Bean '" + beanName + "' not found in application context of beans:" + getAvailableServiceBeans());
				return applicationContext.getBean(beanName);
			});
		}
		Object result = CAuxillaries.invokeMethod(bean, fieldInfo.getDataProviderMethod(), paramValue);
		Check.notNull(result, "Result from data provider method cannot be null");
		return (List<T>) result;
	}
}
