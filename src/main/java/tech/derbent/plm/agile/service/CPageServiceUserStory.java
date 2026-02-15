package tech.derbent.plm.agile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.ISprintItemPageService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.view.CComponentWidgetUserStory;

public class CPageServiceUserStory extends CPageServiceDynamicPage<CUserStory>
		implements IComponentWidgetEntityProvider<CUserStory>, ISprintItemPageService<CUserStory> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceUserStory.class);
	private CProjectItemStatusService statusService;

	public CPageServiceUserStory(final IPageServiceImplementer<CUserStory> view) {
		super(view);
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
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
}
