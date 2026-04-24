package tech.derbent.plm.requirements.requirement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.parentrelation.service.CHierarchyPageSupport;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.agile.view.CComponentAgileChildren;
import tech.derbent.plm.requirements.requirement.domain.CRequirement;

/**
 * Page service for requirements.
 *
 * <p>Requirements expose both parent and children placeholders so mixed hierarchies can be managed
 * from the same detail page.</p>
 */
public class CPageServiceRequirement extends CPageServiceDynamicPage<CRequirement> implements IPageServiceHasStatusAndWorkflow<CRequirement> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceRequirement.class);
	private CComponentAgileChildren componentHierarchyChildren;
	private CProjectItemStatusService statusService;

	public CPageServiceRequirement(final IPageServiceImplementer<CRequirement> view) {
		super(view);
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated reason={}", e.getMessage());
		}
	}

	public Component createComponentParent() {
		// The shared selector ensures requirement pages honor the same type-driven rules as agile pages.
		return CHierarchyPageSupport.createParentComponent();
	}

	public Component createComponentParentChildren() {
		if (componentHierarchyChildren == null) {
			componentHierarchyChildren = (CComponentAgileChildren) CHierarchyPageSupport.createChildrenComponent();
		}
		return componentHierarchyChildren;
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return statusService; }
}
