package tech.derbent.api.scheduler.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.scheduler.domain.CScheduleTask;

public interface IScheduleTaskRepository extends IEntityOfCompanyRepository<CScheduleTask> {

	@Query ("SELECT COUNT(t) FROM CScheduleTask t WHERE t.enabled = true AND t.company = :company")
	long countEnabledByCompany(@Param ("company") CCompany company);
	@Query ("SELECT t FROM CScheduleTask t WHERE t.action = :action AND t.enabled = true AND t.company = :company")
	List<CScheduleTask> findByActionAndCompany(@Param ("action") String action, @Param ("company") CCompany company);
	@Query ("SELECT t FROM CScheduleTask t WHERE t.enabled = true AND t.company = :company ORDER BY t.nextRun ASC")
	List<CScheduleTask> findEnabledByCompany(@Param ("company") CCompany company);
	@Query ("SELECT t FROM CScheduleTask t WHERE t.lastError IS NOT NULL ORDER BY t.lastRun DESC")
	List<CScheduleTask> findFailedTasks();
	@Query ("SELECT t FROM CScheduleTask t WHERE t.enabled = true AND (t.nextRun IS NULL OR t.nextRun <= :now) ORDER BY t.nextRun ASC")
	List<CScheduleTask> findTasksDueForExecution(@Param ("now") LocalDateTime now);
}
