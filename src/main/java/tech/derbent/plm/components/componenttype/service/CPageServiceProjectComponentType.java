package tech.derbent.plm.components.componenttype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.components.componenttype.domain.CProjectComponentType;

public class CPageServiceProjectComponentType extends CPageServiceDynamicPage<CProjectComponentType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectComponentType.class);
	Long serialVersionUID = 1L;

	public CPageServiceProjectComponentType(IPageServiceImplementer<CProjectComponentType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CProjectComponentType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CProjectComponentType> gridView = (CGridViewBaseDBEntity<CProjectComponentType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
