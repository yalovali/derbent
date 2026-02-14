package tech.derbent.plm.storage.storage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.storage.storage.domain.CStorage;

public class CPageServiceStorage extends CPageServiceDynamicPage<CStorage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceStorage.class);
    public CPageServiceStorage(final IPageServiceImplementer<CStorage> view) {
        super(view);
    }

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CStorage");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CStorage> gridView = (CGridViewBaseDBEntity<CStorage>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
