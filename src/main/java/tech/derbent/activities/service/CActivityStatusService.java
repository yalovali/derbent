package tech.derbent.activities.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.activities.domain.CActivityStatus;

/**
 * CActivityStatusService - Service class for managing CActivityStatus entities. Layer:
 * Service (MVC) Provides business logic for activity status management including CRUD
 * operations, validation, and workflow management.
 */
@Service
@Transactional
public class CActivityStatusService extends CAbstractService<CActivityStatus> {

	private static final Logger logger =
		LoggerFactory.getLogger(CActivityStatusService.class);
	private final CActivityStatusRepository activityStatusRepository;

	@Autowired
	public CActivityStatusService(
		final CActivityStatusRepository activityStatusRepository, final Clock clock) {
		super(activityStatusRepository, clock);
		logger.debug(
			"CActivityStatusService(activityStatusRepository={}) - Initializing service",
			activityStatusRepository);
		if (activityStatusRepository == null) {
			logger.error(
				"CActivityStatusService constructor - Repository parameter is null");
			throw new IllegalArgumentException(
				"Activity status repository cannot be null");
		}
		this.activityStatusRepository = activityStatusRepository;
	}

	/**
	 * Create default activity statuses if they don't exist. This method should be called
	 * during application startup.
	 */
	public void createDefaultStatusesIfNotExist() {
		logger.debug(
			"createDefaultStatusesIfNotExist() - Creating default activity statuses");
		final String[][] defaultStatuses = {
			{
				"TODO", "Task is ready to be worked on", "#808080", "false", "1" },
			{
				"IN_PROGRESS", "Task is currently being worked on", "#007ACC", "false",
				"2" },
			{
				"REVIEW", "Task is under review", "#FFA500", "false", "3" },
			{
				"BLOCKED", "Task is blocked and cannot proceed", "#FF4444", "false",
				"4" },
			{
				"DONE", "Task has been completed", "#00AA00", "true", "5" },
			{
				"CANCELLED", "Task has been cancelled", "#888888", "true", "6" } };
		for (final String[] statusData : defaultStatuses) {
			final String name = statusData[0];
			if (!existsByName(name)) {
				logger.debug(
					"createDefaultStatusesIfNotExist() - Creating default status: {}",
					name);
				final CActivityStatus status = new CActivityStatus(name, statusData[1],
					statusData[2], Boolean.parseBoolean(statusData[3]));
				status.setSortOrder(Integer.parseInt(statusData[4]));
				save(status);
			}
			else {
				logger.debug(
					"createDefaultStatusesIfNotExist() - Status '{}' already exists",
					name);
			}
		}
		logger.debug(
			"createDefaultStatusesIfNotExist() - Completed creating default activity statuses");
	}

	/**
	 * Delete an activity status by ID.
	 * @param id the status ID - must not be null
	 * @throws IllegalArgumentException if the ID is null
	 */
	public void deleteById(final Long id) {
		logger.debug("deleteById(id={}) - Deleting activity status", id);
		if (id == null) {
			logger.error("deleteById(id=null) - ID parameter is null");
			throw new IllegalArgumentException("Activity status ID cannot be null");
		}
		final Optional<CActivityStatus> existing = activityStatusRepository.findById(id);
		if (!existing.isPresent()) {
			logger.warn("deleteById(id={}) - Activity status not found", id);
			return;
		}
		try {
			activityStatusRepository.deleteById(id);
			logger.debug("deleteById(id={}) - Successfully deleted activity status", id);
		} catch (final Exception e) {
			logger.error("deleteById(id={}) - Error deleting activity status: {}", id,
				e.getMessage(), e);
			throw new RuntimeException("Failed to delete activity status", e);
		}
	}

	/**
	 * Check if an activity status name exists (case-insensitive).
	 * @param name the name to check - must not be null
	 * @return true if the name exists, false otherwise
	 */
	@Transactional(readOnly = true)
	public boolean existsByName(final String name) {
		logger.debug("existsByName(name={}) - Checking if activity status name exists",
			name);
		if ((name == null) || name.trim().isEmpty()) {
			logger.warn("existsByName(name={}) - Name parameter is null or empty", name);
			return false;
		}
		final boolean exists =
			activityStatusRepository.existsByNameIgnoreCase(name.trim());
		logger.debug("existsByName(name={}) - Name exists: {}", name, exists);
		return exists;
	}

	/**
	 * Find all activity statuses ordered by sort order.
	 * @return List of all activity statuses
	 */
	@Transactional(readOnly = true)
	public List<CActivityStatus> findAll() {
		logger.debug("findAll() - Finding all activity statuses");
		final List<CActivityStatus> statuses =
			activityStatusRepository.findAllOrderedBySortOrder();
		logger.debug("findAll() - Found {} activity statuses", statuses.size());
		return statuses;
	}

