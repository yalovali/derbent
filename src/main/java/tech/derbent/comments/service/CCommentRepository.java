package tech.derbent.comments.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.CAbstractRepository;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.comments.domain.CComment;

/** CCommentRepository - Repository interface for CComment entities. Layer: Service (MVC) - Repository interface Provides data access methods for
 * comment entities with support for: - Activity-based queries - Author-based queries - Chronological ordering - Pagination support */
public interface CCommentRepository extends CAbstractRepository<CComment> {

	long countByActivity(CActivity activity);
	@Query ("SELECT c FROM CComment c WHERE c.activity = :activity ORDER BY c.eventDate ASC")
	List<CComment> findByActivity(@Param ("activity") CActivity activity);
	@Query ("SELECT c FROM CComment c WHERE c.activity = :activity ORDER BY c.eventDate ASC")
	Page<CComment> findByActivity(@Param ("activity") CActivity activity, Pageable pageable);
}
