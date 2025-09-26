package tech.derbent.api.ui.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CAbstractNamedEntityPage;
import tech.derbent.page.view.CPageGenericEntity;
import tech.derbent.session.service.CLayoutService;

/** BeanPostProcessor that automatically injects LayoutService into all CAbstractNamedEntityPage and CPageGenericEntity instances. This ensures that
 * all views that extend these base classes can respond to layout changes. */
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
		} else if (bean instanceof CPageGenericEntity) {
			((CPageGenericEntity<?>) bean).setLayoutService(layoutService);
			Check.notNull(layoutService, "LayoutService cannot be null in " + beanName);
		}
		return bean;
	}
}
