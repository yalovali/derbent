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
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.view.CComponentWidgetFeature;

public class CPageServiceFeature extends CPageServiceDynamicPage<CFeature>
		implements IPageServiceHasStatusAndWorkflow<CFeature>, IComponentWidgetEntityProvider<CFeature>, ISprintItemPageService<CFeature> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceFeature.class);
	private CProjectItemStatusService statusService;
	private static final long serialVersionUID = 1L;

	public CPageServiceFeature(final IPageServiceImplementer<CFeature> view) {
		super(view);
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CFeature");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CFeature> gridView = (CGridViewBaseDBEntity<CFeature>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

	@Override
	public CComponentWidgetEntity<CFeature> getComponentWidget(final CFeature entity) {
		return new CComponentWidgetFeature(entity);
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return statusService; }

	@Override
	public Component getSprintItemWidget(final CFeature entity) {
		return new CComponentWidgetFeature(entity);
	}
}
