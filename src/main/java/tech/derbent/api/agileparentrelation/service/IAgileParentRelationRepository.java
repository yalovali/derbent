package tech.derbent.api.agileparentrelation.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.agileparentrelation.domain.CAgileParentRelation;
import tech.derbent.api.entity.service.IAbstractRepository;

public interface IAgileParentRelationRepository extends IAbstractRepository<CAgileParentRelation> {

	@Query("SELECT COUNT(pr) FROM CAgileParentRelation pr WHERE pr.parentItemId = :parentItemId")
	long countChildrenByParentId(@Param("parentItemId") Long parentItemId);
	
	@Query(
			value = "WITH RECURSIVE descendants AS (" + 
					"  SELECT parent_item_id FROM cagile_parent_relation WHERE parent_item_id = :childItemId " + 
					"  UNION ALL " + 
					"  SELECT pr.parent_item_id FROM cagile_parent_relation pr " + 
					"  INNER JOIN descendants d ON pr.parent_item_id = d.parent_item_id" + 
					") SELECT DISTINCT parent_item_id FROM descendants WHERE parent_item_id IS NOT NULL",
			nativeQuery = true)
	List<Long> findAllDescendantIds(@Param("childItemId") Long childItemId);
	
	@Query("SELECT pr FROM CAgileParentRelation pr WHERE pr.parentItemId = :parentItemId")
	List<CAgileParentRelation> findChildrenByParentId(@Param("parentItemId") Long parentItemId);
	
	@Query("SELECT pr FROM CAgileParentRelation pr WHERE pr.parentItemId IS NULL")
	List<CAgileParentRelation> findRootItems();
}
