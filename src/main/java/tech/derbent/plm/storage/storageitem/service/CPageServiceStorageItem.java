package tech.derbent.plm.storage.storageitem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;

public class CPageServiceStorageItem extends CPageServiceDynamicPage<CStorageItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceStorageItem.class);
    public CPageServiceStorageItem(final IPageServiceImplementer<CStorageItem> view) {
        super(view);
    }

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CStorageItem");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CStorageItem> gridView = (CGridViewBaseDBEntity<CStorageItem>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
