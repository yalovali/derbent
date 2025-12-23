package tech.derbent.api.config;

import tech.derbent.api.utils.Check;
// package tech.derbent.api.config;

import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class CSpringContext implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

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
		final T result = (T) CSpringContext.applicationContext.getBean(beanName);
		if (result == null) {
			LoggerFactory.getLogger(CSpringContext.class).error("Bean of type {} not found in application context", beanName);
		}
		Check.notNull(result, "Bean of type " + beanName + " not found in application context");
		return result;
	}

	public static <T> Map<String, T> getBeansOfType(Class<T> type) {
		final Map<String, T> beans = applicationContext.getBeansOfType(type);
		return beans;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		/***/
		Check.notNull(applicationContext, "Application context cannot be null");
		CSpringContext.applicationContext = applicationContext;
	}
}
