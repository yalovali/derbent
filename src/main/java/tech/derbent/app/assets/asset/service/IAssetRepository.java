package tech.derbent.app.assets.asset.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.assets.asset.domain.CAsset;
import tech.derbent.app.assets.assettype.domain.CAssetType;

public interface IAssetRepository extends IEntityOfProjectRepository<CAsset> {

	@Query ("SELECT COUNT(a) FROM {#entityName} a WHERE a.entityType = :type")
	long countByType(@Param ("type") CAssetType type);
	@Override
	@Query (
		"SELECT r FROM CAsset r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy LEFT JOIN FETCH r.status LEFT JOIN FETCH r.entityType " + "WHERE r.id = :id"
	)
	Optional<CAsset> findById(@Param ("id") Long id);
}
