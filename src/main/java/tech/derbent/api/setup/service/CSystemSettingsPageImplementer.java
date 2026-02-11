package tech.derbent.api.setup.service;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.authentication.component.CComponentLdapTest;
import tech.derbent.api.authentication.service.CLdapAuthenticator;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entity.service.CPageServiceEntityDB;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.views.CDetailsBuilder;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.setup.domain.CSystemSettings;

/**
 * CSystemSettingsPageImplementer - Abstract base page implementer for system settings.
 * Layer: Service (MVC)
 * 
 * Base page implementer for all CSystemSettings variants.
 * Extracts common boilerplate from concrete implementations.
 * Follows Derbent pattern: Abstract base with NO @Service annotation.
 * 
 * Concrete implementations: CSystemSettings_BabPageImplementer, CSystemSettings_DerbentPageImplementer
 */
public abstract class CSystemSettingsPageImplementer<SettingsClass extends CSystemSettings<SettingsClass>> 
        implements IPageServiceImplementer<SettingsClass> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettingsPageImplementer.class);

    protected final CSystemSettingsService<SettingsClass> systemSettingsService;
    protected final ISessionService sessionService;
    protected final CEnhancedBinder<SettingsClass> binder;
    protected final Map<String, Component> componentMap;
    protected SettingsClass currentValue;
    protected final CPageService<SettingsClass> pageService;

    protected CSystemSettingsPageImplementer(
            final CSystemSettingsService<SettingsClass> systemSettingsService, 
            final ISessionService sessionService,
            final Class<SettingsClass> entityClass) {
        this.systemSettingsService = systemSettingsService;
        this.sessionService = sessionService;
        this.binder = new CEnhancedBinder<>(entityClass);
        this.componentMap = new HashMap<>();
        this.pageService = new CPageServiceEntityDB<>(this);
    }

    @Override
    public CEntityDB<?> createNewEntityInstance() throws Exception {
        LOGGER.debug("Creating new {} entity instance", getEntityClass().getSimpleName());
        return systemSettingsService.newEntity();
    }

    @Override
    public final CEnhancedBinder<SettingsClass> getBinder() {
        return binder;
    }

    @Override
    public final Map<String, Component> getComponentMap() {
        return componentMap;
    }

    @Override
    public final String getCurrentEntityIdString() {
        return currentValue == null ? "null" : String.valueOf(currentValue.getId());
    }

    @Override
    public CDetailsBuilder getDetailsBuilder() {
        return null; // Not used in system settings
    }

    @Override
    public final CAbstractService<SettingsClass> getEntityService() {
        return systemSettingsService;
    }

    @Override
    public final CPageService<SettingsClass> getPageService() {
        return pageService;
    }

    @Override
    public final ISessionService getSessionService() {
        return sessionService;
    }

    @Override
    public final SettingsClass getValue() {
        return currentValue;
    }

    @Override
    public void onEntityCreated(final SettingsClass entity) throws Exception {
        LOGGER.debug("Entity created: {}", entity.getId());
    }

    @Override
    public void onEntityDeleted(final SettingsClass entity) throws Exception {
        LOGGER.debug("Entity deleted: {}", entity.getId());
    }

    @Override
    public void onEntitySaved(final SettingsClass entity) throws Exception {
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
    @SuppressWarnings("unchecked")
    public final void setValue(final CEntityDB<?> entity) {
        this.currentValue = (SettingsClass) entity;
    }

    /**
     * Creates LDAP test component for system settings form.
     * Called by CFormBuilder when building form from @AMetaData.
     * 
     * @return CComponentLdapTest for LDAP authentication testing
     */
    public Component createComponentLdapTest() {
        try {
            LOGGER.debug("Creating LDAP test component");
            
            // Get LDAP authenticator from Spring context
            final CLdapAuthenticator ldapAuthenticator = CSpringContext.getBean(CLdapAuthenticator.class);
            
            // Create component with authenticator
            final CComponentLdapTest component = new CComponentLdapTest(ldapAuthenticator);
            
            LOGGER.debug("LDAP test component created successfully");
            return component;
        } catch (final Exception e) {
            LOGGER.error("Failed to create LDAP test component: {}", e.getMessage());
            CNotificationService.showException("Failed to load LDAP test component", e);
            return CDiv.errorDiv("Failed to load LDAP test component: " + e.getMessage());
        }
    }
}
