package tech.derbent.bab.setup.service;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;
import tech.derbent.base.setup.service.ISystemSettingsRepository;

/**
 * ISystemSettings_BabRepository - BAB IoT Gateway-specific system settings repository.
 * Layer: Service (MVC) - Repository interface
 * Active when: 'bab' profile is active
 * 
 * Provides database access methods for BAB gateway configuration settings.
 */
@Profile("bab")
public interface ISystemSettings_BabRepository extends ISystemSettingsRepository<CSystemSettings_Bab> {

    @Override
    @Query("SELECT ss FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<CSystemSettings_Bab> findSystemSettings();

    @Override
    @Query("SELECT ss FROM CSystemSettings_Bab ss WHERE ss.applicationName = :applicationName")
    Optional<CSystemSettings_Bab> findByApplicationName(@Param("applicationName") String applicationName);

    @Override
    @Query("SELECT ss FROM CSystemSettings_Bab ss WHERE ss.applicationVersion = :applicationVersion")
    Optional<CSystemSettings_Bab> findByApplicationVersion(@Param("applicationVersion") String applicationVersion);

    @Override
    @Query("SELECT ss.maintenanceModeEnabled FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isMaintenanceModeEnabled();

    @Override
    @Query("SELECT ss.maintenanceMessage FROM CSystemSettings_Bab ss WHERE ss.maintenanceModeEnabled = true ORDER BY ss.id LIMIT 1")
    Optional<String> getMaintenanceMessage();

    @Override
    @Query("SELECT ss.enableAutomaticBackups FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isAutomaticBackupsEnabled();

    @Override
    @Query("SELECT ss.backupScheduleCron FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<String> getBackupScheduleCron();

    @Override
    @Query("SELECT ss.sessionTimeoutMinutes FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<Integer> getSessionTimeoutMinutes();

    @Override
    @Query("SELECT ss.maxFileUploadSizeMb FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<java.math.BigDecimal> getMaxFileUploadSizeMb();

    @Override
    @Query("SELECT ss.allowedFileExtensions FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<String> getAllowedFileExtensions();

    @Override
    @Query("SELECT ss.requireStrongPasswords FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isStrongPasswordsRequired();

    @Override
    @Query("SELECT ss.maxLoginAttempts FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<Integer> getMaxLoginAttempts();

    @Override
    @Query("SELECT COUNT(ss) > 0 FROM CSystemSettings_Bab ss")
    boolean existsSystemSettings();

    @Override
    @Query("SELECT ss.systemEmailFrom FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<String> getSystemEmailFrom();

    @Override
    @Query("SELECT ss.enableDatabaseLogging FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isDatabaseLoggingEnabled();

    // BAB-specific queries
    
    /** Gets the gateway IP address.
     * @return Optional containing the gateway IP address if available */
    @Query("SELECT ss.gatewayIpAddress FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<String> getGatewayIpAddress();

    /** Gets the gateway communication port.
     * @return Optional containing the gateway port if available */
    @Query("SELECT ss.gatewayPort FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<Integer> getGatewayPort();

    /** Gets the device scan interval in seconds.
     * @return Optional containing the device scan interval if available */
    @Query("SELECT ss.deviceScanIntervalSeconds FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<Integer> getDeviceScanIntervalSeconds();

    /** Gets the maximum number of concurrent device connections.
     * @return Optional containing the max concurrent connections if available */
    @Query("SELECT ss.maxConcurrentConnections FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<Integer> getMaxConcurrentConnections();

    /** Checks if device auto-discovery is enabled.
     * @return true if device auto-discovery is enabled, false otherwise */
    @Query("SELECT ss.enableDeviceAutoDiscovery FROM CSystemSettings_Bab ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isDeviceAutoDiscoveryEnabled();
}