package tech.derbent.base.setup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.base.setup.domain.CSystemSettings;

public class CPageServiceSystemSettings extends CPageServiceDynamicPage<CSystemSettings> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceSystemSettings.class);
	Long serialVersionUID = 1L;

	public CPageServiceSystemSettings(IPageServiceImplementer<CSystemSettings> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CSystemSettings.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CSystemSettings.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CSystemSettings");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CSystemSettings> gridView = (CGridViewBaseDBEntity<CSystemSettings>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
