package tech.derbent.api.agileparentrelation.service;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.agileparentrelation.domain.CAgileParentRelation;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.api.entityOfProject.domain.CProjectItem;

/** Repository interface for CAgileParentRelation entities. Provides methods for querying and managing agile hierarchy relationships. */
@Repository
public interface IAgileParentRelationRepository extends JpaRepository<CAgileParentRelation, Long>, IAbstractRepository<CAgileParentRelation> {

	/** Count how many children a parent item has.
	 * @param parentItemId the ID of the parent item
	 * @return count of children */
	@Query ("SELECT COUNT(pr) FROM CAgileParentRelation pr WHERE pr.parentItem.id = :parentItemId")
	long countChildrenByParentId(@Param ("parentItemId") Long parentItemId);
	/** Check if setting a parent would create a circular dependency. This uses a recursive query to find all descendants of a potential child.
	 * @param childItemId ID of the child item
	 * @return list of all descendant item IDs */
	@Query (
			value = "WITH RECURSIVE descendants AS ("
					+ "  SELECT parent_item_id FROM cagile_parent_relation WHERE parent_item_id = :childItemId " + "  UNION ALL "
					+ "  SELECT pr.parent_item_id FROM cagile_parent_relation pr "
					+ "  INNER JOIN descendants d ON pr.parent_item_id = d.parent_item_id"
					+ ") SELECT DISTINCT parent_item_id FROM descendants WHERE parent_item_id IS NOT NULL",
			nativeQuery = true
	)
	List<Long> findAllDescendantIds(@Param ("childItemId") Long childItemId);
	/** Find parent relation by the parent item.
	 * @param parentItem the parent item
	 * @return list of parent relations that reference this parent */
	List<CAgileParentRelation> findByParentItem(CProjectItem<?> parentItem);
	/** Find all children of a parent item. This query finds all entities that have this item as their parent.
	 * @param parentItemId the ID of the parent item
	 * @return list of parent relations where parentItem.id matches */
	@Query ("SELECT pr FROM CAgileParentRelation pr WHERE pr.parentItem.id = :parentItemId")
	List<CAgileParentRelation> findChildrenByParentId(@Param ("parentItemId") Long parentItemId);
	/** Find root items (those without a parent).
	 * @return list of parent relations where parentItem is null */
	@Query ("SELECT pr FROM CAgileParentRelation pr WHERE pr.parentItem IS NULL")
	List<CAgileParentRelation> findRootItems();
}
