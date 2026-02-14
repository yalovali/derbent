package tech.derbent.api.screens.service;

import tech.derbent.api.grid.view.CGridViewBaseDBEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

public class CPageServiceGridEntity extends CPageServiceDynamicPage<CGridEntity> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceGridEntity.class);
	Long serialVersionUID = 1L;

	public CPageServiceGridEntity(IPageServiceImplementer<CGridEntity> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CGridEntity");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CGridEntity> gridView = (CGridViewBaseDBEntity<CGridEntity>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
