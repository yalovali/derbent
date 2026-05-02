package tech.derbent.api.dashboard.dashboardprojecttype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.dashboard.dashboardprojecttype.domain.CDashboardProjectType;

public class CPageServiceDashboardProjectType extends CPageServiceDynamicPage<CDashboardProjectType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceDashboardProjectType.class);
	Long serialVersionUID = 1L;

	public CPageServiceDashboardProjectType(final IPageServiceImplementer<CDashboardProjectType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CDashboardProjectType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CDashboardProjectType> gridView = (CGridViewBaseDBEntity<CDashboardProjectType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}
}
