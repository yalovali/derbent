package tech.derbent.plm.setup.service;

import org.springframework.context.annotation.Profile;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.setup.domain.CSystemSettings;
import tech.derbent.api.setup.service.CPageServiceSystemSettings;
import tech.derbent.plm.setup.domain.CSystemSettings_Derbent;

/** Derbent system settings page service (dynamic page integration). */
@Profile ({
		"derbent", "test", "default"
})
public final class CPageServiceSystemSettings_Derbent extends CPageServiceSystemSettings<CSystemSettings_Derbent> {

	public CPageServiceSystemSettings_Derbent(final IPageServiceImplementer<CSystemSettings_Derbent> view) {
		super(view);
	}

	@Override
	protected CSystemSettings<?> getSystemSettings() {
		return CSpringContext.getBean(CSystemSettings_DerbentService.class).getSystemSettings();
	}
}
