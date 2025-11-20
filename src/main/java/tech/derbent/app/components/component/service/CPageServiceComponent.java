package tech.derbent.app.components.component.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.components.component.domain.CComponent;

public class CPageServiceComponent extends CPageServiceDynamicPage<CComponent> implements IPageServiceHasStatusAndWorkflow<CComponent> {

Logger LOGGER = LoggerFactory.getLogger(CPageServiceComponent.class);
Long serialVersionUID = 1L;

// Declare the field required by the interface
private CProjectItemStatusService projectItemStatusService;

public CPageServiceComponent(IPageServiceImplementer<CComponent> view) {
super(view);
// Initialize the service from Spring context
try {
projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
} catch (Exception e) {
LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
}
}

@Override
public void bind() {
try {
LOGGER.debug("Binding {} to dynamic page for entity {}.", 
this.getClass().getSimpleName(), CComponent.class.getSimpleName());
Check.notNull(getView(), "View must not be null to bind page service.");
super.bind();
} catch (Exception e) {
LOGGER.error("Error binding {} to dynamic page for entity {}: {}", 
this.getClass().getSimpleName(), CComponent.class.getSimpleName(), e.getMessage());
throw e;
}
}

@Override
public CProjectItemStatusService getProjectItemStatusService() {
return projectItemStatusService;
}
}
