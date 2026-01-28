package tech.derbent.plm.setup.service;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entity.service.CPageServiceEntityDB;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.views.CDetailsBuilder;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.setup.domain.CSystemSettings_Derbent;

/**
 * CSystemSettings_DerbentPageImplementer - Page implementer for Derbent system settings.
 * Layer: Service (MVC)
 * Active when: default profile or 'derbent' profile (NOT 'bab' profile)
 * 
 * Provides the IPageServiceImplementer interface implementation for system settings pages.
 * Follows the same pattern as other entity page implementers.
 */
@Service
@Profile({"derbent", "default"})
public class CSystemSettings_DerbentPageImplementer implements IPageServiceImplementer<CSystemSettings_Derbent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettings_DerbentPageImplementer.class);

    private final CSystemSettings_DerbentService systemSettingsService;
    private final ISessionService sessionService;
    private final CEnhancedBinder<CSystemSettings_Derbent> binder;
    private final Map<String, Component> componentMap;
    private CSystemSettings_Derbent currentValue;
    private final CPageService<CSystemSettings_Derbent> pageService;
    
    public CSystemSettings_DerbentPageImplementer(final CSystemSettings_DerbentService systemSettingsService, final ISessionService sessionService) {
        this.systemSettingsService = systemSettingsService;
        this.sessionService = sessionService;
        this.binder = new CEnhancedBinder<>(CSystemSettings_Derbent.class);
        this.componentMap = new HashMap<>();
        this.pageService = new CPageServiceEntityDB<>(this);
    }

    @Override
    public CEntityDB<?> createNewEntityInstance() throws Exception {
        LOGGER.debug("Creating new CSystemSettings_Derbent entity instance");
        return systemSettingsService.newEntity();
    }

    @Override
    public CEnhancedBinder<CSystemSettings_Derbent> getBinder() {
        return binder;
    }

    @Override
    public Map<String, Component> getComponentMap() {
        return componentMap;
    }

    @Override
    public String getCurrentEntityIdString() {
        return currentValue == null ? "null" : String.valueOf(currentValue.getId());
    }

    @Override
    public CDetailsBuilder getDetailsBuilder() {
        return null; // Not used in system settings
    }

    @Override
    public Class<?> getEntityClass() {
        return CSystemSettings_Derbent.class;
    }

    @Override
    public CAbstractService<CSystemSettings_Derbent> getEntityService() {
        return systemSettingsService;
    }

    @Override
    public CPageService<CSystemSettings_Derbent> getPageService() {
        return pageService;
    }

    @Override
    public ISessionService getSessionService() {
        return sessionService;
    }

    @Override
    public CSystemSettings_Derbent getValue() {
        return currentValue;
    }

    @Override
    public void onEntityCreated(final CSystemSettings_Derbent entity) throws Exception {
        LOGGER.debug("Entity created: {}", entity.getId());
    }

    @Override
    public void onEntityDeleted(final CSystemSettings_Derbent entity) throws Exception {
        LOGGER.debug("Entity deleted: {}", entity.getId());
    }

    @Override
    public void onEntitySaved(final CSystemSettings_Derbent entity) throws Exception {
        LOGGER.debug("Entity saved: {}", entity.getId());
    }

    @Override
    public void populateForm() throws Exception {
        // Form population logic would go here
    }

    @Override
    public void selectFirstInGrid() {
        // Grid selection logic would go here
    }

    @Override
    public void setValue(final CEntityDB<?> entity) {
        this.currentValue = (CSystemSettings_Derbent) entity;
    }
}