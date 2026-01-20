package tech.derbent.plm.storage.storage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.storage.storage.domain.CStorage;

public class CPageServiceStorage extends CPageServiceDynamicPage<CStorage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceStorage.class);
    private static final long serialVersionUID = 1L;

    public CPageServiceStorage(final IPageServiceImplementer<CStorage> view) {
        super(view);
    }

    @Override
    public void bind() {
        LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CStorage.class.getSimpleName());
        Check.notNull(getView(), "View must not be null to bind page service.");
        super.bind();
    }
}