	/**
	 * Find all active (non-final) statuses.
	 * @return List of active statuses
	 */
	@Transactional(readOnly = true)
	public List<CActivityStatus> findAllActiveStatuses() {
		logger.debug("findAllActiveStatuses() - Finding all active activity statuses");
		final List<CActivityStatus> statuses =
			activityStatusRepository.findAllActiveStatuses();
		logger.debug("findAllActiveStatuses() - Found {} active statuses",
			statuses.size());
		return statuses;
	}

	/**
	 * Find all final statuses (completed/cancelled states).
	 * @return List of final statuses
	 */
	@Transactional(readOnly = true)
	public List<CActivityStatus> findAllFinalStatuses() {
		logger.debug("findAllFinalStatuses() - Finding all final activity statuses");
		final List<CActivityStatus> statuses =
			activityStatusRepository.findAllFinalStatuses();
		logger.debug("findAllFinalStatuses() - Found {} final statuses", statuses.size());
		return statuses;
	}

	/**
	 * Find activity status by ID.
	 * @param id the status ID - must not be null
	 * @return Optional containing the status if found, empty otherwise
	 */
	@Transactional(readOnly = true)
	public Optional<CActivityStatus> findById(final Long id) {
		logger.debug("findById(id={}) - Finding activity status by ID", id);
		if (id == null) {
			logger.warn("findById(id=null) - ID parameter is null");
			return Optional.empty();
		}
		final Optional<CActivityStatus> status = activityStatusRepository.findById(id);
		logger.debug("findById(id={}) - Found status: {}", id, status.isPresent());
		return status;
	}

	/**
	 * Find activity status by name (case-insensitive).
	 * @param name the status name - must not be null or empty
	 * @return Optional containing the status if found, empty otherwise
	 */
	@Transactional(readOnly = true)
	public Optional<CActivityStatus> findByName(final String name) {
		logger.debug("findByName(name={}) - Finding activity status by name", name);
		if ((name == null) || name.trim().isEmpty()) {
			logger.warn("findByName(name={}) - Name parameter is null or empty", name);
			return Optional.empty();
		}
		final Optional<CActivityStatus> status =
			activityStatusRepository.findByNameIgnoreCase(name.trim());
		logger.debug("findByName(name={}) - Found status: {}", name, status.isPresent());
		return status;
	}

	/**
	 * Find the default status for new activities.
	 * @return Optional containing the default status if found
	 */
	@Transactional(readOnly = true)
	public Optional<CActivityStatus> findDefaultStatus() {
		logger.debug("findDefaultStatus() - Finding default activity status");
		final Optional<CActivityStatus> status =
			activityStatusRepository.findDefaultStatus();
		logger.debug("findDefaultStatus() - Found default status: {}",
			status.isPresent());
		return status;
	}

	/**
	 * Save or update an activity status.
	 * @param activityStatus the status to save - must not be null
	 * @return the saved status
	 * @throws IllegalArgumentException if the status is null or invalid
	 */
	@Override
	public CActivityStatus save(final CActivityStatus activityStatus) {
		logger.debug("save(activityStatus={}) - Saving activity status",
			activityStatus != null ? activityStatus.getName() : "null");
		if (activityStatus == null) {
			logger.error("save(activityStatus=null) - Activity status parameter is null");
			throw new IllegalArgumentException("Activity status cannot be null");
		}
		if ((activityStatus.getName() == null)
			|| activityStatus.getName().trim().isEmpty()) {
			logger.error(
				"save() - Activity status name is null or empty for status id={}",
				activityStatus.getId());
			throw new IllegalArgumentException(
				"Activity status name cannot be null or empty");
		}
		// Check for duplicate names (excluding self for updates)
		final String trimmedName = activityStatus.getName().trim();
		final Optional<CActivityStatus> existing =
			activityStatusRepository.findByNameIgnoreCase(trimmedName);
		if (existing.isPresent()
			&& !existing.get().getId().equals(activityStatus.getId())) {
			logger.error("save() - Activity status name '{}' already exists",
				trimmedName);
			throw new IllegalArgumentException(
				"Activity status name '" + trimmedName + "' already exists");
		}
		try {
			final CActivityStatus savedStatus =
				activityStatusRepository.save(activityStatus);
			logger.debug("save() - Successfully saved activity status with id={}",
				savedStatus.getId());
			return savedStatus;
		} catch (final Exception e) {
			logger.error("save() - Error saving activity status: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to save activity status", e);
		}
	}
}