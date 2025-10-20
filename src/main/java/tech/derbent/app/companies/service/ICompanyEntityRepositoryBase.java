package tech.derbent.app.companies.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface ICompanyEntityRepositoryBase<EntityClass> {

	List<EntityClass> findByCompanyId(@Param ("companyId") Long companyId);
	Page<EntityClass> findByCompanyId(@Param ("companyId") Long companyId, Pageable pageable);
}
