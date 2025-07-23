package tech.derbent.base.ui.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.session.service.LayoutService;

/**
 * BeanPostProcessor that automatically injects LayoutService into all CAbstractMDPage instances.
 * This ensures that all views that extend CAbstractMDPage can respond to layout changes.
 */
@Component
public class LayoutServiceInjector implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutServiceInjector.class);
    private final LayoutService layoutService;

    public LayoutServiceInjector(final LayoutService layoutService) {
        LOGGER.debug("Creating LayoutServiceInjector with layoutService: {}", 
                    layoutService != null ? layoutService.getClass().getSimpleName() : "null");
        this.layoutService = layoutService;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        LOGGER.debug("Processing bean: {} (type: {})", beanName, 
                    bean != null ? bean.getClass().getSimpleName() : "null");
        
        if (bean instanceof CAbstractMDPage) {
            if (layoutService != null) {
                ((CAbstractMDPage<?>) bean).setLayoutService(layoutService);
                LOGGER.debug("Injected LayoutService into CAbstractMDPage: {}", beanName);
            } else {
                LOGGER.warn("Cannot inject LayoutService into {} - layoutService is null", beanName);
            }
        }
        return bean;
    }
}