package tech.derbent.api.email.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.email.domain.CEmailQueued;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;

/**
 * IEmailQueuedRepository - Repository for queued email entities.
 * 
 * Service Layer (MVC Pattern)
 * 
 * Provides data access operations for emails waiting to be sent.
 * 
 * @author Derbent Team
 * @since 2026-02-11
 */
public interface IEmailQueuedRepository extends IEntityOfCompanyRepository<CEmailQueued> {

	/**
	 * Find all queued emails by company ordered by priority and queued time.
	 * Priority order: HIGH → NORMAL → LOW
	 * Then by queued time (oldest first)
	 * 
	 * @param company the company
	 * @return list of queued emails
	 */
	@Query("""
		SELECT e FROM #{#entityName} e
		LEFT JOIN FETCH e.company co
		WHERE e.company = :company
		ORDER BY 
			CASE e.priority
				WHEN 'HIGH' THEN 1
				WHEN 'NORMAL' THEN 2
				WHEN 'LOW' THEN 3
			END,
			e.queuedAt ASC
		""")
	@Override
	List<CEmailQueued> listByCompanyForPageView(@Param("company") CCompany company);

	/**
	 * Find pending emails ready to be sent.
	 * Excludes emails that have reached max retries.
	 * 
	 * @param company the company
	 * @return list of sendable emails
	 */
	@Query("""
		SELECT e FROM #{#entityName} e
		LEFT JOIN FETCH e.company co
		WHERE e.company = :company
		AND (e.retryCount < e.maxRetries OR e.retryCount IS NULL)
		ORDER BY 
			CASE e.priority
				WHEN 'HIGH' THEN 1
				WHEN 'NORMAL' THEN 2
				WHEN 'LOW' THEN 3
			END,
			e.queuedAt ASC
		""")
	List<CEmailQueued> findPendingEmails(@Param("company") CCompany company);
	
	/**
	 * Find all pending emails across all companies.
	 * Used by scheduler.
	 * 
	 * @return list of sendable emails
	 */
	@Query("""
		SELECT e FROM #{#entityName} e
		LEFT JOIN FETCH e.company co
		WHERE (e.retryCount < e.maxRetries OR e.retryCount IS NULL)
		ORDER BY 
			CASE e.priority
				WHEN 'HIGH' THEN 1
				WHEN 'NORMAL' THEN 2
				WHEN 'LOW' THEN 3
			END,
			e.queuedAt ASC
		""")
	List<CEmailQueued> findPendingEmails();

	/**
	 * Find failed emails that have reached max retries.
	 * 
	 * @param company the company
	 * @return list of failed emails
	 */
	@Query("""
		SELECT e FROM #{#entityName} e
		LEFT JOIN FETCH e.company co
		WHERE e.company = :company
		AND e.retryCount >= e.maxRetries
		ORDER BY e.queuedAt DESC
		""")
	List<CEmailQueued> findFailedEmails(@Param("company") CCompany company);

	/**
	 * Find emails by type.
	 * 
	 * @param company the company
	 * @param emailType the email type
	 * @return list of emails of specified type
	 */
	@Query("""
		SELECT e FROM #{#entityName} e
		LEFT JOIN FETCH e.company co
		WHERE e.company = :company
		AND e.emailType = :emailType
		ORDER BY e.queuedAt DESC
		""")
	List<CEmailQueued> findByEmailType(@Param("company") CCompany company, 
		@Param("emailType") String emailType);

	/**
	 * Find emails queued before a specific date.
	 * Used for cleanup/purging operations.
	 * 
	 * @param company the company
	 * @param beforeDate the cutoff date
	 * @return list of old emails
	 */
	@Query("""
		SELECT e FROM #{#entityName} e
		WHERE e.company = :company
		AND e.queuedAt < :beforeDate
		""")
	List<CEmailQueued> findQueuedBefore(@Param("company") CCompany company, 
		@Param("beforeDate") LocalDateTime beforeDate);

	/**
	 * Count pending emails for a company.
	 * 
	 * @param company the company
	 * @return count of pending emails
	 */
	@Query("""
		SELECT COUNT(e) FROM #{#entityName} e
		WHERE e.company = :company
		AND (e.retryCount < e.maxRetries OR e.retryCount IS NULL)
		""")
	long countPendingEmails(@Param("company") CCompany company);

	/**
	 * Count failed emails for a company.
	 * 
	 * @param company the company
	 * @return count of failed emails
	 */
	@Query("""
		SELECT COUNT(e) FROM #{#entityName} e
		WHERE e.company = :company
		AND e.retryCount >= e.maxRetries
		""")
	long countFailedEmails(@Param("company") CCompany company);
}
