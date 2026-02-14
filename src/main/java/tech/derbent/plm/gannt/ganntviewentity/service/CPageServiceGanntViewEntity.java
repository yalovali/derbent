package tech.derbent.plm.gannt.ganntviewentity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.gannt.ganntviewentity.domain.CGanntViewEntity;

public class CPageServiceGanntViewEntity extends CPageServiceDynamicPage<CGanntViewEntity> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceGanntViewEntity.class);
	Long serialVersionUID = 1L;

	public CPageServiceGanntViewEntity(IPageServiceImplementer<CGanntViewEntity> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CGanntViewEntity");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CGanntViewEntity> gridView = (CGridViewBaseDBEntity<CGanntViewEntity>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
