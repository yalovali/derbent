package tech.derbent.app.testcases.testcasetype.service;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.testcases.testcasetype.domain.CTestCaseType;

public interface ITestCaseTypeRepository extends IEntityOfCompanyRepository<CTestCaseType> {

	/** Find test case type by name and company ID (for uniqueness check).
	 * @param name      the test case type name
	 * @param companyId the company ID
	 * @return the matching test case type, or null if not found */
	@Query("SELECT e FROM #{#entityName} e WHERE e.name = :name AND e.company.id = :companyId")
	CTestCaseType findByNameAndCompanyId(@Param("name") String name, @Param("companyId") Long companyId);
}
