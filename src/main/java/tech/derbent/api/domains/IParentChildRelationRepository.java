package tech.derbent.api.domains;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;

/** Repository interface for managing parent-child relationships between project items.
 * Provides queries for finding relationships, checking for circular dependencies,
 * and managing hierarchical structures. */
public interface IParentChildRelationRepository extends IAbstractRepository<CParentChildRelation> {

	/** Find all children of a specific parent.
	 * @param parentId   the ID of the parent entity
	 * @param parentType the class name of the parent entity (e.g., "CActivity")
	 * @return list of parent-child relations where the given entity is the parent */
	@Query("SELECT r FROM #{#entityName} r WHERE r.parentId = :parentId AND r.parentType = :parentType ORDER BY r.id ASC")
	List<CParentChildRelation> findByParent(@Param("parentId") Long parentId, @Param("parentType") String parentType);

	/** Find the parent of a specific child.
	 * @param childId   the ID of the child entity
	 * @param childType the class name of the child entity (e.g., "CActivity")
	 * @return optional parent-child relation where the given entity is the child */
	@Query("SELECT r FROM #{#entityName} r WHERE r.childId = :childId AND r.childType = :childType")
	Optional<CParentChildRelation> findByChild(@Param("childId") Long childId, @Param("childType") String childType);

	/** Check if a relationship exists between parent and child.
	 * @param childId    the ID of the child entity
	 * @param childType  the class name of the child entity
	 * @param parentId   the ID of the parent entity
	 * @param parentType the class name of the parent entity
	 * @return true if the relationship exists */
	@Query("SELECT COUNT(r) > 0 FROM #{#entityName} r WHERE r.childId = :childId AND r.childType = :childType "
			+ "AND r.parentId = :parentId AND r.parentType = :parentType")
	boolean existsByChildAndParent(@Param("childId") Long childId, @Param("childType") String childType, @Param("parentId") Long parentId,
			@Param("parentType") String parentType);

	/** Delete relationship by child entity.
	 * @param childId   the ID of the child entity
	 * @param childType the class name of the child entity
	 * @return number of deleted relationships */
	@Query("DELETE FROM #{#entityName} r WHERE r.childId = :childId AND r.childType = :childType")
	int deleteByChild(@Param("childId") Long childId, @Param("childType") String childType);

	/** Find all descendants (recursive) of a parent entity.
	 * This is useful for checking circular dependencies and validating hierarchy.
	 * @param parentId   the ID of the parent entity
	 * @param parentType the class name of the parent entity
	 * @return list of all descendant relations (children, grandchildren, etc.) */
	@Query(value = "WITH RECURSIVE descendants AS ( " + "  SELECT child_id, child_type, parent_id, parent_type, 1 as depth "
			+ "  FROM cparentchildrelation " + "  WHERE parent_id = :parentId AND parent_type = :parentType " + "  UNION ALL "
			+ "  SELECT r.child_id, r.child_type, r.parent_id, r.parent_type, d.depth + 1 " + "  FROM cparentchildrelation r "
			+ "  INNER JOIN descendants d ON r.parent_id = d.child_id AND r.parent_type = d.child_type " + "  WHERE d.depth < 10 "
			+ ") SELECT * FROM descendants", nativeQuery = true)
	List<Object[]> findAllDescendants(@Param("parentId") Long parentId, @Param("parentType") String parentType);
}
