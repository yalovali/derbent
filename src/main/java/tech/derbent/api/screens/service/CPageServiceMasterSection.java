package tech.derbent.api.screens.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CMasterSection;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

public class CPageServiceMasterSection extends CPageServiceDynamicPage<CMasterSection> {

    Logger LOGGER = LoggerFactory.getLogger(CPageServiceMasterSection.class);
    Long serialVersionUID = 1L;

    public CPageServiceMasterSection(IPageServiceImplementer<CMasterSection> view) {
        super(view);
    }

}
