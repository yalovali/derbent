package tech.derbent.api.email.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.email.domain.CEmailSent;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;

/** IEmailSentRepository - Repository for sent email entities (archive). Service Layer (MVC Pattern) Provides data access operations for successfully
 * sent emails. Used for audit trail, compliance, and historical tracking.
 * @author Derbent Team
 * @since 2026-02-11 */
public interface IEmailSentRepository extends IEntityOfCompanyRepository<CEmailSent> {

	/** Count sent emails in date range.
	 * @param company   the company
	 * @param startDate start of date range
	 * @param endDate   end of date range
	 * @return count of sent emails */
	@Query ("""
			SELECT COUNT(e) FROM #{#entityName} e
			WHERE e.company = :company
			AND e.sentAt BETWEEN :startDate AND :endDate
			""")
	long countByDateRange(@Param ("company") CCompany company, @Param ("startDate") LocalDateTime startDate,
			@Param ("endDate") LocalDateTime endDate);
	/** Count sent emails by type.
	 * @param company   the company
	 * @param emailType the email type
	 * @return count of sent emails of type */
	@Query ("""
			SELECT COUNT(e) FROM #{#entityName} e
			WHERE e.company = :company
			AND e.emailType = :emailType
			""")
	long countByEmailType(@Param ("company") CCompany company, @Param ("emailType") String emailType);
	/** Find sent emails by date range.
	 * @param company   the company
	 * @param startDate start of date range
	 * @param endDate   end of date range
	 * @return list of sent emails in range */
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.company co
			WHERE e.company = :company
			AND e.sentAt BETWEEN :startDate AND :endDate
			ORDER BY e.sentAt DESC
			""")
	List<CEmailSent> findByDateRange(@Param ("company") CCompany company, @Param ("startDate") LocalDateTime startDate,
			@Param ("endDate") LocalDateTime endDate);
	/** Find sent emails by type.
	 * @param company   the company
	 * @param emailType the email type
	 * @return list of sent emails of specified type */
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.company co
			WHERE e.company = :company
			AND e.emailType = :emailType
			ORDER BY e.sentAt DESC
			""")
	List<CEmailSent> findByEmailType(@Param ("company") CCompany company, @Param ("emailType") String emailType);
	/** Find sent emails by recipient.
	 * @param company the company
	 * @param toEmail recipient email address
	 * @return list of sent emails to recipient */
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.company co
			WHERE e.company = :company
			AND e.toEmail = :toEmail
			ORDER BY e.sentAt DESC
			""")
	List<CEmailSent> findByRecipient(@Param ("company") CCompany company, @Param ("toEmail") String toEmail);
	/** Find sent emails by reference entity.
	 * @param company    the company
	 * @param entityType reference entity type
	 * @param entityId   reference entity ID
	 * @return list of sent emails related to entity */
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.company co
			WHERE e.company = :company
			AND e.referenceEntityType = :entityType
			AND e.referenceEntityId = :entityId
			ORDER BY e.sentAt DESC
			""")
	List<CEmailSent> findByReferenceEntity(@Param ("company") CCompany company, @Param ("entityType") String entityType,
			@Param ("entityId") Long entityId);
	/** Find sent emails before a specific date. Used for cleanup/archiving operations.
	 * @param company    the company
	 * @param beforeDate the cutoff date
	 * @return list of old sent emails */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.company = :company
			AND e.sentAt < :beforeDate
			""")
	List<CEmailSent> findSentBefore(@Param ("company") CCompany company, @Param ("beforeDate") LocalDateTime beforeDate);
	/** Get email statistics by type for date range.
	 * @param company   the company
	 * @param startDate start of date range
	 * @param endDate   end of date range
	 * @return list of [emailType, count] pairs */
	@Query ("""
			SELECT e.emailType, COUNT(e)
			FROM #{#entityName} e
			WHERE e.company = :company
			AND e.sentAt BETWEEN :startDate AND :endDate
			GROUP BY e.emailType
			ORDER BY COUNT(e) DESC
			""")
	List<Object[]> getStatisticsByType(@Param ("company") CCompany company, @Param ("startDate") LocalDateTime startDate,
			@Param ("endDate") LocalDateTime endDate);
	/** Find all sent emails by company ordered by sent time (newest first).
	 * @param company the company
	 * @return list of sent emails */
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.company co
			WHERE e.company = :company
			ORDER BY e.sentAt DESC
			""")
	@Override
	List<CEmailSent> listByCompanyForPageView(@Param ("company") CCompany company);

	/** Find recent sent emails by company (limited).
	 * @param company the company
	 * @param limit   maximum number of results
	 * @return list of recent sent emails */
	@Query (value = """
			SELECT e FROM CEmailSent e
			LEFT JOIN FETCH e.company co
			WHERE e.company = :company
			ORDER BY e.sentAt DESC
			LIMIT :limit
			""", nativeQuery = false)
	List<CEmailSent> findRecentByCompany(@Param ("company") CCompany company, @Param ("limit") int limit);
}
