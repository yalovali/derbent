package tech.derbent.api.entity.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.page.domain.CPageEntityType;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

public class CPageServicePageEntityType extends CPageServiceDynamicPage<CPageEntityType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServicePageEntityType.class);
	Long serialVersionUID = 1L;

	public CPageServicePageEntityType(final IPageServiceImplementer<CPageEntityType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CPageEntityType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CPageEntityType> gridView = (CGridViewBaseDBEntity<CPageEntityType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}
}
