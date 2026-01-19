package tech.derbent.plm.validation.validationcasetype.service;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.validation.validationcasetype.domain.CValidationCaseType;

public interface IValidationCaseTypeRepository extends IEntityOfCompanyRepository<CValidationCaseType> {

	/** Find validation case type by name and company ID (for uniqueness check).
	 * @param name      the validation case type name
	 * @param companyId the company ID
	 * @return the matching validation case type, or null if not found */
	@Query("SELECT e FROM #{#entityName} e WHERE e.name = :name AND e.company.id = :companyId")
	CValidationCaseType findByNameAndCompanyId(@Param("name") String name, @Param("companyId") Long companyId);
}
