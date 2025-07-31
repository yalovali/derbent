package tech.derbent.comments.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.comments.domain.CCommentPriority;

/**
 * CCommentPriorityRepository - Repository interface for CCommentPriority entities.
 * Layer: Service (MVC) - Repository interface
 * 
 * Provides data access methods for comment priority entities.
 */
public interface CCommentPriorityRepository extends CAbstractNamedRepository<CCommentPriority> {

    /**
     * Finds all comment priorities ordered by priority level (highest first).
     * @return list of comment priorities ordered by priority level
     */
    @Query("SELECT p FROM CCommentPriority p ORDER BY p.PriorityLevel ASC")
    List<CCommentPriority> findAllOrderByPriorityLevel();

    /**
     * Finds the default comment priority.
     * @return optional containing the default priority if found
     */
    Optional<CCommentPriority> findByIsDefaultTrue();

    /**
     * Finds comment priority by priority level.
     * @param priorityLevel the priority level
     * @return optional containing the priority if found
     */
    Optional<CCommentPriority> findByPriorityLevel(Integer priorityLevel);
}