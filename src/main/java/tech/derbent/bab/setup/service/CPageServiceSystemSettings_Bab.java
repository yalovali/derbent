package tech.derbent.bab.setup.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;

/**
 * CPageServiceSystemSettings_Bab - BAB IoT Gateway system settings page service.
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * 
 * Provides page management functionality for BAB gateway system settings.
 */
@Service
@Profile("bab")
public class CPageServiceSystemSettings_Bab extends CPageServiceDynamicPage<CSystemSettings_Bab> {

    public CPageServiceSystemSettings_Bab(IPageServiceImplementer<CSystemSettings_Bab> view) {
        super(view);
    }
}