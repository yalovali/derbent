package tech.derbent.base.setup.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.NoRepositoryBean;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.base.setup.domain.CSystemSettings;

/** 
 * ISystemSettingsRepository - Abstract repository for CSystemSettings entities. 
 * Layer: Service (MVC) - Repository interface 
 * 
 * Provides base database access methods for system-wide configuration settings. 
 * Since system settings are singleton by design, most operations focus on retrieving 
 * and updating the single system settings record.
 * 
 * Concrete implementations: ISystemSettings_DerbentRepository, ISystemSettings_BabRepository
 */
@NoRepositoryBean
public interface ISystemSettingsRepository<SettingsType extends CSystemSettings<SettingsType>> extends IAbstractRepository<SettingsType> {

	// Abstract method signatures - implemented by concrete repositories
	Optional<SettingsType> findSystemSettings();
	Optional<SettingsType> findByApplicationName(String applicationName);
	Optional<SettingsType> findByApplicationVersion(String applicationVersion);
	Optional<Boolean> isMaintenanceModeEnabled();
	Optional<String> getMaintenanceMessage();
	Optional<Boolean> isAutomaticBackupsEnabled();
	Optional<String> getBackupScheduleCron();
	Optional<Integer> getSessionTimeoutMinutes();
	Optional<java.math.BigDecimal> getMaxFileUploadSizeMb();
	Optional<String> getAllowedFileExtensions();
	Optional<Boolean> isStrongPasswordsRequired();
	Optional<Integer> getMaxLoginAttempts();
	boolean existsSystemSettings();
	Optional<String> getSystemEmailFrom();
	Optional<Boolean> isDatabaseLoggingEnabled();
}
