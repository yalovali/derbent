package tech.derbent.api.imports.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.imports.domain.CDataImport;

public interface IDataImportRepository extends IEntityOfCompanyRepository<CDataImport> {

    @Query ("""
            SELECT d FROM CDataImport d
            LEFT JOIN FETCH d.company
            WHERE d.company = :company
            ORDER BY d.importedAt DESC
            """)
    List<CDataImport> findByCompanyOrderByImportedAtDesc(@Param ("company") CCompany company);
}
