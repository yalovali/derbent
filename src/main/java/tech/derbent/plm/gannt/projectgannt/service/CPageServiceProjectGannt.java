package tech.derbent.plm.gannt.projectgannt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.gannt.ganntviewentity.domain.CGanntViewEntity;

public class CPageServiceProjectGannt extends CPageServiceDynamicPage<CGanntViewEntity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectGannt.class);
	protected CProjectItemStatusService projectItemStatusService;

	public CPageServiceProjectGannt(final IPageServiceImplementer<CGanntViewEntity> view) {
		super(view);
		// public CPageServiceActivity(final IPageServiceImplementer<CActivity> view) {
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	@Override
	public void actionChangeStatus(final CProjectItemStatus newStatus) { /*****/
	}

	@Override
	public void actionCreate() throws Exception {/**/}

	@Override
	public void actionDelete() throws Exception {/**/}

	@Override
	@SuppressWarnings ({})
	public void actionRefresh() {/**/}

	@Override
	@SuppressWarnings ({})
	public void actionSave() throws Exception {/**/}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CGanntViewEntity");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CGanntViewEntity> gridView = (CGridViewBaseDBEntity<CGanntViewEntity>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
