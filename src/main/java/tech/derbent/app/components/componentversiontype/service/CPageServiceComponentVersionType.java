package tech.derbent.app.components.componentversiontype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.components.componentversiontype.domain.CComponentVersionType;

public class CPageServiceComponentVersionType extends CPageServiceDynamicPage<CComponentVersionType> {

Logger LOGGER = LoggerFactory.getLogger(CPageServiceComponentVersionType.class);
Long serialVersionUID = 1L;

public CPageServiceComponentVersionType(IPageServiceImplementer<CComponentVersionType> view) {
super(view);
}

@Override
public void bind() {
try {
LOGGER.debug("Binding {} to dynamic page for entity {}.", 
this.getClass().getSimpleName(), CComponentVersionType.class.getSimpleName());
Check.notNull(view, "View must not be null to bind page service.");
super.bind();
} catch (Exception e) {
LOGGER.error("Error binding {} to dynamic page for entity {}: {}", 
this.getClass().getSimpleName(), CComponentVersionType.class.getSimpleName(), e.getMessage());
throw e;
}
}
}
