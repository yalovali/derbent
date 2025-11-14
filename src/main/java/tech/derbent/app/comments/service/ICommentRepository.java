package tech.derbent.app.comments.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.comments.domain.CComment;

/** CCommentRepository - Repository interface for CComment entities. Layer: Service (MVC) - Repository interface Provides data access methods for
 * comment entities with support for: - Activity-based queries - Author-based queries - Chronological ordering - Pagination support */
public interface ICommentRepository extends IAbstractRepository<CComment> {

	long countByActivity(CActivity activity);
	@Query ("SELECT c FROM CComment c WHERE c.activity = :activity ORDER BY c.eventDate ASC")
	List<CComment> findByActivity(@Param ("activity") CActivity activity);
	@Query ("SELECT c FROM CComment c WHERE c.activity = :activity ORDER BY c.eventDate ASC")
	Page<CComment> findByActivity(@Param ("activity") CActivity activity, Pageable pageable);
}
