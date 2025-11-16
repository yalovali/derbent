package tech.derbent.api.entityOfCompany.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.app.companies.domain.CCompany;

public interface IEntityOfCompanyRepository<EntityClass extends CEntityOfCompany<EntityClass>> extends IAbstractNamedRepository<EntityClass> {

	@Query ("SELECT COUNT(s) FROM #{#entityName} s WHERE s.company = :company")
	long countByCompany(@Param ("company") CCompany company);
	@Query ("SELECT COUNT(s) > 0 FROM #{#entityName} s WHERE LOWER(s.name) = LOWER(:name) AND s.company = :company")
	boolean existsByNameProject(@Param ("name") String name, @Param ("company") CCompany company);
	@Query ("SELECT s FROM #{#entityName} s WHERE LOWER(s.name) = LOWER(:name) AND s.company = :company")
	Optional<EntityClass> findByNameAndCompany(@Param ("name") String name, @Param ("company") CCompany company);
	@Query ("SELECT e FROM #{#entityName} e WHERE e.company = :company")
	List<EntityClass> listByCompany(@Param ("company") CCompany company);
	@Query ("SELECT e FROM #{#entityName} e WHERE e.company.id = :cid")
	List<EntityClass> listByCompanyId(@Param ("cid") Long cid);
	@Query ("SELECT e FROM #{#entityName} e WHERE e.company = :company")
	Page<EntityClass> listByProject(@Param ("company") CCompany company, Pageable pageable);
}
