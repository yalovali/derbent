package tech.derbent.api.imports.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.imports.domain.CDataImport;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

public class CPageServiceDataImport extends CPageServiceDynamicPage<CDataImport> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceDataImport.class);

    public CPageServiceDataImport(final IPageServiceImplementer<CDataImport> view) {
        super(view);
    }

    @Override
    public void actionReport() throws Exception {
        LOGGER.debug("Report action triggered for CDataImport");
        if (getView() instanceof final CGridViewBaseDBEntity<CDataImport> gridView) {
            gridView.generateGridReport();
        } else {
            super.actionReport();
        }
    }
}
