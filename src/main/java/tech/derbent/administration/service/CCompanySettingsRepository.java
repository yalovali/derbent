package tech.derbent.administration.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.administration.domain.CCompanySettings;
import tech.derbent.companies.domain.CCompany;

/** CCompanySettingsRepository - Data access layer for CCompanySettings entities. Layer: Service (MVC) - Repository interface Provides database access
 * methods for company-wide administration settings, including CRUD operations and specialized queries for settings management. */
public interface CCompanySettingsRepository extends CAbstractRepository<CCompanySettings> {

	/** Finds company settings by the associated company. Each company should have only one settings record.
	 * @param company the company to find settings for
	 * @return Optional containing the CCompanySettings if found, empty otherwise */
	@Query ("SELECT cs FROM CCompanySettings cs WHERE cs.company = :company")
	Optional<CCompanySettings> findByCompany(@Param ("company") CCompany company);
	/** Finds company settings by company ID.
	 * @param companyId the ID of the company
	 * @return Optional containing the CCompanySettings if found, empty otherwise */
	@Query ("SELECT cs FROM CCompanySettings cs WHERE cs.company.id = :companyId")
	Optional<CCompanySettings> findByCompanyId(@Param ("companyId") Long companyId);
	/** Finds all companies that have email notifications enabled. Useful for system-wide email processing.
	 * @return List of CCompanySettings with email notifications enabled */
	@Query ("SELECT cs FROM CCompanySettings cs WHERE cs.emailNotificationsEnabled = true ORDER BY cs.company.name")
	List<CCompanySettings> findByEmailNotificationsEnabled();
	/** Finds companies with specific default project status.
	 * @param defaultProjectStatus the project status to search for
	 * @return List of CCompanySettings with the specified default project status */
	@Query ("SELECT cs FROM CCompanySettings cs WHERE cs.defaultProjectStatus = :defaultProjectStatus ORDER BY cs.company.name")
	List<CCompanySettings> findByDefaultProjectStatus(@Param ("defaultProjectStatus") String defaultProjectStatus);
	/** Finds companies in a specific timezone. Useful for time-based operations and scheduling.
	 * @param timezone the timezone to search for
	 * @return List of CCompanySettings in the specified timezone */
	@Query ("SELECT cs FROM CCompanySettings cs WHERE cs.companyTimezone = :timezone ORDER BY cs.company.name")
	List<CCompanySettings> findByCompanyTimezone(@Param ("timezone") String timezone);
	/** Finds companies with time tracking requirements.
	 * @param requireTimeTracking true to find companies requiring time tracking
	 * @return List of CCompanySettings with the specified time tracking requirement */
	@Query ("SELECT cs FROM CCompanySettings cs WHERE cs.requireTimeTracking = :requireTimeTracking ORDER BY cs.company.name")
	List<CCompanySettings> findByRequireTimeTracking(@Param ("requireTimeTracking") boolean requireTimeTracking);
	/** Finds companies with Gantt chart functionality enabled.
	 * @return List of CCompanySettings with Gantt charts enabled */
	@Query ("SELECT cs FROM CCompanySettings cs WHERE cs.enableGanttCharts = true ORDER BY cs.company.name")
	List<CCompanySettings> findByEnableGanttCharts();
	/** Finds companies with overdue notifications enabled. Useful for automated notification processing.
	 * @return List of CCompanySettings with overdue notifications enabled */
	@Query ("SELECT cs FROM CCompanySettings cs WHERE cs.overdueNotificationEnabled = true ORDER BY cs.company.name")
	List<CCompanySettings> findByOverdueNotificationEnabled();
	/** Checks if company settings exist for a specific company.
	 * @param companyId the ID of the company
	 * @return true if settings exist, false otherwise */
	@Query ("SELECT COUNT(cs) > 0 FROM CCompanySettings cs WHERE cs.company.id = :companyId")
	boolean existsByCompanyId(@Param ("companyId") Long companyId);
	/** Finds all company settings ordered by company name for administration views.
	 * @return List of all CCompanySettings ordered by company name */
	@Query ("SELECT cs FROM CCompanySettings cs ORDER BY cs.company.name")
	List<CCompanySettings> findAllOrderByCompanyName();
}
