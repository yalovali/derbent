package tech.derbent.api.config;
// package tech.derbent.api.config;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import tech.derbent.api.utils.Check;

@Component
public class CSpringContext implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	public static <T> T getBean(Class<T> type) {
		Check.notNull(CSpringContext.applicationContext, "Application context is not initialized");
		T result = CSpringContext.applicationContext.getBean(type);
		if (result == null) {
			LoggerFactory.getLogger(CSpringContext.class).error("Bean of type {} not found in application context", type.getName());
		}
		Check.notNull(result, "Bean of type " + type.getName() + " not found in application context");
		return result;
	}

	@SuppressWarnings ("unchecked")
	public static <T> T getBean(String beanName) {
		Check.notNull(CSpringContext.applicationContext, "Application context is not initialized");
		T result = (T) CSpringContext.applicationContext.getBean(beanName);
		if (result == null) {
			LoggerFactory.getLogger(CSpringContext.class).error("Bean of type {} not found in application context", beanName);
		}
		Check.notNull(result, "Bean of type " + beanName + " not found in application context");
		return result;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		/***/
		Check.notNull(applicationContext, "Application context cannot be null");
		CSpringContext.applicationContext = applicationContext;
	}
}
