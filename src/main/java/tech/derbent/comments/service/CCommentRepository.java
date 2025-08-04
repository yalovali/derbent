package tech.derbent.comments.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.comments.domain.CComment;
import tech.derbent.users.domain.CUser;

/**
 * CCommentRepository - Repository interface for CComment entities. Layer: Service (MVC) - Repository interface Provides
 * data access methods for comment entities with support for: - Activity-based queries - Author-based queries -
 * Chronological ordering - Pagination support
 */
public interface CCommentRepository extends CAbstractRepository<CComment> {

    /**
     * Counts the number of comments for a specific activity.
     * 
     * @param activity
     *            the activity
     * @return count of comments for the activity
     */
    long countByActivity(CActivity activity);

    /**
     * Finds all comments for a specific activity, ordered by event date (chronological).
     * 
     * @param activity
     *            the activity
     * @return list of comments for the activity ordered by event date
     */
    @Query("SELECT c FROM CComment c WHERE c.activity = :activity ORDER BY c.eventDate ASC")
    List<CComment> findByActivityOrderByEventDateAsc(@Param("activity") CActivity activity);

    /**
     * Finds all comments for a specific activity with pagination, ordered by event date.
     * 
     * @param activity
     *            the activity
     * @param pageable
     *            pagination information
     * @return page of comments for the activity ordered by event date
     */
    @Query("SELECT c FROM CComment c WHERE c.activity = :activity ORDER BY c.eventDate ASC")
    Page<CComment> findByActivityOrderByEventDateAsc(@Param("activity") CActivity activity, Pageable pageable);

    /**
     * Finds all comments by a specific author, ordered by event date.
     * 
     * @param author
     *            the comment author
     * @return list of comments by the author ordered by event date
     */
    @Query("SELECT c FROM CComment c WHERE c.author = :author ORDER BY c.eventDate DESC")
    List<CComment> findByAuthorOrderByEventDateDesc(@Param("author") CUser author);

    /**
     * Finds important comments for an activity.
     * 
     * @param activity
     *            the activity
     * @return list of important comments for the activity ordered by event date
     */
    @Query("SELECT c FROM CComment c WHERE c.activity = :activity AND c.important = true ORDER BY c.eventDate ASC")
    List<CComment> findImportantByActivity(@Param("activity") CActivity activity);
}