package tech.derbent.plm.project.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.project.domain.CProject_Derbent;

public class CPageServiceProject_Derbent extends CPageServiceDynamicPage<CProject_Derbent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceProject_Derbent.class);
    private static final Long serialVersionUID = 1L;

    public CPageServiceProject_Derbent(IPageServiceImplementer<CProject_Derbent> view) {
        super(view);
    }

    @Override
    public void bind() {
        try {
            LOGGER.debug("Binding {} to dynamic page for entity {}.", 
                this.getClass().getSimpleName(), CProject_Derbent.class.getSimpleName());
            Check.notNull(getView(), "View must not be null to bind page service.");
            super.bind();
        } catch (Exception e) {
            LOGGER.error("Error binding {} to dynamic page for entity {}: {}", 
                this.getClass().getSimpleName(), CProject_Derbent.class.getSimpleName(),
                e.getMessage());
            throw e;
        }
    }
}
