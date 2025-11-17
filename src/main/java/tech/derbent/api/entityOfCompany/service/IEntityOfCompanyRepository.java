package tech.derbent.api.entityOfCompany.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.app.companies.domain.CCompany;

@NoRepositoryBean
public interface IEntityOfCompanyRepository<EntityClass extends CEntityOfCompany<EntityClass>> extends IAbstractNamedRepository<EntityClass> {

	long countByCompany(CCompany company);
	boolean existsByNameIgnoreCaseAndCompany(String name, CCompany company);
	List<EntityClass> findByCompany(CCompany company);
	Page<EntityClass> findByCompany(CCompany company, Pageable pageable);
	Optional<EntityClass> findByNameIgnoreCaseAndCompany(String name, CCompany company);
	@Query ("SELECT e FROM #{#entityName} e WHERE e.company.id = :cid")
	List<EntityClass> listByCompanyId(@Param ("cid") Long cid);
}
