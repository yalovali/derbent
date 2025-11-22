package tech.derbent.app.providers.provider.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.providers.provider.domain.CProvider;
import tech.derbent.app.providers.providertype.domain.CProviderType;

public interface IProviderRepository extends IEntityOfProjectRepository<CProvider> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param ("entityType") CProviderType type);
	@Override
	@Query (
		"SELECT r FROM CProvider r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy LEFT JOIN FETCH r.status LEFT JOIN FETCH r.entityType et LEFT JOIN FETCH et.workflow " + "WHERE r.id = :id"
	)
	Optional<CProvider> findById(@Param ("id") Long id);
}
