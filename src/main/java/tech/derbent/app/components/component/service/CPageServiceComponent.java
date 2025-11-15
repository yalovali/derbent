package tech.derbent.app.components.component.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.components.component.domain.CComponent;

public class CPageServiceComponent extends CPageServiceDynamicPage<CComponent> {

Logger LOGGER = LoggerFactory.getLogger(CPageServiceComponent.class);
Long serialVersionUID = 1L;

public CPageServiceComponent(IPageServiceImplementer<CComponent> view) {
super(view);
}

@Override
public void bind() {
try {
LOGGER.debug("Binding {} to dynamic page for entity {}.", 
this.getClass().getSimpleName(), CComponent.class.getSimpleName());
Check.notNull(view, "View must not be null to bind page service.");
super.bind();
} catch (Exception e) {
LOGGER.error("Error binding {} to dynamic page for entity {}: {}", 
this.getClass().getSimpleName(), CComponent.class.getSimpleName(), e.getMessage());
throw e;
}
}
}
