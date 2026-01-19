package tech.derbent.plm.documenttypes.service;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.documenttypes.domain.CDocumentType;

/**
 * Repository interface for CDocumentType entities.
 * Provides data access methods for document type management.
 */
public interface IDocumentTypeRepository extends IEntityOfCompanyRepository<CDocumentType> {

	/** Find document type by name and company.
	 * @param name the document type name
	 * @param company the company
	 * @return the document type or null */
	@Query("SELECT e FROM #{#entityName} e WHERE e.name = :name AND e.company = :company")
	CDocumentType findByNameAndCompany(@Param("name") String name, @Param("company") CCompany company);
}
