package tech.derbent.plm.setup.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.setup.domain.CSystemSettings_Derbent;

/**
 * CPageServiceSystemSettings_Derbent - Derbent PLM system settings page service.
 * Layer: Service (MVC)
 * Active when: default profile or 'derbent' profile (NOT 'bab' profile)
 * 
 * Provides page management functionality for comprehensive PLM system settings.
 * Follows Derbent pattern: Concrete class marked final.
 */
@Service
@Profile("derbent")
public final class CPageServiceSystemSettings_Derbent extends CPageServiceDynamicPage<CSystemSettings_Derbent> {

    public CPageServiceSystemSettings_Derbent(IPageServiceImplementer<CSystemSettings_Derbent> view) {
        super(view);
    }
}