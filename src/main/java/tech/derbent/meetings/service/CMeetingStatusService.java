package tech.derbent.meetings.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CMeetingStatusService - Service class for managing CMeetingStatus entities. Layer:
 * Service (MVC) Provides business logic for meeting status management including CRUD
 * operations, validation, and workflow management. Since CMeetingStatus extends CStatus
 * which extends CTypeEntity which extends CEntityOfProject, this service must extend
 * CEntityOfProjectService to enforce project-based queries.
 */
@Service
@Transactional
public class CMeetingStatusService extends CEntityOfProjectService<CMeetingStatus> {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CMeetingStatusService.class);

	private final CMeetingStatusRepository meetingStatusRepository;

	@Autowired
	public CMeetingStatusService(final CMeetingStatusRepository meetingStatusRepository,
		final Clock clock) {
		super(meetingStatusRepository, clock);

		if (meetingStatusRepository == null) {
			LOGGER.error(
				"CMeetingStatusService constructor - Repository parameter is null");
			throw new IllegalArgumentException(
				"Meeting status repository cannot be null");
		}
		this.meetingStatusRepository = meetingStatusRepository;
	}

	/**
	 * Create default meeting statuses if they don't exist. This method should be called
	 * during application startup.
	 */
	public void createDefaultStatusesIfNotExist() {
		// TODO implement default statuses creation logic
	}

	/**
	 * Delete a meeting status by ID.
	 * @param id the status ID - must not be null
	 * @throws IllegalArgumentException if the ID is null
	 */
	public void deleteById(final Long id) {
		LOGGER.debug("deleteById(id={}) - Deleting meeting status", id);

		if (id == null) {
			LOGGER.error("deleteById(id=null) - ID parameter is null");
			throw new IllegalArgumentException("Meeting status ID cannot be null");
		}
		final Optional<CMeetingStatus> existing = meetingStatusRepository.findById(id);

		if (!existing.isPresent()) {
			LOGGER.warn("deleteById(id={}) - Meeting status not found", id);
			return;
		}

		try {
			meetingStatusRepository.deleteById(id);
			LOGGER.debug("deleteById(id={}) - Successfully deleted meeting status", id);
		} catch (final Exception e) {
			LOGGER.error("deleteById(id={}) - Error deleting meeting status: {}", id,
				e.getMessage(), e);
			throw new RuntimeException("Failed to delete meeting status", e);
		}
	}

	/**
	 * Check if a meeting status name exists (case-insensitive).
	 * @param name the name to check - must not be null
	 * @return true if the name exists, false otherwise
	 */
	@Override
	@Transactional (readOnly = true)
	public boolean existsByName(final String name) {
		LOGGER.debug("existsByName(name={}) - Checking if meeting status name exists",
			name);

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.warn("existsByName(name={}) - Name parameter is null or empty", name);
			return false;
		}
		final boolean exists =
			meetingStatusRepository.existsByNameIgnoreCase(name.trim());
		LOGGER.debug("existsByName(name={}) - Name exists: {}", name, exists);
		return exists;
	}

	/**
	 * Find all active (non-final) statuses for a specific project.
	 * @param project the project to find statuses for
	 * @return List of active statuses for the project
	 */
	@Transactional (readOnly = true)
	public List<CMeetingStatus> findAllActiveStatusesByProject(final CProject project) {
		// Use the inherited findAllByProject and filter for active statuses
		return findEntriesByProject(project).stream().filter(status -> !status.isFinal())
			.toList();
	}

	/**
	 * Find the default status for new meetings.
	 * @return Optional containing the default status if found
	 */
	@Transactional (readOnly = true)
	public Optional<CMeetingStatus> findDefaultStatus(final CProject project) {
		LOGGER.debug("findDefaultStatus() - Finding default meeting status");
		final Optional<CMeetingStatus> status =
			meetingStatusRepository.findDefaultStatus(project);
		return status;
	}

	@Override
	protected Class<CMeetingStatus> getEntityClass() { return CMeetingStatus.class; }

	/**
	 * Save or update a meeting status.
	 * @param status the status to save - must not be null
	 * @return the saved status
	 * @throws IllegalArgumentException if the status is null or invalid
	 */
	@Override
	public CMeetingStatus save(final CMeetingStatus status) {
		LOGGER.debug("save(status={}) - Saving meeting status",
			status != null ? status.getName() : "null");

		if (status == null) {
			LOGGER.error("save(meetingStatus=null) - Meeting status parameter is null");
			throw new IllegalArgumentException("Meeting status cannot be null");
		}

		if ((status.getName() == null) || status.getName().trim().isEmpty()) {
			LOGGER.error("save() - Meeting status name is null or empty for status id={}",
				status.getId());
			throw new IllegalArgumentException(
				"Meeting status name cannot be null or empty");
		}
		// Check for duplicate names (excluding self for updates)
		final String trimmedName = status.getName().trim();
		// search with same name and same project
		final Optional<CMeetingStatus> existing = meetingStatusRepository
			.findByNameAndProject(trimmedName, status.getProject());

		if (existing.isPresent()) {
			LOGGER.error("save() - Meeting status name '{}' already exists", trimmedName);
			throw new IllegalArgumentException(
				"Meeting status name '" + trimmedName + "' already exists");
		}

		try {
			final CMeetingStatus savedStatus = meetingStatusRepository.save(status);
			LOGGER.debug("save() - Successfully saved meeting status with id={}",
				savedStatus.getId());
			return savedStatus;
		} catch (final Exception e) {
			LOGGER.error("save() - Error saving meeting status: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to save meeting status", e);
		}
	}
}