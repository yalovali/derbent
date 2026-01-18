package tech.derbent.api.domains;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.activities.domain.CActivity;

import java.util.List;

/**
 * Repository interface for CAgileParentRelation entities.
 * Provides methods for querying and managing agile hierarchy relationships.
 */
@Repository
public interface IAgileParentRelationRepository extends JpaRepository<CAgileParentRelation, Long>, IAbstractRepository<CAgileParentRelation> {

    /**
     * Find parent relation by the parent activity.
     * 
     * @param parentActivity the parent activity
     * @return list of parent relations that reference this parent
     */
    List<CAgileParentRelation> findByParentActivity(CActivity parentActivity);

    /**
     * Find all children of a parent activity.
     * This query finds all entities that have this activity as their parent.
     * 
     * @param parentActivityId the ID of the parent activity
     * @return list of parent relations where parentActivity.id matches
     */
    @Query("SELECT pr FROM CAgileParentRelation pr WHERE pr.parentActivity.id = :parentActivityId")
    List<CAgileParentRelation> findChildrenByParentId(@Param("parentActivityId") Long parentActivityId);

    /**
     * Check if setting a parent would create a circular dependency.
     * This uses a recursive query to find all descendants of a potential child.
     * 
     * @param childActivityId ID of the child activity
     * @return list of all descendant activity IDs
     */
    @Query(value = "WITH RECURSIVE descendants AS (" +
        "  SELECT parent_activity_id FROM cagile_parent_relation WHERE parent_activity_id = :childActivityId " +
        "  UNION ALL " +
        "  SELECT pr.parent_activity_id FROM cagile_parent_relation pr " +
        "  INNER JOIN descendants d ON pr.parent_activity_id = d.parent_activity_id" +
        ") SELECT DISTINCT parent_activity_id FROM descendants WHERE parent_activity_id IS NOT NULL",
        nativeQuery = true)
    List<Long> findAllDescendantIds(@Param("childActivityId") Long childActivityId);

    /**
     * Find root items (those without a parent).
     * 
     * @return list of parent relations where parentActivity is null
     */
    @Query("SELECT pr FROM CAgileParentRelation pr WHERE pr.parentActivity IS NULL")
    List<CAgileParentRelation> findRootItems();

    /**
     * Count how many children a parent activity has.
     * 
     * @param parentActivityId the ID of the parent activity
     * @return count of children
     */
    @Query("SELECT COUNT(pr) FROM CAgileParentRelation pr WHERE pr.parentActivity.id = :parentActivityId")
    long countChildrenByParentId(@Param("parentActivityId") Long parentActivityId);
}
