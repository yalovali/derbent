package tech.derbent.api.entityOfCompany.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;

@NoRepositoryBean
public interface IEntityOfCompanyRepository<EntityClass extends CEntityOfCompany<EntityClass>> extends IAbstractNamedRepository<EntityClass> {

	long countByCompany(CCompany company);
	boolean existsByNameIgnoreCaseAndCompany(String name, CCompany company);
	@Query ("SELECT e FROM #{#entityName} e LEFT JOIN FETCH e.company co WHERE e.company = :company ORDER BY e.name ASC")
	List<EntityClass> findByCompany(@Param ("company") CCompany company);
	@Query ("SELECT e FROM #{#entityName} e LEFT JOIN FETCH e.company co WHERE e.company = :company ORDER BY e.name ASC")
	Page<EntityClass> findByCompany(@Param ("company") CCompany company, Pageable pageable);
	@Query ("SELECT e FROM #{#entityName} e WHERE e.name = :name AND e.company = :company")
	Optional<EntityClass> findByNameAndCompany(@Param ("name") String name, @Param ("company") CCompany company);
	Optional<EntityClass> findByNameIgnoreCaseAndCompany(String name, CCompany company);
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.company co
			WHERE e.company = :company
			ORDER BY e.name ASC
			""")
	List<EntityClass> listByCompanyForPageView(@Param ("company") CCompany company);
	@Query ("SELECT e FROM #{#entityName} e WHERE e.company.id = :cid ORDER BY e.name ASC")
	List<EntityClass> listByCompanyId(@Param ("cid") Long cid);
}
