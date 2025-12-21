package tech.derbent.api.annotations;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IHasContentOwner;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.CSessionService;

@Service
public final class CDataProviderResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDataProviderResolver.class);

	@SuppressWarnings ("rawtypes")
	public static Object resolveBean(final String beanName, final IContentOwner contentOwner) throws Exception {
		Object bean;
		// paramBeanName is ok now
		if ("context".equals(beanName)) {
			// just the content owner
			bean = contentOwner;
		} else if ("session".equals(beanName)) {
			bean = CSpringContext.getBean(CSessionService.class);
		} else if ("pageservice".equals(beanName)) {
			Check.instanceOf(contentOwner, IPageServiceImplementer.class,
					"Content owner must implement IPageServiceImplementer to use 'view' as data provider bean");
			bean = ((IPageServiceImplementer) contentOwner).getPageService();
		} else {
			// Get bean from Spring context
			bean = CSpringContext.getBean(beanName);
		}
		Check.notNull(bean, "Data Provider Service bean cannot be null for bean name: " + beanName);
		return bean;
	}

	private final ApplicationContext applicationContext;

	@Autowired
	public CDataProviderResolver(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public List<String> getAvailableServiceBeans() {
		return Arrays.stream(applicationContext.getBeanDefinitionNames()).filter(name -> name.toLowerCase().contains("service")).sorted()
				.collect(Collectors.toList());
	}

	public Component resolveDataComponent(final IContentOwner contentOwner, final EntityFieldInfo fieldInfo) throws Exception {
		try {
			final Object result = resolveMethodAnnotations(null, contentOwner, fieldInfo);
			final Component component = (Component) result;
			if (component instanceof IHasContentOwner) {
				((IHasContentOwner) component).setContentOwner(contentOwner);
			}
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error resolving data for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
	}

	@SuppressWarnings ("unchecked")
	public <T> List<T> resolveDataList(final IContentOwner contentOwner, final EntityFieldInfo fieldInfo) throws Exception {
		try {
			if (fieldInfo.getDataProviderBean().equalsIgnoreCase("none")) {
				return List.of();
			}
			final Object result = resolveMethodAnnotations(null, contentOwner, fieldInfo);
			return (List<T>) result;
		} catch (final Exception e) {
			LOGGER.error("Error resolving data for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
	}

	public Object resolveMethodAnnotations(CEntityDB<?> entity, final IContentOwner contentOwner, final EntityFieldInfo fieldInfo) throws Exception {
		try {
			boolean there_is_param = false;
			Check.notNull(fieldInfo, "Field info cannot be null");
			final String beanName = fieldInfo.getDataProviderBean();
			// Check for "none" sentinel value first - indicates field should not have a data provider
			if (beanName != null && "none".equalsIgnoreCase(beanName.trim())) {
				// dont come here !!! fix it before
				throw new IllegalArgumentException("Data provider bean is set to 'none' for field '" + fieldInfo.getFieldName()
						+ "' - this field should not use a data provider");
			}
			Check.notBlank(beanName, "Data provider owner or bean name cannot be empty");
			Object paramValue = null;
			if (!fieldInfo.getDataProviderParamMethod().isEmpty()) {
				there_is_param = true;
				paramValue = resolveParamValue(entity, contentOwner, fieldInfo);
			}
			final Object bean = resolveBean(beanName, contentOwner);
			Check.notNull(bean, "Data Provider Service bean cannot be null for bean name: " + beanName + " field: " + fieldInfo.getFieldName());
			Object result;
			if (there_is_param) {
				result = CAuxillaries.invokeMethod(bean, fieldInfo.getDataProviderMethod(), paramValue);
			} else {
				result = CAuxillaries.invokeMethod(bean, fieldInfo.getDataProviderMethod());
			}
			Check.notNull(result, "Result from data provider method cannot be null");
			return result;
		} catch (final Exception e) {
			LOGGER.error("Error resolving method annotations for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
	}

	Object resolveParamValue(CEntityDB<?> entity, final IContentOwner contentOwner, final EntityFieldInfo fieldInfo) throws Exception {
		Check.notNull(fieldInfo, "Field info cannot be null");
		Object paramValue = null;
		Object paramBean = null;
		String bName = fieldInfo.getDataProviderParamBean();
		final String paramMethod = fieldInfo.getDataProviderParamMethod();
		if (bName.isEmpty()) {
			bName = fieldInfo.getDataProviderBean();
		}
		final String paramBeanName = bName;
		Check.notBlank(paramBeanName, "Parameter bean name cannot be empty");
		// paramBeanName is ok now
		if ("this".equalsIgnoreCase(paramMethod)) {
			return entity;
		}
		if ("this".equals(paramBeanName)) {
			// just the content owner
			paramBean = this;
		} else if ("context".equals(paramBeanName)) {
			// just the content owner
			paramBean = contentOwner;
		} else if ("session".equals(paramBeanName)) {
			// session service must be ISessionService of CSessionService or CWebSessionService
			// Get the actual session service bean from Spring context
			Check.isTrue(applicationContext.containsBean("CSessionService"),
					"Session service bean 'CSessionService' not found in application context of beans:" + getAvailableServiceBeans());
			paramBean = applicationContext.getBean("CSessionService");
		} else {
			// Get bean from Spring context
			Check.isTrue(applicationContext.containsBean(paramBeanName),
					"Parameter Bean '" + paramBeanName + "' not found in application context of beans:" + getAvailableServiceBeans());
			paramBean = applicationContext.getBean(paramBeanName);
		}
		// param bean must be ok now
		Check.notNull(paramBean, "Parameter Service bean cannot be null for bean name: " + paramBeanName);
		paramValue = CAuxillaries.invokeMethod(paramBean, paramMethod);
		return paramValue;
	}
}
