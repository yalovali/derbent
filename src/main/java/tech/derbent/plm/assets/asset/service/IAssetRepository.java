package tech.derbent.plm.assets.asset.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.plm.assets.asset.domain.CAsset;
import tech.derbent.plm.assets.assettype.domain.CAssetType;
import tech.derbent.api.projects.domain.CProject;

public interface IAssetRepository extends IEntityOfProjectRepository<CAsset> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param ("entityType") CAssetType type);
	@Override
	@Query (
		"SELECT r FROM CAsset r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy LEFT JOIN FETCH r.status LEFT JOIN FETCH r.entityType et LEFT JOIN FETCH et.workflow LEFT JOIN FETCH r.attachments LEFT JOIN FETCH r.comments LEFT JOIN FETCH r.links " + "WHERE r.id = :id"
	)
	Optional<CAsset> findById(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT r FROM CAsset r
			LEFT JOIN FETCH r.project
			LEFT JOIN FETCH r.assignedTo
			LEFT JOIN FETCH r.createdBy
			LEFT JOIN FETCH r.status
			LEFT JOIN FETCH r.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH r.attachments
			LEFT JOIN FETCH r.comments
			LEFT JOIN FETCH r.links
			WHERE r.project = :project
			ORDER BY r.name ASC
			""")
	List<CAsset> listByProjectForPageView(@Param ("project") CProject<?> project);
}
