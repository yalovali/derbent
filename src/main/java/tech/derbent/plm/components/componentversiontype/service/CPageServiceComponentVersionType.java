package tech.derbent.plm.components.componentversiontype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.components.componentversiontype.domain.CProjectComponentVersionType;

public class CPageServiceComponentVersionType extends CPageServiceDynamicPage<CProjectComponentVersionType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceComponentVersionType.class);
	Long serialVersionUID = 1L;

	public CPageServiceComponentVersionType(IPageServiceImplementer<CProjectComponentVersionType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CProjectComponentVersionType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CProjectComponentVersionType> gridView = (CGridViewBaseDBEntity<CProjectComponentVersionType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
