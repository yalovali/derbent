package tech.derbent.plm.setup.service;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.base.setup.service.ISystemSettingsRepository;
import tech.derbent.plm.setup.domain.CSystemSettings_Derbent;

/**
 * ISystemSettings_DerbentRepository - Concrete repository for CSystemSettings_Derbent entities.
 * Layer: Service (MVC)
 * Active when: default profile or 'derbent' profile (NOT 'bab' profile)
 * 
 * Provides Derbent-specific database queries for comprehensive PLM system settings.
 * Follows the same pattern as IProject_DerbentRepository.
 */
@Profile({"derbent", "default"})
public interface ISystemSettings_DerbentRepository extends ISystemSettingsRepository<CSystemSettings_Derbent> {

    @Override
    @Query("""
        SELECT s FROM CSystemSettings_Derbent s
        LEFT JOIN FETCH s.attachments
        LEFT JOIN FETCH s.comments
        WHERE s.applicationName = :applicationName
        ORDER BY s.id ASC
        """)
    Optional<CSystemSettings_Derbent> findByApplicationName(@Param("applicationName") String applicationName);

    @Override
    @Query("""
        SELECT s FROM CSystemSettings_Derbent s
        LEFT JOIN FETCH s.attachments
        LEFT JOIN FETCH s.comments
        ORDER BY s.id ASC
        """)
    Optional<CSystemSettings_Derbent> findFirst();

    @Override
    @Query("""
        SELECT s FROM CSystemSettings_Derbent s
        LEFT JOIN FETCH s.attachments
        LEFT JOIN FETCH s.comments
        WHERE s.id = :id
        """)
    Optional<CSystemSettings_Derbent> findByIdForPageView(@Param("id") Long id);

    // Derbent-specific queries for PLM features
    @Query("""
        SELECT s.enableProjectTemplates FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isProjectTemplatesEnabled();

    @Query("""
        SELECT s.enableKanbanBoards FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isKanbanBoardsEnabled();

    @Query("""
        SELECT s.enableTimeTracking FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isTimeTrackingEnabled();

    @Query("""
        SELECT s.enableGanttCharts FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isGanttChartsEnabled();

    @Query("""
        SELECT s.enableResourcePlanning FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isResourcePlanningEnabled();

    @Query("""
        SELECT s.enableAdvancedReporting FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isAdvancedReportingEnabled();

    @Query("""
        SELECT s.reportGenerationTimeoutMinutes FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Integer> getReportGenerationTimeoutMinutes();

    @Query("""
        SELECT s.enableAuditLogging FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isAuditLoggingEnabled();

    @Query("""
        SELECT s.auditLogRetentionDays FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Integer> getAuditLogRetentionDays();

    @Query("""
        SELECT s.enableTwoFactorAuth FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isTwoFactorAuthEnabled();

    @Query("""
        SELECT s.enableRestApi FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isRestApiEnabled();

    @Query("""
        SELECT s.apiRateLimitPerMinute FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Integer> getApiRateLimitPerMinute();

    @Query("""
        SELECT s.maxTotalStorageGb FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<java.math.BigDecimal> getMaxTotalStorageGb();

    @Query("""
        SELECT s.enableFileCompression FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isFileCompressionEnabled();

    @Query("""
        SELECT s.enableFileVirusScanning FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isFileVirusScanningEnabled();

    @Query("""
        SELECT s.enableEmailNotifications FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isEmailNotificationsEnabled();

    @Query("""
        SELECT s.enablePushNotifications FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Boolean> isPushNotificationsEnabled();

    @Query("""
        SELECT s.notificationBatchSize FROM CSystemSettings_Derbent s
        ORDER BY s.id ASC
        """)
    Optional<Integer> getNotificationBatchSize();
}