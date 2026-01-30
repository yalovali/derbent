package tech.derbent.api.screens.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CMasterSection;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;

public class CPageServiceMasterSection extends CPageServiceDynamicPage<CMasterSection> {

    Logger LOGGER = LoggerFactory.getLogger(CPageServiceMasterSection.class);
    Long serialVersionUID = 1L;

    public CPageServiceMasterSection(IPageServiceImplementer<CMasterSection> view) {
        super(view);
    }

    @Override
    public void bind() {
        try {
            LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CMasterSection.class.getSimpleName());
            Check.notNull(getView(), "View must not be null to bind page service.");
            super.bind();
        } catch (final Exception e) {
            LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CMasterSection.class.getSimpleName(),
                    e.getMessage());
            throw e;
        }
    }

}
