package tech.derbent.plm.sprints.service;

import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.sprints.domain.CSprintType;

/** CPageServiceSprintType - Page service for Sprint Type management UI. Handles UI events and interactions for sprint type views. */
public class CPageServiceSprintType extends CPageServiceDynamicPage<CSprintType> {

	public CPageServiceSprintType(final IPageServiceImplementer<CSprintType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CSprintType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CSprintType> gridView = (CGridViewBaseDBEntity<CSprintType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
