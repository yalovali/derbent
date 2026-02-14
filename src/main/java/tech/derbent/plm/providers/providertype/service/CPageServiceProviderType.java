package tech.derbent.plm.providers.providertype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.providers.providertype.domain.CProviderType;

public class CPageServiceProviderType extends CPageServiceDynamicPage<CProviderType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProviderType.class);
	Long serialVersionUID = 1L;

	public CPageServiceProviderType(IPageServiceImplementer<CProviderType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CProviderType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CProviderType> gridView = (CGridViewBaseDBEntity<CProviderType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
