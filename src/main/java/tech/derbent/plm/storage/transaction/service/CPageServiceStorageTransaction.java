package tech.derbent.plm.storage.transaction.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.storage.transaction.domain.CStorageTransaction;

public class CPageServiceStorageTransaction extends CPageServiceDynamicPage<CStorageTransaction> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceStorageTransaction.class);
    public CPageServiceStorageTransaction(final IPageServiceImplementer<CStorageTransaction> view) {
        super(view);
    }

    @Override
    public void bind() {
        LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(),
                CStorageTransaction.class.getSimpleName());
        Check.notNull(getView(), "View must not be null to bind page service.");
        super.bind();
    }
}
