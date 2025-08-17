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
 * CMeetingStatusService - Service class for managing CMeetingStatus entities. Layer: Service (MVC) Provides business
 * logic for meeting status management including CRUD operations, validation, and workflow management. Since
 * CMeetingStatus extends CStatus which extends CTypeEntity which extends CEntityOfProject, this service must extend
 * CEntityOfProjectService to enforce project-based queries.
 */
@Service
@Transactional
public class CMeetingStatusService extends CEntityOfProjectService<CMeetingStatus> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingStatusService.class);

    @Autowired
    public CMeetingStatusService(final CMeetingStatusRepository meetingStatusRepository, final Clock clock) {
        super(meetingStatusRepository, clock);

        if (meetingStatusRepository == null) {
            LOGGER.error("CMeetingStatusService constructor - Repository parameter is null");
            throw new IllegalArgumentException("Meeting status repository cannot be null");
        }
    }

    /**
     * Create default meeting statuses if they don't exist. This method should be called during application startup.
     */
    public void createDefaultStatusesIfNotExist() {
        // TODO implement default statuses creation logic
    }

    /**
     * Find all active (non-final) statuses for a specific project.
     * 
     * @param project
     *            the project to find statuses for
     * @return List of active statuses for the project
     */
    @Transactional(readOnly = true)
    public List<CMeetingStatus> findAllActiveStatusesByProject(final CProject project) {
        // Use the inherited findAllByProject and filter for active statuses
        return findEntriesByProject(project).stream().filter(status -> !status.getFinalStatus()).toList();
    }

    /**
     * Find the default status for new meetings.
     * 
     * @return Optional containing the default status if found
     */
    @Transactional(readOnly = true)
    public Optional<CMeetingStatus> findDefaultStatus(final CProject project) {
        LOGGER.debug("findDefaultStatus() - Finding default meeting status");
        final Optional<CMeetingStatus> status = ((CMeetingStatusService) repository).findDefaultStatus(project);
        return status;
    }

    @Override
    protected Class<CMeetingStatus> getEntityClass() {
        return CMeetingStatus.class;
    }
}