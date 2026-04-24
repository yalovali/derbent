package tech.derbent.plm.agile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.parentrelation.service.CParentRelationService;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.ISprintItemPageService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.view.CComponentAgileChildren;
import tech.derbent.plm.agile.view.CComponentAgileParentSelector;
import tech.derbent.plm.agile.view.CComponentWidgetEpic;

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
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CEpic");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CEpic> gridView = (CGridViewBaseDBEntity<CEpic>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
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
			componentAgileChildren = new CComponentAgileChildren(CSpringContext.getBean(CParentRelationService.class),
					CSpringContext.getBean(CPageEntityService.class), CSpringContext.getBean(ISessionService.class));
		}
		return componentAgileChildren;
	}

	public Component createComponentParent() {
		return new CComponentAgileParentSelector(CSpringContext.getBean(CParentRelationService.class));
	}
}
