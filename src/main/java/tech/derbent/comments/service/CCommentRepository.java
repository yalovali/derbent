package tech.derbent.comments.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.comments.domain.CComment;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * CCommentRepository - Repository interface for CComment entities.
 * Layer: Service (MVC) - Repository interface
 * 
 * Provides data access methods for comment entities with support for:
 * - Activity-based queries
 * - Project-based queries  
 * - Author-based queries
 * - Chronological ordering
 * - Eager loading to prevent lazy initialization exceptions
 */
public interface CCommentRepository extends CAbstractNamedRepository<CComment> {

    /**
     * Finds all comments for a specific activity, ordered by event date (chronological).
     * @param activity the activity
     * @return list of comments for the activity ordered by event date
     */
    @Query("SELECT c FROM CComment c WHERE c.activity = :activity ORDER BY c.eventDate ASC")
    List<CComment> findByActivityOrderByEventDateAsc(@Param("activity") CActivity activity);

    /**
     * Finds all comments for a specific activity with pagination, ordered by event date.
     * @param activity the activity
     * @param pageable pagination information
     * @return page of comments for the activity ordered by event date
     */
    @Query("SELECT c FROM CComment c WHERE c.activity = :activity ORDER BY c.eventDate ASC")
    Page<CComment> findByActivityOrderByEventDateAsc(@Param("activity") CActivity activity, Pageable pageable);

    /**
     * Finds all comments for a specific project, ordered by event date with eagerly loaded relationships.
     * @param project the project
     * @return list of comments for the project ordered by event date
     */
    @Query("SELECT c FROM CComment c LEFT JOIN FETCH c.author LEFT JOIN FETCH c.priority WHERE c.project = :project ORDER BY c.eventDate ASC")
    List<CComment> findByProjectOrderByEventDateAsc(@Param("project") CProject project);

    /**
     * Finds all comments by a specific author, ordered by event date with eagerly loaded relationships.
     * @param author the comment author
     * @return list of comments by the author ordered by event date
     */
    @Query("SELECT c FROM CComment c LEFT JOIN FETCH c.priority WHERE c.author = :author ORDER BY c.eventDate DESC")
    List<CComment> findByAuthorOrderByEventDateDesc(@Param("author") CUser author);

    /**
     * Finds comment by ID with eagerly loaded activity, author, and priority.
     * @param id the comment ID
     * @return optional CComment with loaded relationships
     */
    @Query("SELECT c FROM CComment c LEFT JOIN FETCH c.activity LEFT JOIN FETCH c.author LEFT JOIN FETCH c.priority WHERE c.id = :id")
    Optional<CComment> findByIdWithRelationships(@Param("id") Long id);

    /**
     * Finds all comments for an activity with eagerly loaded relationships.
     * @param activity the activity
     * @return list of comments with loaded relationships ordered by event date
     */
    @Query("SELECT c FROM CComment c LEFT JOIN FETCH c.author LEFT JOIN FETCH c.priority WHERE c.activity = :activity ORDER BY c.eventDate ASC")
    List<CComment> findByActivityWithRelationships(@Param("activity") CActivity activity);

    /**
     * Counts the number of comments for a specific activity.
     * @param activity the activity
     * @return count of comments for the activity
     */
    long countByActivity(CActivity activity);

    /**
     * Counts the number of comments for a specific project.
     * @param project the project  
     * @return count of comments for the project
     */
    long countByProject(CProject project);

    /**
     * Finds important comments for an activity with eagerly loaded relationships.
     * @param activity the activity
     * @return list of important comments for the activity ordered by event date
     */
    @Query("SELECT c FROM CComment c LEFT JOIN FETCH c.author LEFT JOIN FETCH c.priority WHERE c.activity = :activity AND c.important = true ORDER BY c.eventDate ASC")
    List<CComment> findImportantByActivity(@Param("activity") CActivity activity);

    /**
     * Finds recent comments for a project (last 30 days) with eagerly loaded relationships.
     * Uses parameter-based date calculation for database portability and eager loading to prevent lazy loading issues.
     * @param project the project
     * @param since the date threshold (30 days ago)
     * @return list of recent comments for the project ordered by event date (newest first)
     */
    @Query("SELECT c FROM CComment c LEFT JOIN FETCH c.author LEFT JOIN FETCH c.priority WHERE c.project = :project AND c.eventDate >= :since ORDER BY c.eventDate DESC")
    List<CComment> findRecentByProject(@Param("project") CProject project, @Param("since") java.time.LocalDateTime since);
}