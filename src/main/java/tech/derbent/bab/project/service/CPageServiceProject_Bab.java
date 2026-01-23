package tech.derbent.bab.project.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.project.domain.CProject_Bab;

public class CPageServiceProject_Bab extends CPageServiceDynamicPage<CProject_Bab> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceProject_Bab.class);
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

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CProject_Bab");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CProject_Bab> gridView = (CGridViewBaseDBEntity<CProject_Bab>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
