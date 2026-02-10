package tech.derbent.api.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.api.users.domain.CUser;

public class CPageServiceUser extends CPageServiceDynamicPage<CUser> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceUser.class);
	Long serialVersionUID = 1L;

	public CPageServiceUser(IPageServiceImplementer<CUser> view) {
		super(view);
	}

	@Override
	public void actionCreate() throws Exception {
		super.actionCreate();
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CUser.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CUser.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CUser");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CUser> gridView = (CGridViewBaseDBEntity<CUser>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
