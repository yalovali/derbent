package tech.derbent.app.products.productversion.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.products.productversion.domain.CProductVersion;
import tech.derbent.app.products.productversiontype.domain.CProductVersionType;

public interface IProductVersionRepository extends IEntityOfProjectRepository<CProductVersion> {

@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
long countByType(@Param ("entityType") CProductVersionType type);

@Override
@Query ("SELECT r FROM CProductVersion r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy LEFT JOIN FETCH r.status LEFT JOIN FETCH r.entityType LEFT JOIN FETCH r.product WHERE r.id = :id")
Optional<CProductVersion> findById(@Param ("id") Long id);
}
