package tech.derbent.app.assets.assettype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.assets.assettype.domain.CAssetType;
import tech.derbent.api.companies.domain.CCompany;

public interface IAssetTypeRepository extends IEntityOfCompanyRepository<CAssetType> {

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<CAssetType> listByCompanyForPageView(@Param ("company") CCompany company);
}
