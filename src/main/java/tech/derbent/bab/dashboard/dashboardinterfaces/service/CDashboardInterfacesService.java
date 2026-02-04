package tech.derbent.bab.dashboard.dashboardinterfaces.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.dashboard.dashboardinterfaces.domain.CDashboardInterfaces;
import tech.derbent.base.session.service.ISessionService;

/**
 * CDashboardInterfacesService - Service for BAB interface configuration dashboard.
 * <p>
 * Layer: Service (MVC)
 * Profile: bab
 * <p>
 * Following Derbent pattern: Concrete service with @Service annotation and interfaces.
 * Manages CDashboardInterfaces entities with BAB-specific interface configuration logic.
 * <p>
 * Provides specialized dashboard functionality for BAB interface management:
 * <ul>
 * <li>Interface configuration validation</li>
 * <li>Interface dashboard initialization</li>
 * <li>Interface status monitoring</li>
 * <li>Configuration consistency checks</li>
 * </ul>
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CDashboardInterfacesService extends CProjectItemService<CDashboardInterfaces>
        implements IEntityRegistrable, IEntityWithView {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardInterfacesService.class);

    public CDashboardInterfacesService(final IDashboardInterfacesRepository repository,
                                     final Clock clock,
                                     final ISessionService sessionService,
                                     final tech.derbent.api.entityOfCompany.service.CProjectItemStatusService statusService) {
        super(repository, clock, sessionService, statusService);
    }

    @Override
    public Class<CDashboardInterfaces> getEntityClass() {
        return CDashboardInterfaces.class;
    }

    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceDashboardInterfaces.class;
    }

    @Override
    public Class<?> getInitializerServiceClass() {
        return null; // No initializer for now - manual registration
    }

    @Override
    public Class<?> getServiceClass() {
        return this.getClass();
    }

    public void initializeNewEntity(final CDashboardInterfaces entity) {
        super.initializeNewEntity(entity);
        LOGGER.debug("Initializing new interface dashboard entity");

        // Set BAB-specific defaults
        entity.setIsActive(true);
        if (entity.getConfigurationMode() == null) {
            entity.setConfigurationMode("automatic");
        }

        LOGGER.debug("Interface dashboard initialization complete");
    }

    @Override
    protected void validateEntity(final CDashboardInterfaces entity) {
        super.validateEntity(entity);

        // Required fields validation
        Check.notNull(entity.getIsActive(), "Active status is required");

        // Configuration mode validation
        if (entity.getConfigurationMode() != null) {
            final List<String> validModes = List.of("automatic", "manual", "hybrid");
            if (!validModes.contains(entity.getConfigurationMode())) {
                throw new IllegalArgumentException(
                    "Configuration mode must be one of: " + String.join(", ", validModes));
            }
        }

        LOGGER.debug("Interface dashboard validation complete");
    }

    @Override
    public String checkDeleteAllowed(final CDashboardInterfaces entity) {
        final String superCheck = super.checkDeleteAllowed(entity);
        if (superCheck != null) {
            return superCheck;
        }

        // Interface-specific deletion checks
        if (Boolean.TRUE.equals(entity.getIsActive())) {
            return "Cannot delete active interface dashboard. Deactivate first.";
        }

        return null; // Delete allowed
    }

    /**
     * Get all active interface dashboards for the given project.
     * 
     * @param projectId project identifier
     * @return list of active interface dashboards
     */
    public List<CDashboardInterfaces> findActiveByProject(final Long projectId) {
        LOGGER.debug("Finding active interface dashboards for project: {}", projectId);
        return ((IDashboardInterfacesRepository) repository).findByProjectIdAndIsActive(projectId, true);
    }

    /**
     * Get interface dashboards by configuration mode.
     * 
     * @param configurationMode the configuration mode to filter by
     * @return list of matching dashboards
     */
    public List<CDashboardInterfaces> findByConfigurationMode(final String configurationMode) {
        LOGGER.debug("Finding interface dashboards by configuration mode: {}", configurationMode);
        return ((IDashboardInterfacesRepository) repository).findByConfigurationMode(configurationMode);
    }

    /**
     * Activate or deactivate an interface dashboard.
     * 
     * @param entity the dashboard entity
     * @param active true to activate, false to deactivate
     */
    public void setActiveStatus(final CDashboardInterfaces entity, final boolean active) {
        LOGGER.debug("Setting interface dashboard active status to: {}", active);
        entity.setIsActive(active);
        save(entity);
    }
}