package tech.derbent.base.ui.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CAbstractNamedEntityPage;
import tech.derbent.session.service.CLayoutService;

/** BeanPostProcessor that automatically injects LayoutService into all CAbstractMDPage instances. This ensures that all views that extend
 * CAbstractMDPage can respond to layout changes. */
@Component
public class LayoutServiceInjector implements BeanPostProcessor {

	private final CLayoutService layoutService;

	public LayoutServiceInjector(final CLayoutService layoutService) {
		this.layoutService = layoutService;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
		if (bean instanceof CAbstractNamedEntityPage) {
			((CAbstractNamedEntityPage<?>) bean).setLayoutService(layoutService);
			Check.notNull(layoutService, "LayoutService cannot be null in " + beanName);
		}
		return bean;
	}
}
