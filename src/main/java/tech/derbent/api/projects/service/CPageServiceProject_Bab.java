package tech.derbent.api.projects.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.projects.domain.CProject_Bab;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;

public class CPageServiceProject_Bab extends CPageServiceDynamicPage<CProject_Bab> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceProject_Bab.class);
    private static final Long serialVersionUID = 1L;

    public CPageServiceProject_Bab(IPageServiceImplementer<CProject_Bab> view) {
        super(view);
    }

    @Override
    public void bind() {
        try {
            LOGGER.debug("Binding {} to dynamic page for entity {}.", 
                this.getClass().getSimpleName(), CProject_Bab.class.getSimpleName());
            Check.notNull(getView(), "View must not be null to bind page service.");
            super.bind();
        } catch (Exception e) {
            LOGGER.error("Error binding {} to dynamic page for entity {}: {}", 
                this.getClass().getSimpleName(), CProject_Bab.class.getSimpleName(),
                e.getMessage());
            throw e;
        }
    }
}
