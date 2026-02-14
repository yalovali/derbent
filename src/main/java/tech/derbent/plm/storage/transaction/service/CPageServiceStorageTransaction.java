package tech.derbent.plm.storage.transaction.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.storage.transaction.domain.CStorageTransaction;

public class CPageServiceStorageTransaction extends CPageServiceDynamicPage<CStorageTransaction> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceStorageTransaction.class);
    public CPageServiceStorageTransaction(final IPageServiceImplementer<CStorageTransaction> view) {
        super(view);
    }

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CStorageTransaction");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CStorageTransaction> gridView = (CGridViewBaseDBEntity<CStorageTransaction>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
