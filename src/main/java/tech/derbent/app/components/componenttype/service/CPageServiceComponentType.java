package tech.derbent.app.components.componenttype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.components.componenttype.domain.CComponentType;

public class CPageServiceComponentType extends CPageServiceDynamicPage<CComponentType> {

Logger LOGGER = LoggerFactory.getLogger(CPageServiceComponentType.class);
Long serialVersionUID = 1L;

public CPageServiceComponentType(IPageServiceImplementer<CComponentType> view) {
super(view);
}

@Override
public void bind() {
try {
LOGGER.debug("Binding {} to dynamic page for entity {}.", 
this.getClass().getSimpleName(), CComponentType.class.getSimpleName());
Check.notNull(view, "View must not be null to bind page service.");
super.bind();
} catch (Exception e) {
LOGGER.error("Error binding {} to dynamic page for entity {}: {}", 
this.getClass().getSimpleName(), CComponentType.class.getSimpleName(), e.getMessage());
throw e;
}
}
}
