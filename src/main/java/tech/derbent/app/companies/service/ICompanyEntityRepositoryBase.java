package tech.derbent.app.companies.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface ICompanyEntityRepositoryBase<EntityClass> {

	long countByCompany_Id(@Param ("company_id") Long company_id);
	List<EntityClass> findByCompany_Id(@Param ("company_id") Long company_id);
	Page<EntityClass> findByCompany_Id(@Param ("company_id") Long company_id, Pageable pageable);
}
