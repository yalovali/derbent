package tech.derbent.app.products.producttype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.products.producttype.domain.CProductType;

public class CPageServiceProductType extends CPageServiceDynamicPage<CProductType> {

Logger LOGGER = LoggerFactory.getLogger(CPageServiceProductType.class);
Long serialVersionUID = 1L;

public CPageServiceProductType(IPageServiceImplementer<CProductType> view) {
super(view);
}

@Override
public void bind() {
try {
LOGGER.debug("Binding {} to dynamic page for entity {}.", 
this.getClass().getSimpleName(), CProductType.class.getSimpleName());
Check.notNull(view, "View must not be null to bind page service.");
super.bind();
} catch (Exception e) {
LOGGER.error("Error binding {} to dynamic page for entity {}: {}", 
this.getClass().getSimpleName(), CProductType.class.getSimpleName(), e.getMessage());
throw e;
}
}
}
