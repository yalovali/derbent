package tech.derbent.plm.storage.storagetype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.storage.storagetype.domain.CStorageType;

public class CPageServiceStorageType extends CPageServiceDynamicPage<CStorageType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceStorageType.class);
    public CPageServiceStorageType(final IPageServiceImplementer<CStorageType> view) {
        super(view);
    }

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CStorageType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CStorageType> gridView = (CGridViewBaseDBEntity<CStorageType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
