package tech.derbent.bab.device.service;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.api.companies.domain.CCompany;

/**
 * Repository interface for CBabDevice entities.
 * Following Derbent pattern: Repository interfaces in service package.
 */
@Profile("bab")
public interface IBabDeviceRepository extends IAbstractRepository<CBabDevice> {

	/**
	 * Find device by company (unique constraint ensures max 1 result).
	 * 
	 * @param company the company
	 * @return list of devices (max 1 due to unique constraint)
	 */
	@Query("SELECT e FROM #{#entityName} e WHERE e.company = :company ORDER BY e.id DESC")
	List<CBabDevice> findByCompany(@Param("company") CCompany company);

	/**
	 * Find device by company ID.
	 * 
	 * @param companyId the company ID
	 * @return optional device
	 */
	@Query("SELECT e FROM #{#entityName} e WHERE e.company.id = :companyId ORDER BY e.id DESC")
	Optional<CBabDevice> findByCompanyId(@Param("companyId") Long companyId);

	/**
	 * Find device by serial number.
	 * 
	 * @param serialNumber the serial number
	 * @return optional device
	 */
	@Query("SELECT e FROM #{#entityName} e WHERE e.serialNumber = :serialNumber")
	Optional<CBabDevice> findBySerialNumber(@Param("serialNumber") String serialNumber);

	/**
	 * Count devices by company (should be 0 or 1).
	 * 
	 * @param company the company
	 * @return device count
	 */
	@Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.company = :company")
	Long countByCompany(@Param("company") CCompany company);
}
