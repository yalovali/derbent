package tech.derbent.api.page.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

public final class CPageServicePageEntity extends CPageServiceDynamicPage<CPageEntity> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServicePageEntity.class);
	Long serialVersionUID = 1L;

	public CPageServicePageEntity(IPageServiceImplementer<CPageEntity> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CPageEntity");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CPageEntity> gridView = (CGridViewBaseDBEntity<CPageEntity>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}
}
