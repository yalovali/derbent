package tech.derbent.api.projects.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.projects.domain.CProjectType;

public class CPageServiceProjectType extends CPageServiceDynamicPage<CProjectType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectType.class);
	Long serialVersionUID = 1L;

	public CPageServiceProjectType(IPageServiceImplementer<CProjectType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CProjectType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CProjectType> gridView = (CGridViewBaseDBEntity<CProjectType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
