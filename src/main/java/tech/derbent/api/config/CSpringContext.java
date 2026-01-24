package tech.derbent.api.config;

import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.utils.Check;
// package tech.derbent.api.config;

@Component
public class CSpringContext implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	// get active profile name
	public static String getActiveProfiles() {
		Check.notNull(CSpringContext.applicationContext, "Application context is not initialized");
		return CSpringContext.applicationContext.getEnvironment().getActiveProfiles().toString();
	}

	public static <T> T getBean(Class<T> type) {
		Check.notNull(CSpringContext.applicationContext, "Application context is not initialized");
		final T result = CSpringContext.applicationContext.getBean(type);
		if (result == null) {
			LoggerFactory.getLogger(CSpringContext.class).error("Bean of type {} not found in application context", type.getName());
		}
		Check.notNull(result, "Bean of type " + type.getName() + " not found in application context");
		return result;
	}

	@SuppressWarnings ("unchecked")
	public static <T> T getBean(String beanName) {
		Check.notNull(CSpringContext.applicationContext, "Application context is not initialized");
		T result;
		try {
			result = (T) CSpringContext.applicationContext.getBean(beanName);
		} catch (@SuppressWarnings ("unused") final Exception e) {
			result = null;
		}
		if (result == null) {
			// final check for profile different
			if (beanName.equals("CProjectService") && CSpringContext.isProfile("derbent")) {
				return (T) CSpringContext.applicationContext.getBean("CProject_DerbentService");
			}
			if (beanName.equals("CProjectService") && CSpringContext.isProfile("bab")) {
				return (T) CSpringContext.applicationContext.getBean("CProject_BabService");
			}
		}
		Check.notNull(result, "Bean of type " + beanName + " not found in application context");
		return result;
	}

	public static <T> Map<String, T> getBeansOfType(Class<T> type) {
		final Map<String, T> beans = applicationContext.getBeansOfType(type);
		return beans;
	}

	public static CAbstractService<?> getServiceClassForEntity(Object entity) {
		final Class<?> entityClass = entity.getClass();
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
		Check.notNull(serviceClass, "Service class not found for entity class " + entityClass.getName());
		final Object serviceBean = getBean(serviceClass);
		return (CAbstractService<?>) serviceBean;
	}

	private static boolean isProfile(String profile) {
		return applicationContext.getEnvironment().acceptsProfiles(Profiles.of(profile));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		/***/
		Check.notNull(applicationContext, "Application context cannot be null");
		CSpringContext.applicationContext = applicationContext;
	}
}
