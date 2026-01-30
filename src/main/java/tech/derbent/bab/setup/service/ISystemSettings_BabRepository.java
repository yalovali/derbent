package tech.derbent.bab.setup.service;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;
import tech.derbent.base.setup.service.ISystemSettingsRepository;

/** ISystemSettings_BabRepository - Concrete repository for CSystemSettings_Bab entities. Layer: Service (MVC) Active when: 'bab' profile is active
 * Provides BAB-specific database queries for IoT gateway system settings. Follows the same pattern as IProject_BabRepository. */
@Profile ("bab")
public interface ISystemSettings_BabRepository extends ISystemSettingsRepository<CSystemSettings_Bab> {

	@Override
	@Query ("""
			SELECT s FROM #{#entityName} s
			WHERE s.applicationName = :applicationName
			""")
	Optional<CSystemSettings_Bab> findByApplicationName(@Param ("applicationName") String applicationName);
	@Override
	@Query ("""
			SELECT s FROM #{#entityName} s
			WHERE s.id = :id
			""")
	Optional<CSystemSettings_Bab> findByIdForPageView(@Param ("id") Long id);
	@Override
	@Query ("SELECT s FROM #{#entityName} s WHERE s.id = (SELECT MIN(s2.id) FROM #{#entityName} s2)")
	Optional<CSystemSettings_Bab> findFirst();
	@Query ("SELECT s.deviceScanIntervalSeconds FROM #{#entityName} s WHERE s.id = (SELECT MIN(s2.id) FROM #{#entityName} s2)")
	Optional<Integer> getDeviceScanIntervalSeconds();
	// BAB-specific queries for gateway settings
	@Query ("SELECT s.gatewayIpAddress FROM #{#entityName} s WHERE s.id = (SELECT MIN(s2.id) FROM #{#entityName} s2)")
	Optional<String> getGatewayIpAddress();
	@Query ("SELECT s.gatewayPort FROM #{#entityName} s WHERE s.id = (SELECT MIN(s2.id) FROM #{#entityName} s2)")
	Optional<Integer> getGatewayPort();
	@Query ("SELECT s.maxConcurrentConnections FROM #{#entityName} s WHERE s.id = (SELECT MIN(s2.id) FROM #{#entityName} s2)")
	Optional<Integer> getMaxConcurrentConnections();
	@Query ("SELECT s.enableDeviceAutoDiscovery FROM #{#entityName} s WHERE s.id = (SELECT MIN(s2.id) FROM #{#entityName} s2)")
	Optional<Boolean> isDeviceAutoDiscoveryEnabled();
}
