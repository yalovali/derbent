package tech.derbent.plm.assets.assettype.service;

import tech.derbent.api.utils.Check;
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
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CAssetType.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CAssetType.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
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
