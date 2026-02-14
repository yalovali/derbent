package tech.derbent.plm.teams.team.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.teams.team.domain.CTeam;

public class CPageServiceTeam extends CPageServiceDynamicPage<CTeam> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceTeam.class);
	Long serialVersionUID = 1L;

	public CPageServiceTeam(IPageServiceImplementer<CTeam> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CTeam");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CTeam> gridView = (CGridViewBaseDBEntity<CTeam>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
