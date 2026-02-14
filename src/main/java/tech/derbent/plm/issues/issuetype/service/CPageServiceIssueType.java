package tech.derbent.plm.issues.issuetype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.issues.issuetype.domain.CIssueType;

public class CPageServiceIssueType extends CPageServiceDynamicPage<CIssueType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceIssueType.class);
	Long serialVersionUID = 1L;

	public CPageServiceIssueType(IPageServiceImplementer<CIssueType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CIssueType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CIssueType> gridView = (CGridViewBaseDBEntity<CIssueType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
