package tech.derbent.app.components.componentversion.service;

import tech.derbent.api.utils.Check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.app.components.componentversion.domain.CProjectComponentVersion;

public class CPageServiceComponentVersion extends CPageServiceDynamicPage<CProjectComponentVersion>
		implements IPageServiceHasStatusAndWorkflow<CProjectComponentVersion> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceComponentVersion.class);
	// Declare the field required by the interface
	private CProjectItemStatusService projectItemStatusService;
	Long serialVersionUID = 1L;

	public CPageServiceComponentVersion(IPageServiceImplementer<CProjectComponentVersion> view) {
		super(view);
		// Initialize the service from Spring context
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(),
					CProjectComponentVersion.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CProjectComponentVersion.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return projectItemStatusService; }
}
