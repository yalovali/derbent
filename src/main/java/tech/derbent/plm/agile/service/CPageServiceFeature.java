package tech.derbent.plm.agile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.parentrelation.service.CHierarchyPageSupport;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.ISprintItemPageService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.view.CComponentAgileChildren;
import tech.derbent.plm.agile.view.CComponentWidgetFeature;

/**
 * Dynamic page service for feature pages.
 *
 * <p>Shared hierarchy components keep level-based parent and child rules in one place, so this
 * class only caches the child component and exposes the feature widget.</p>
 */
public class CPageServiceFeature extends CPageServiceDynamicPage<CFeature>
		implements IComponentWidgetEntityProvider<CFeature>, ISprintItemPageService<CFeature> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceFeature.class);
	private CComponentAgileChildren componentAgileChildren;
	private CProjectItemStatusService statusService;

	public CPageServiceFeature(final IPageServiceImplementer<CFeature> view) {
		super(view);
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated reason={}", e.getMessage());
		}
	}

	@Override
	public CComponentWidgetEntity<CFeature> buildDataProviderComponentWidget(final CFeature entity) {
		return new CComponentWidgetFeature(entity);
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return statusService; }

	@Override
	public Component getSprintItemWidget(final CFeature entity) {
		return new CComponentWidgetFeature(entity);
	}

	public Component createComponentParentChildren() {
		if (componentAgileChildren == null) {
			componentAgileChildren = (CComponentAgileChildren) CHierarchyPageSupport.createChildrenComponent();
		}
		return componentAgileChildren;
	}

	public Component createComponentParent() {
		// Features now reuse the shared hierarchy selector so level rules stay centralized.
		return CHierarchyPageSupport.createParentComponent();
	}
}
