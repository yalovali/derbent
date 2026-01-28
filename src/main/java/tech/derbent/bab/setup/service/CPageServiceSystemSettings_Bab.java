package tech.derbent.bab.setup.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;
import tech.derbent.base.setup.service.CPageServiceSystemSettings;

/**
 * CPageServiceSystemSettings_Bab - BAB IoT Gateway system settings page service.
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * 
 * Provides page management functionality for BAB gateway system settings.
 * Follows Derbent pattern: Concrete class marked final.
 */
@Service
@Profile("bab")
public final class CPageServiceSystemSettings_Bab extends CPageServiceSystemSettings<CSystemSettings_Bab> {

    public CPageServiceSystemSettings_Bab(IPageServiceImplementer<CSystemSettings_Bab> view) {
        super(view);
    }
}
