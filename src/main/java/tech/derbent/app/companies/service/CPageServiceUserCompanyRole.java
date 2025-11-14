package tech.derbent.app.companies.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.roles.domain.CUserCompanyRole;

public class CPageServiceUserCompanyRole extends CPageServiceDynamicPage<CUserCompanyRole> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceUserCompanyRole.class);
	Long serialVersionUID = 1L;

	public CPageServiceUserCompanyRole(IPageServiceImplementer<CUserCompanyRole> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CUserCompanyRole.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CUserCompanyRole.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
