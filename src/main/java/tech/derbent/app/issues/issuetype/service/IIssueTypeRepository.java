package tech.derbent.app.issues.issuetype.service;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.issues.issuetype.domain.CIssueType;

public interface IIssueTypeRepository extends IEntityOfCompanyRepository<CIssueType> {

	/** Find issue type by name and company ID (for uniqueness check).
	 * @param name      the issue type name
	 * @param companyId the company ID
	 * @return the matching issue type, or null if not found */
	@Query("SELECT e FROM #{#entityName} e WHERE e.name = :name AND e.company.id = :companyId")
	CIssueType findByNameAndCompanyId(@Param("name") String name, @Param("companyId") Long companyId);
}
