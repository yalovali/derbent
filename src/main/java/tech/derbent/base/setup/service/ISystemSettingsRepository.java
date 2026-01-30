package tech.derbent.base.setup.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.base.setup.domain.CSystemSettings;

/**
 * ISystemSettingsRepository - Abstract repository interface for system settings.
 * Layer: Service (MVC)
 * 
 * Base repository interface for all system settings variants.
 * Follows the same pattern as IProjectRepository.
 * Concrete implementations: ISystemSettings_BabRepository, ISystemSettings_DerbentRepository
 */
@NoRepositoryBean
public interface ISystemSettingsRepository<SettingsClass extends CSystemSettings<SettingsClass>> extends IAbstractRepository<SettingsClass> {

    /**
     * Find system settings by application name.
     * System settings should be unique by application name.
     */
    @Query("SELECT s FROM #{#entityName} s WHERE s.applicationName = :applicationName")
    Optional<SettingsClass> findByApplicationName(@Param("applicationName") String applicationName);

    /**
     * Find the first (and should be only) system settings record.
     * Used for singleton-like behavior.
     */
    @Query(value = "SELECT s.* FROM #{#entityName} s ORDER BY s.id ASC LIMIT 1", nativeQuery = true)
    Optional<SettingsClass> findFirst();

    /**
     * Check if any system settings exist.
     */
    @Query("SELECT COUNT(s) > 0 FROM #{#entityName} s")
    boolean existsAny();

    /**
     * Find system settings for page view with all necessary joins.
     */
    @Query("SELECT s FROM #{#entityName} s WHERE s.id = :id")
    Optional<SettingsClass> findByIdForPageView(@Param("id") Long id);
}
