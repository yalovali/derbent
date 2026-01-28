package tech.derbent.plm.setup.service;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.base.setup.service.ISystemSettingsRepository;
import tech.derbent.plm.setup.domain.CSystemSettings_Derbent;

/**
 * ISystemSettings_DerbentRepository - Derbent PLM-specific system settings repository.
 * Layer: Service (MVC) - Repository interface
 * Active when: default profile or 'derbent' profile (NOT 'bab' profile)
 * 
 * Provides database access methods for comprehensive PLM configuration settings.
 */
@Profile({"derbent", "default"})
public interface ISystemSettings_DerbentRepository extends ISystemSettingsRepository<CSystemSettings_Derbent> {

    @Override
    @Query("SELECT ss FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<CSystemSettings_Derbent> findSystemSettings();

    @Override
    @Query("SELECT ss FROM CSystemSettings_Derbent ss WHERE ss.applicationName = :applicationName")
    Optional<CSystemSettings_Derbent> findByApplicationName(@Param("applicationName") String applicationName);

    @Override
    @Query("SELECT ss FROM CSystemSettings_Derbent ss WHERE ss.applicationVersion = :applicationVersion")
    Optional<CSystemSettings_Derbent> findByApplicationVersion(@Param("applicationVersion") String applicationVersion);

    @Override
    @Query("SELECT ss.maintenanceModeEnabled FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isMaintenanceModeEnabled();

    @Override
    @Query("SELECT ss.maintenanceMessage FROM CSystemSettings_Derbent ss WHERE ss.maintenanceModeEnabled = true ORDER BY ss.id LIMIT 1")
    Optional<String> getMaintenanceMessage();

    @Override
    @Query("SELECT ss.enableAutomaticBackups FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isAutomaticBackupsEnabled();

    @Override
    @Query("SELECT ss.backupScheduleCron FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<String> getBackupScheduleCron();

    @Override
    @Query("SELECT ss.sessionTimeoutMinutes FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Integer> getSessionTimeoutMinutes();

    @Override
    @Query("SELECT ss.maxFileUploadSizeMb FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<java.math.BigDecimal> getMaxFileUploadSizeMb();

    @Override
    @Query("SELECT ss.allowedFileExtensions FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<String> getAllowedFileExtensions();

    @Override
    @Query("SELECT ss.requireStrongPasswords FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isStrongPasswordsRequired();

    @Override
    @Query("SELECT ss.maxLoginAttempts FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Integer> getMaxLoginAttempts();

    @Override
    @Query("SELECT COUNT(ss) > 0 FROM CSystemSettings_Derbent ss")
    boolean existsSystemSettings();

    @Override
    @Query("SELECT ss.systemEmailFrom FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<String> getSystemEmailFrom();

    @Override
    @Query("SELECT ss.enableDatabaseLogging FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isDatabaseLoggingEnabled();

    // Derbent-specific queries

    /** Checks if project templates are enabled.
     * @return true if project templates are enabled, false otherwise */
    @Query("SELECT ss.enableProjectTemplates FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isProjectTemplatesEnabled();

    /** Checks if Kanban boards are enabled.
     * @return true if Kanban boards are enabled, false otherwise */
    @Query("SELECT ss.enableKanbanBoards FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isKanbanBoardsEnabled();

    /** Checks if time tracking is enabled.
     * @return true if time tracking is enabled, false otherwise */
    @Query("SELECT ss.enableTimeTracking FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isTimeTrackingEnabled();

    /** Checks if Gantt charts are enabled.
     * @return true if Gantt charts are enabled, false otherwise */
    @Query("SELECT ss.enableGanttCharts FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isGanttChartsEnabled();

    /** Checks if resource planning is enabled.
     * @return true if resource planning is enabled, false otherwise */
    @Query("SELECT ss.enableResourcePlanning FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isResourcePlanningEnabled();

    /** Checks if advanced reporting is enabled.
     * @return true if advanced reporting is enabled, false otherwise */
    @Query("SELECT ss.enableAdvancedReporting FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isAdvancedReportingEnabled();

    /** Gets the report generation timeout in minutes.
     * @return Optional containing the report generation timeout if available */
    @Query("SELECT ss.reportGenerationTimeoutMinutes FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Integer> getReportGenerationTimeoutMinutes();

    /** Checks if audit logging is enabled.
     * @return true if audit logging is enabled, false otherwise */
    @Query("SELECT ss.enableAuditLogging FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isAuditLoggingEnabled();

    /** Gets the audit log retention period in days.
     * @return Optional containing the audit log retention days if available */
    @Query("SELECT ss.auditLogRetentionDays FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Integer> getAuditLogRetentionDays();

    /** Checks if two-factor authentication is enabled.
     * @return true if two-factor authentication is enabled, false otherwise */
    @Query("SELECT ss.enableTwoFactorAuth FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isTwoFactorAuthEnabled();

    /** Checks if REST API is enabled.
     * @return true if REST API is enabled, false otherwise */
    @Query("SELECT ss.enableRestApi FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isRestApiEnabled();

    /** Gets the API rate limit per minute.
     * @return Optional containing the API rate limit if available */
    @Query("SELECT ss.apiRateLimitPerMinute FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Integer> getApiRateLimitPerMinute();

    /** Gets the maximum total storage in GB.
     * @return Optional containing the max total storage if available */
    @Query("SELECT ss.maxTotalStorageGb FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<java.math.BigDecimal> getMaxTotalStorageGb();

    /** Checks if file compression is enabled.
     * @return true if file compression is enabled, false otherwise */
    @Query("SELECT ss.enableFileCompression FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isFileCompressionEnabled();

    /** Checks if file virus scanning is enabled.
     * @return true if file virus scanning is enabled, false otherwise */
    @Query("SELECT ss.enableFileVirusScanning FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isFileVirusScanningEnabled();

    /** Checks if email notifications are enabled.
     * @return true if email notifications are enabled, false otherwise */
    @Query("SELECT ss.enableEmailNotifications FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isEmailNotificationsEnabled();

    /** Checks if push notifications are enabled.
     * @return true if push notifications are enabled, false otherwise */
    @Query("SELECT ss.enablePushNotifications FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Boolean> isPushNotificationsEnabled();

    /** Gets the notification batch size.
     * @return Optional containing the notification batch size if available */
    @Query("SELECT ss.notificationBatchSize FROM CSystemSettings_Derbent ss ORDER BY ss.id LIMIT 1")
    Optional<Integer> getNotificationBatchSize();
}