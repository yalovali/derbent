package tech.derbent.plm.agile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.agile.domain.CFeatureType;

public class CPageServiceFeatureType extends CPageServiceDynamicPage<CFeatureType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceFeatureType.class);

	public CPageServiceFeatureType(final IPageServiceImplementer<CFeatureType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CFeatureType");
		super.actionReport();
	}

	@SuppressWarnings ("unused")
	public CComponentWidgetEntity<CFeatureType> buildDataProviderComponentWidget(final CFeatureType entity) {
		return null;
	}
}
