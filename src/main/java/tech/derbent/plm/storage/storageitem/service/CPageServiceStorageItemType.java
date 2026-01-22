package tech.derbent.plm.storage.storageitem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.storage.storageitem.domain.CStorageItemType;

public class CPageServiceStorageItemType extends CPageServiceDynamicPage<CStorageItemType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceStorageItemType.class);
    public CPageServiceStorageItemType(final IPageServiceImplementer<CStorageItemType> view) {
        super(view);
    }

    @Override
    public void bind() {
        LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CStorageItemType.class.getSimpleName());
        Check.notNull(getView(), "View must not be null to bind page service.");
        super.bind();
    }
}
