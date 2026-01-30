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
    @Query(value = """
        SELECT s.* FROM csystem_settings_bab s
        WHERE s.application_name = :applicationName
        ORDER BY s.id ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<CSystemSettings_Bab> findByApplicationName(@Param("applicationName") String applicationName);

    @Override
    @Query(value = """
        SELECT s.* FROM csystem_settings_bab s
        ORDER BY s.id ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<CSystemSettings_Bab> findFirst();

    @Override
    @Query("""
        SELECT s FROM CSystemSettings_Bab s
        WHERE s.id = :id
        """)
    Optional<CSystemSettings_Bab> findByIdForPageView(@Param("id") Long id);

    // BAB-specific queries for gateway settings
    @Query(value = """
        SELECT s.gateway_ip_address FROM csystem_settings_bab s
        ORDER BY s.id ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<String> getGatewayIpAddress();

    @Query(value = """
        SELECT s.gateway_port FROM csystem_settings_bab s
        ORDER BY s.id ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<Integer> getGatewayPort();

    @Query(value = """
        SELECT s.device_scan_interval_seconds FROM csystem_settings_bab s
        ORDER BY s.id ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<Integer> getDeviceScanIntervalSeconds();

    @Query(value = """
        SELECT s.max_concurrent_connections FROM csystem_settings_bab s
        ORDER BY s.id ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<Integer> getMaxConcurrentConnections();

    @Query(value = """
        SELECT s.enable_device_auto_discovery FROM csystem_settings_bab s
        ORDER BY s.id ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<Boolean> isDeviceAutoDiscoveryEnabled();
}