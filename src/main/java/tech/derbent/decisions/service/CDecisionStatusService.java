package tech.derbent.decisions.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CDecisionStatusService - Service class for CDecisionStatus entities. Layer: Service (MVC) Provides business logic
 * operations for decision status management including validation, creation, and workflow management.
 * Since CDecisionStatus extends CStatus which extends CTypeEntity which extends CEntityOfProject,
 * this service must extend CEntityOfProjectService to enforce project-based queries.
 */
@Service
@PreAuthorize("isAuthenticated()")
public class CDecisionStatusService extends CEntityOfProjectService<CDecisionStatus> {

    public CDecisionStatusService(final CDecisionStatusRepository repository, final Clock clock) {
        super(repository, clock);
    }

    @Override
    protected Class<CDecisionStatus> getEntityClass() {
        return CDecisionStatus.class;
    }

    /**
     * Finds all non-final decision statuses for a specific project ordered by sort order.
     * This replaces the problematic findAllActiveOrdered() method that didn't require project.
     * 
     * @param project the project to find statuses for
     * @return list of non-final decision statuses sorted by sort order for the project
     */
    @Transactional(readOnly = true)
    public List<CDecisionStatus> findAllActiveOrderedByProject(final CProject project) {
        LOGGER.info("findAllActiveOrderedByProject called for decision statuses in project: {}", 
                project != null ? project.getName() : "null");
        
        if (project == null) {
            LOGGER.warn("findAllActiveOrderedByProject called with null project");
            return List.of();
        }
        
        return ((CDecisionStatusRepository) repository).findByProjectAndIsFinalFalseOrderBySortOrderAsc(project);
    }

    /**
     * Finds all final decision statuses for a specific project.
     * This replaces the problematic findAllFinal() method that didn't require project.
     * 
     * @param project the project to find statuses for
     * @return list of final decision statuses for the project
     */
    @Transactional(readOnly = true)
    public List<CDecisionStatus> findAllFinalByProject(final CProject project) {
        LOGGER.info("findAllFinalByProject called for decision statuses in project: {}", 
                project != null ? project.getName() : "null");
        
        if (project == null) {
            LOGGER.warn("findAllFinalByProject called with null project");
            return List.of();
        }
        
        return ((CDecisionStatusRepository) repository).findByProjectAndIsFinalTrue(project);
    }

    /**
     * Finds all decision statuses for a specific project ordered by sort order.
     * This replaces the problematic findAllOrdered() method that didn't require project.
     * 
     * @param project the project to find statuses for
     * @return list of decision statuses sorted by sort order for the project
     */
    @Transactional(readOnly = true)
    public List<CDecisionStatus> findAllOrderedByProject(final CProject project) {
        LOGGER.info("findAllOrderedByProject called for decision statuses in project: {}", 
                project != null ? project.getName() : "null");
        
        if (project == null) {
            LOGGER.warn("findAllOrderedByProject called with null project");
            return List.of();
        }
        
        return ((CDecisionStatusRepository) repository).findAllByProjectOrderBySortOrderAsc(project);
    }

    /**
     * Finds decision statuses that require approval for a specific project.
     * This replaces the problematic findRequiringApproval() method that didn't require project.
     * 
     * @param project the project to find statuses for
     * @return list of decision statuses that require approval for the project
     */
    @Transactional(readOnly = true)
    public List<CDecisionStatus> findRequiringApprovalByProject(final CProject project) {
        LOGGER.info("findRequiringApprovalByProject called for decision statuses in project: {}", 
                project != null ? project.getName() : "null");
        
        if (project == null) {
            LOGGER.warn("findRequiringApprovalByProject called with null project");
            return List.of();
        }
        
        return ((CDecisionStatusRepository) repository).findByProjectAndRequiresApprovalTrue(project);
    }

    /**
     * Override get() method to use eager loading and prevent LazyInitializationException.
     * 
     * @param id
     *            the decision status ID
     * @return optional CDecisionStatus with relationships loaded
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CDecisionStatus> getById(final Long id) {
        LOGGER.info("get called with id: {} (overridden to use eager loading)", id);

        if (id == null) {
            return Optional.empty();
        }
        final Optional<CDecisionStatus> entity = ((CDecisionStatusRepository) repository).findByIdWithEagerLoading(id);
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }

    /**
     * Checks if this status indicates completion of the decision process.
     * 
     * @param decisionStatus
     *            the decision status to check
     * @return true if this is a final status
     */
    @Transactional(readOnly = true)
    public boolean isStatusCompleted(final CDecisionStatus decisionStatus) {
        LOGGER.info("isStatusCompleted called with decisionStatus: {}",
                decisionStatus != null ? decisionStatus.getName() : "null");

        if (decisionStatus == null) {
            LOGGER.warn("isStatusCompleted called with null decisionStatus");
            return false;
        }
        return decisionStatus.isCompleted();
    }

    /**
     * Checks if decisions with this status are pending approval.
     * 
     * @param decisionStatus
     *            the decision status to check
     * @return true if approval is required and status is not final
     */
    @Transactional(readOnly = true)
    public boolean isStatusPendingApproval(final CDecisionStatus decisionStatus) {
        LOGGER.info("isStatusPendingApproval called with decisionStatus: {}",
                decisionStatus != null ? decisionStatus.getName() : "null");

        if (decisionStatus == null) {
            LOGGER.warn("isStatusPendingApproval called with null decisionStatus");
            return false;
        }
        return decisionStatus.isPendingApproval();
    }

    /**
     * Updates the sort order of a decision status.
     * 
     * @param decisionStatus
     *            the decision status to update - must not be null
     * @param sortOrder
     *            the new sort order
     * @return the updated decision status
     */
    @Transactional
    public CDecisionStatus updateSortOrder(final CDecisionStatus decisionStatus, final Integer sortOrder) {
        LOGGER.info("updateSortOrder called with decisionStatus: {}, sortOrder: {}",
                decisionStatus != null ? decisionStatus.getName() : "null", sortOrder);

        if (decisionStatus == null) {
            LOGGER.error("updateSortOrder called with null decisionStatus");
            throw new IllegalArgumentException("Decision status cannot be null");
        }
        decisionStatus.setSortOrder(sortOrder);
        return repository.saveAndFlush(decisionStatus);
    }

    /**
     * Updates the properties of a decision status.
     * 
     * @param decisionStatus
     *            the decision status to update - must not be null
     * @param allowsEditing
     *            whether decisions with this status can be edited
     * @param requiresApproval
     *            whether decisions with this status require approval
     * @return the updated decision status
     */
    @Transactional
    public CDecisionStatus updateStatusProperties(final CDecisionStatus decisionStatus,
            final boolean requiresApproval) {
        LOGGER.info("updateStatusProperties called with decisionStatus: {}, requiresApproval: {}",
                decisionStatus != null ? decisionStatus.getName() : "null", requiresApproval);

        if (decisionStatus == null) {
            LOGGER.error("updateStatusProperties called with null decisionStatus");
            throw new IllegalArgumentException("Decision status cannot be null");
        }
        decisionStatus.setRequiresApproval(requiresApproval);
        return repository.saveAndFlush(decisionStatus);
    }
}