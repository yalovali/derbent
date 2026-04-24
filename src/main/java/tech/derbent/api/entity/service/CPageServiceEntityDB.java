package tech.derbent.api.entity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import tech.derbent.api.parentrelation.service.CParentRelationService;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.CComponentAgileParentSelector;
import tech.derbent.plm.activities.service.CActivityService;

public class CPageServiceEntityDB<EntityClass extends CEntityDB<EntityClass>> extends CPageServiceDynamicPage<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceEntityDB.class);

	public CPageServiceEntityDB(IPageServiceImplementer<EntityClass> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for EntityClass");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<EntityClass> gridView = (CGridViewBaseDBEntity<EntityClass>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

	/** Fallback factory for agile parent selector used by generic tooling pages (e.g. DetailSection preview). */
	public Component createComponentParent() {
		try {
			return new CComponentAgileParentSelector(CSpringContext.getBean(CActivityService.class),
					CSpringContext.getBean(CParentRelationService.class));
		} catch (final Exception e) {
			LOGGER.warn("Agile parent component not available: {}", e.getMessage());
			return new Span("Agile parent selector not available.");
		}
	}
}
