package tech.derbent.comments.service;

import java.util.Optional;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.comments.domain.CCommentPriority;

/**
 * CCommentPriorityRepository - Repository interface for CCommentPriority entities. Layer:
 * Service (MVC) - Repository interface Provides data access methods for comment priority
 * entities.
 */
public interface CCommentPriorityRepository
	extends CEntityOfProjectRepository<CCommentPriority> {

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