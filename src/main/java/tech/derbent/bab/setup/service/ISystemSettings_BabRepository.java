package tech.derbent.bab.setup.service;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;
import tech.derbent.base.setup.service.ISystemSettingsRepository;

/**
 * ISystemSettings_BabRepository - Concrete repository for CSystemSettings_Bab entities.
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * 
 * Provides BAB-specific database queries for IoT gateway system settings.
 * Follows the same pattern as IProject_BabRepository.
 */
@Profile("bab")
public interface ISystemSettings_BabRepository extends ISystemSettingsRepository<CSystemSettings_Bab> {

    @Override
    @Query("""
        SELECT s FROM CSystemSettings_Bab s
        WHERE s.applicationName = :applicationName
        ORDER BY s.id ASC
        """)
    Optional<CSystemSettings_Bab> findByApplicationName(@Param("applicationName") String applicationName);

    @Override
    @Query("""
        SELECT s FROM CSystemSettings_Bab s
        ORDER BY s.id ASC
        """)
    Optional<CSystemSettings_Bab> findFirst();

    @Override
    @Query("""
        SELECT s FROM CSystemSettings_Bab s
        WHERE s.id = :id
        """)
    Optional<CSystemSettings_Bab> findByIdForPageView(@Param("id") Long id);

    // BAB-specific queries for gateway settings
    @Query("""
        SELECT s.gatewayIpAddress FROM CSystemSettings_Bab s
        ORDER BY s.id ASC
        """)
    Optional<String> getGatewayIpAddress();

    @Query("""
        SELECT s.gatewayPort FROM CSystemSettings_Bab s
        ORDER BY s.id ASC
        """)
    Optional<Integer> getGatewayPort();

    @Query("""
        SELECT s.deviceScanIntervalSeconds FROM CSystemSettings_Bab s
        ORDER BY s.id ASC
        """)
    Optional<Integer> getDeviceScanIntervalSeconds();

    @Query("""
        SELECT s.maxConcurrentConnections FROM CSystemSettings_Bab s
        ORDER BY s.id ASC
        """)
    Optional<Integer> getMaxConcurrentConnections();

    @Query("""
        SELECT s.enableDeviceAutoDiscovery FROM CSystemSettings_Bab s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isDeviceAutoDiscoveryEnabled();
}