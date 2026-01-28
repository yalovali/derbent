package tech.derbent.plm.setup.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.setup.service.CSystemSettingsPageImplementer;
import tech.derbent.plm.setup.domain.CSystemSettings_Derbent;

/**
 * CSystemSettings_DerbentPageImplementer - Page implementer for Derbent system settings.
 * Layer: Service (MVC)
 * Active when: default profile or 'derbent' profile (NOT 'bab' profile)
 * 
 * Provides the IPageServiceImplementer interface implementation for Derbent PLM system settings pages.
 * Extends base CSystemSettingsPageImplementer with all common boilerplate.
 * Follows Derbent pattern: Concrete class marked final.
 */
@Service
@Profile({"derbent", "default"})
public final class CSystemSettings_DerbentPageImplementer extends CSystemSettingsPageImplementer<CSystemSettings_Derbent> {

    public CSystemSettings_DerbentPageImplementer(
            final CSystemSettings_DerbentService systemSettingsService, 
            final ISessionService sessionService) {
        super(systemSettingsService, sessionService, CSystemSettings_Derbent.class);
    }

    @Override
    public Class<?> getEntityClass() {
        return CSystemSettings_Derbent.class;
    }
}