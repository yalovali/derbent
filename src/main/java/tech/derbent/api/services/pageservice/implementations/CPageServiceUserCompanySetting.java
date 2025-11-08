package tech.derbent.api.services.pageservice.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.base.users.domain.CUserCompanySetting;

public class CPageServiceUserCompanySetting extends CPageServiceDynamicPage<CUserCompanySetting> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceUserCompanySetting.class);
	Long serialVersionUID = 1L;

	public CPageServiceUserCompanySetting(IPageServiceImplementer<CUserCompanySetting> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CUserCompanySetting.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CUserCompanySetting.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
