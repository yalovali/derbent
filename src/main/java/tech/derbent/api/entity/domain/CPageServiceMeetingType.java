package tech.derbent.api.entity.domain;

import tech.derbent.api.grid.view.CGridViewBaseDBEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.meetings.domain.CMeetingType;

public class CPageServiceMeetingType extends CPageServiceDynamicPage<CMeetingType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceMeetingType.class);
	Long serialVersionUID = 1L;

	public CPageServiceMeetingType(IPageServiceImplementer<CMeetingType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CMeetingType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CMeetingType> gridView = (CGridViewBaseDBEntity<CMeetingType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
