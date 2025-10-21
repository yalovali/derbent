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
import com.vaadin.flow.component.Component;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IHasContentOwner;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;

@Service
public final class CDataProviderResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDataProviderResolver.class);
	private final ApplicationContext applicationContext;
	/** Cache for resolved bean instances to improve performance. Key format: "beanName" or "className" */
	private final Map<String, Object> beanCache = new ConcurrentHashMap<>();
	/** Cache for resolved methods to improve performance. Key format: "beanName:methodName:entityType" */
	private final Map<String, Method> methodCache = new ConcurrentHashMap<>();

	@Autowired
	public CDataProviderResolver(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void clearCaches() {
		methodCache.clear();
		beanCache.clear();
		LOGGER.info("CDataProviderResolver caches cleared");
	}

	public List<String> getAvailableServiceBeans() {
		return Arrays.stream(applicationContext.getBeanDefinitionNames()).filter(name -> name.toLowerCase().contains("service")).sorted()
				.collect(Collectors.toList());
	}

	private Object getBeanFromCache(final String cacheKey, final Supplier<Object> beanSupplier) {
		return beanCache.computeIfAbsent(cacheKey, e -> {
			final Object bean = beanSupplier.get();
			if (bean != null) {
				// LOGGER.debug("Cached bean for key: {}", cacheKey);
			}
			return bean;
		});
	}

	public String getCacheStats() {
		return String.format("CDataProviderResolver - Method cache: %d entries, Bean cache: %d entries", methodCache.size(), beanCache.size());
	}

	public Component resolveDataComponent(IContentOwner contentOwner, final EntityFieldInfo fieldInfo) throws Exception {
		try {
			Object result = resolveMethodAnnotations(contentOwner, fieldInfo);
			final Component component = (Component) result;
			if (component instanceof IHasContentOwner) {
				((IHasContentOwner) component).setContentOwner(contentOwner);
			}
			return component;
		} catch (Exception e) {
			LOGGER.error("Error resolving data for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
	}

	@SuppressWarnings ("unchecked")
	public <T> List<T> resolveDataList(IContentOwner contentOwner, final EntityFieldInfo fieldInfo) throws Exception {
		try {
			Object result = resolveMethodAnnotations(contentOwner, fieldInfo);
			return (List<T>) result;
		} catch (Exception e) {
			LOGGER.error("Error resolving data for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
	}

	public Object resolveMethodAnnotations(IContentOwner contentOwner, final EntityFieldInfo fieldInfo) throws Exception {
		try {
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
			} else if ("session".equals(beanName)) {
				bean = getBeanFromCache("CSessionService", () -> {
					Check.isTrue(applicationContext.containsBean("CSessionService"),
							"Session service bean 'CSessionService' not found in application context of beans:" + getAvailableServiceBeans());
					return applicationContext.getBean("CSessionService");
				});
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
			return result;
		} catch (Exception e) {
			LOGGER.error("Error resolving method annotations for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
	}

	Object resolveParamValue(IContentOwner contentOwner, final EntityFieldInfo fieldInfo) throws Exception {
		Check.notNull(fieldInfo, "Field info cannot be null");
		Object paramValue = null;
		Object paramBean = null;
		String bName = fieldInfo.getDataProviderParamBean();
		String paramMethod = fieldInfo.getDataProviderParamMethod();
		if (bName.isEmpty()) {
			bName = fieldInfo.getDataProviderBean();
		}
		final String paramBeanName = bName;
		Check.notBlank(paramBeanName, "Parameter bean name cannot be empty");
		// paramBeanName is ok now
		if ("context".equals(paramBeanName)) {
			// just the content owner
			paramBean = contentOwner;
		} else if ("session".equals(paramBeanName)) {
			// session service must be ISessionService of CSessionService or CWebSessionService
			// Get the actual session service bean from Spring context
			paramBean = getBeanFromCache("CSessionService", () -> {
				Check.isTrue(applicationContext.containsBean("CSessionService"),
						"Session service bean 'CSessionService' not found in application context of beans:" + getAvailableServiceBeans());
				return applicationContext.getBean("CSessionService");
			});
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
		paramValue = CAuxillaries.invokeMethod(paramBean, paramMethod);
		return paramValue;
	}
}
