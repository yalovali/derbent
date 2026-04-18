package tech.derbent.plm.agile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.agileparentrelation.service.CAgileParentRelationService;
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
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.view.CComponentAgileChildren;
import tech.derbent.plm.agile.view.CComponentAgileParentSelector;
import tech.derbent.plm.agile.view.CComponentWidgetUserStory;

public class CPageServiceUserStory extends CPageServiceDynamicPage<CUserStory>
		implements IComponentWidgetEntityProvider<CUserStory>, ISprintItemPageService<CUserStory> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceUserStory.class);
	private CComponentAgileChildren componentAgileChildren;
	private CProjectItemStatusService statusService;

	public CPageServiceUserStory(final IPageServiceImplementer<CUserStory> view) {
		super(view);
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated reason={}", e.getMessage());
		}
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CUserStory");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CUserStory> gridView = (CGridViewBaseDBEntity<CUserStory>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

	@Override
	public CComponentWidgetEntity<CUserStory> buildDataProviderComponentWidget(final CUserStory entity) {
		return new CComponentWidgetUserStory(entity);
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return statusService; }

	@Override
	public Component getSprintItemWidget(final CUserStory entity) {
		return new CComponentWidgetUserStory(entity);
	}

	public Component createComponentAgileChildren() {
		if (componentAgileChildren == null) {
			componentAgileChildren = new CComponentAgileChildren(CSpringContext.getBean(CAgileParentRelationService.class),
					CSpringContext.getBean(CPageEntityService.class), CSpringContext.getBean(ISessionService.class));
		}
		return componentAgileChildren;
	}

	public Component createComponentAgileParent() {
		return new CComponentAgileParentSelector(CSpringContext.getBean(CAgileParentRelationService.class));
	}
}
