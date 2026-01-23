package tech.derbent.api.companies.service;

import tech.derbent.api.utils.Check;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.companies.domain.CCompany;

public class CPageServiceCompany extends CPageServiceDynamicPage<CCompany> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceCompany.class);
	Long serialVersionUID = 1L;

	public CPageServiceCompany(IPageServiceImplementer<CCompany> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CCompany.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CCompany.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CCompany");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CCompany> gridView = (CGridViewBaseDBEntity<CCompany>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
