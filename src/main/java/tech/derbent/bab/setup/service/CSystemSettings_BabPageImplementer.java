package tech.derbent.bab.setup.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.setup.service.CSystemSettingsPageImplementer;

/**
 * CSystemSettings_BabPageImplementer - Page implementer for BAB system settings.
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * 
 * Provides the IPageServiceImplementer interface implementation for BAB gateway system settings pages.
 * Extends base CSystemSettingsPageImplementer with all common boilerplate.
 * Follows Derbent pattern: Concrete class marked final.
 */
@Service
@Profile("bab")
public final class CSystemSettings_BabPageImplementer extends CSystemSettingsPageImplementer<CSystemSettings_Bab> {

    public CSystemSettings_BabPageImplementer(
            final CSystemSettings_BabService systemSettingsService, 
            final ISessionService sessionService) {
        super(systemSettingsService, sessionService, CSystemSettings_Bab.class);
    }

    @Override
    public Class<?> getEntityClass() {
        return CSystemSettings_Bab.class;
    }
}