package tech.derbent.plm.deliverables.deliverabletype.service;

import tech.derbent.api.grid.view.CGridViewBaseDBEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.deliverables.deliverabletype.domain.CDeliverableType;

public class CPageServiceDeliverableType extends CPageServiceDynamicPage<CDeliverableType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceDeliverableType.class);
	Long serialVersionUID = 1L;

	public CPageServiceDeliverableType(IPageServiceImplementer<CDeliverableType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CDeliverableType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CDeliverableType> gridView = (CGridViewBaseDBEntity<CDeliverableType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
