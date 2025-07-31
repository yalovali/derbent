package tech.derbent.comments.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.comments.domain.CCommentPriority;

/**
 * CCommentPriorityService - Service class for CCommentPriority entities. Layer: Service
 * (MVC) Provides business logic operations for comment priority management including: -
 * CRUD operations - Priority level management - Default priority handling - Data provider
 * functionality for UI components
 */
@Service
@PreAuthorize ("isAuthenticated()")
public class CCommentPriorityService
	extends CAbstractNamedEntityService<CCommentPriority> {

	/**
	 * Constructor for CCommentPriorityService.
	 * @param repository the comment priority repository
	 * @param clock      the Clock instance for time-related operations
	 */
	CCommentPriorityService(final CCommentPriorityRepository repository,
		final Clock clock) {
		super(repository, clock);
	}

	@Override
	protected CCommentPriority createNewEntityInstance() {
		return new CCommentPriority();
	}

	/**
	 * Finds all comment priorities ordered by priority level.
	 * @return list of comment priorities ordered by priority level (highest first)
	 */
	@PreAuthorize ("permitAll()")
	public List<CCommentPriority> findAllOrderByPriorityLevel() {
		LOGGER.info("findAllOrderByPriorityLevel called");
		return ((CCommentPriorityRepository) repository).findAllOrderByPriorityLevel();
	}

	/**
	 * Finds the default comment priority.
	 * @return optional containing the default priority if found
	 */
	@PreAuthorize ("permitAll()")
	public Optional<CCommentPriority> findDefaultPriority() {
		LOGGER.info("findDefaultPriority called");
		return ((CCommentPriorityRepository) repository).findByIsDefaultTrue();
	}

	/**
	 * Finds comment priority by priority level.
	 * @param priorityLevel the priority level
	 * @return optional containing the priority if found
	 */
	@PreAuthorize ("permitAll()")
	public Optional<CCommentPriority> findByPriorityLevel(final Integer priorityLevel) {
		LOGGER.info("findByPriorityLevel called with priorityLevel: {}", priorityLevel);

		if (priorityLevel == null) {
			LOGGER.warn("findByPriorityLevel called with null priorityLevel");
			return Optional.empty();
		}
		return ((CCommentPriorityRepository) repository)
			.findByPriorityLevel(priorityLevel);
	}

	/**
	 * Sets a priority as the default, ensuring only one default exists.
	 * @param priority the priority to set as default
	 */
	@Transactional
	public void setAsDefault(final CCommentPriority priority) {
		LOGGER.info("setAsDefault called with priority: {}", priority);

		if (priority == null) {
			LOGGER.warn("setAsDefault called with null priority");
			return;
		}
		// First, unset any existing default
		final Optional<CCommentPriority> currentDefault = findDefaultPriority();

		if (currentDefault.isPresent() && !currentDefault.get().equals(priority)) {
			currentDefault.get().setDefault(false);
			save(currentDefault.get());
		}
		// Set the new default
		priority.setDefault(true);
		save(priority);
	}

	/**
	 * Creates a new comment priority with name and priority level.
	 * @param name          the priority name
	 * @param priorityLevel the priority level (1=Highest, 4=Lowest)
	 * @return the created comment priority
	 */
	@Transactional
	public CCommentPriority createPriority(final String name,
		final Integer priorityLevel) {
		LOGGER.info("createPriority called with name: {}, priorityLevel: {}", name,
			priorityLevel);

		if ((name == null) || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Priority name cannot be null or empty");
		}

		if ((priorityLevel == null) || (priorityLevel < 1) || (priorityLevel > 4)) {
			throw new IllegalArgumentException("Priority level must be between 1 and 4");
		}
		final CCommentPriority priority = new CCommentPriority(name, priorityLevel);
		return save(priority);
	}

	/**
     * Creates a new comment priority with name, priority level, and description.
     * @param name the priority name
     * @param priorityLevel the priority level (1=Highest, 4=Lowest)
     * @param description the priority description
     * @return the created comment priority
     */
    @Transactional
    public CCommentPriority createPriority(final String name, final Integer priorityLevel, final String description) {
        LOGGER.info("createPriority called with name: {}, priorityLevel: {}, description: {}",
            name, priorityLevel, description);

        if ((name == null) || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Priority name cannot be null or empty");
        }

        if ((priorityLevel == null) || (priorityLevel < 1) || (priorityLevel > 4)) {
            throw new IllegalArgumentException("Priority level must be between 1 and 4");
        }

        final CCommentPriority priority = new CCommentPriority(name, priorityLevel);
        priority.setDescription(description);

        return save(priority);
    }

	/**
	 * Gets or creates the default NORMAL priority.
	 * @return the default normal priority
	 */
	@Transactional
	public CCommentPriority getOrCreateDefaultPriority() {
		LOGGER.info("getOrCreateDefaultPriority called");
		final Optional<CCommentPriority> defaultPriority = findDefaultPriority();

		if (defaultPriority.isPresent()) {
			return defaultPriority.get();
		}
		// Create default normal priority if none exists
		final CCommentPriority normalPriority =
			createPriority("Normal", 2, "Standard priority for comments");
		normalPriority.setColor("#0066CC");
		normalPriority.setDefault(true);
		return save(normalPriority);
	}
}