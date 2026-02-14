package tech.derbent.plm.assets.assettype.service;

import tech.derbent.api.grid.view.CGridViewBaseDBEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.assets.assettype.domain.CAssetType;

public class CPageServiceAssetType extends CPageServiceDynamicPage<CAssetType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceAssetType.class);
	Long serialVersionUID = 1L;

	public CPageServiceAssetType(IPageServiceImplementer<CAssetType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CAssetType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CAssetType> gridView = (CGridViewBaseDBEntity<CAssetType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
