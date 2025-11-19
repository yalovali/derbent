package tech.derbent.app.products.productversion.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.products.productversion.domain.CProductVersion;

public class CPageServiceProductVersion extends CPageServiceDynamicPage<CProductVersion> {

Logger LOGGER = LoggerFactory.getLogger(CPageServiceProductVersion.class);
Long serialVersionUID = 1L;

public CPageServiceProductVersion(IPageServiceImplementer<CProductVersion> view) {
super(view);
}

@Override
public void bind() {
try {
LOGGER.debug("Binding {} to dynamic page for entity {}.", 
this.getClass().getSimpleName(), CProductVersion.class.getSimpleName());
Check.notNull(getView(), "View must not be null to bind page service.");
super.bind();
} catch (Exception e) {
LOGGER.error("Error binding {} to dynamic page for entity {}: {}", 
this.getClass().getSimpleName(), CProductVersion.class.getSimpleName(), e.getMessage());
throw e;
}
}
}
