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
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.view.CComponentAgileChildren;
import tech.derbent.plm.agile.view.CComponentWidgetEpic;

/**
 * Dynamic page service for epics.
 *
 * <p>The page keeps only widget and hierarchy-component wiring because CSV export and generic page
 * actions already live in {@link CPageServiceDynamicPage}.</p>
 */
public class CPageServiceEpic extends CPageServiceDynamicPage<CEpic> implements IComponentWidgetEntityProvider<CEpic>, ISprintItemPageService<CEpic> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceEpic.class);
	private CComponentAgileChildren componentAgileChildren;
	private CProjectItemStatusService statusService;

	public CPageServiceEpic(final IPageServiceImplementer<CEpic> view) {
		super(view);
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated reason={}", e.getMessage());
		}
	}

	@Override
	public CComponentWidgetEntity<CEpic> buildDataProviderComponentWidget(final CEpic entity) {
		return new CComponentWidgetEpic(entity);
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return statusService; }

	@Override
	public Component getSprintItemWidget(final CEpic entity) {
		return new CComponentWidgetEpic(entity);
	}

	public Component createComponentParentChildren() {
		if (componentAgileChildren == null) {
			componentAgileChildren = (CComponentAgileChildren) CHierarchyPageSupport.createChildrenComponent();
		}
		return componentAgileChildren;
	}

	public Component createComponentParent() {
		// Epics still use the same placeholder contract, but the implementation is now fully type-driven.
		return CHierarchyPageSupport.createParentComponent();
	}
}
