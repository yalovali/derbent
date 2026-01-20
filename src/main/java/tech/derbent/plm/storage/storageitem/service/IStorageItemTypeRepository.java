package tech.derbent.plm.storage.storageitem.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.storage.storageitem.domain.CStorageItemType;

public interface IStorageItemTypeRepository extends IEntityOfCompanyRepository<CStorageItemType> {

    @Override
    @Query("""
            SELECT t FROM #{#entityName} t
            LEFT JOIN FETCH t.company
            LEFT JOIN FETCH t.workflow
            WHERE t.company = :company
            ORDER BY t.name ASC
            """)
    List<CStorageItemType> listByCompanyForPageView(@Param("company") CCompany company);
}
