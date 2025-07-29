package tech.derbent.decisions.service;

import java.time.Clock;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.projects.domain.CProject;

/**
 * CDecisionTypeService - Service class for CDecisionType entities.
 * Layer: Service (MVC)
 * 
 * Provides business logic operations for project-aware decision type management including
 * validation, creation, and status management.
 */
@Service
@PreAuthorize("isAuthenticated()")
public class CDecisionTypeService extends CEntityOfProjectService<CDecisionType> {

    public CDecisionTypeService(final CDecisionTypeRepository repository, final Clock clock) {
        super(repository, clock);
    }

    @Override
    protected CDecisionType createNewEntityInstance() {
        return new CDecisionType();
    }

    /**
     * Finds all active decision types for a project.
     * @param project the project
     * @return list of active decision types for the project
     */
    @Transactional(readOnly = true)
    public List<CDecisionType> findAllActiveByProject(final CProject project) {
        LOGGER.info("findAllActiveByProject called for project: {}", project.getName());
        return ((CDecisionTypeRepository) repository).findByProjectAndIsActiveTrue(project);
    }

    /**
     * Finds all decision types for a project ordered by sort order.
     * @param project the project
     * @return list of decision types for the project sorted by sort order
     */
    @Transactional(readOnly = true)
    public List<CDecisionType> findAllByProjectOrdered(final CProject project) {
        LOGGER.info("findAllByProjectOrdered called for project: {}", project.getName());
        return ((CDecisionTypeRepository) repository).findByProjectOrderBySortOrderAsc(project);
    }

    /**
     * Finds decision types that require approval for a project.
     * @param project the project
     * @return list of decision types that require approval for the project
     */
    @Transactional(readOnly = true)
    public List<CDecisionType> findRequiringApprovalByProject(final CProject project) {
        LOGGER.info("findRequiringApprovalByProject called for project: {}", project.getName());
        return ((CDecisionTypeRepository) repository).findByProjectAndRequiresApprovalTrue(project);
    }

    /**
     * Creates a new decision type with basic properties.
     * @param name the decision type name - must not be null or empty
     * @param description the description - can be null
     * @param requiresApproval whether decisions of this type require approval
     * @param project the project this type belongs to
     * @return the created decision type
     */
    @Transactional
    public CDecisionType createDecisionType(final String name, final String description, 
                                          final boolean requiresApproval, final CProject project) {
        LOGGER.info("createDecisionType called with name: {}, description: {}, requiresApproval: {} for project: {}", 
                   name, description, requiresApproval, project.getName());
        
        if (name == null || name.trim().isEmpty()) {
            LOGGER.error("createDecisionType called with null or empty name");
            throw new IllegalArgumentException("Decision type name cannot be null or empty");
        }
        
        final CDecisionType decisionType = new CDecisionType(name.trim(), description, project);
        decisionType.setRequiresApproval(requiresApproval);
        
        return repository.saveAndFlush(decisionType);
    }

    /**
     * Creates a new decision type with all properties.
     * @param name the decision type name - must not be null or empty
     * @param description the description - can be null
     * @param color the hex color code - can be null
     * @param requiresApproval whether decisions of this type require approval
     * @param sortOrder the display sort order
     * @param project the project this type belongs to
     * @return the created decision type
     */
    @Transactional
    public CDecisionType createDecisionType(final String name, final String description, 
                                          final String color, final boolean requiresApproval, 
                                          final Integer sortOrder, final CProject project) {
        LOGGER.info("createDecisionType called with name: {}, description: {}, color: {}, requiresApproval: {}, sortOrder: {} for project: {}", 
                   name, description, color, requiresApproval, sortOrder, project.getName());
        
        if (name == null || name.trim().isEmpty()) {
            LOGGER.error("createDecisionType called with null or empty name");
            throw new IllegalArgumentException("Decision type name cannot be null or empty");
        }
        
        final CDecisionType decisionType = new CDecisionType(name.trim(), description, color, 
                                                           requiresApproval, sortOrder, project);
        
        return repository.saveAndFlush(decisionType);
    }

    /**
     * Activates a decision type.
     * @param decisionType the decision type to activate - must not be null
     * @return the updated decision type
     */
    @Transactional
    public CDecisionType activateDecisionType(final CDecisionType decisionType) {
        LOGGER.info("activateDecisionType called with decisionType: {}", 
                   decisionType != null ? decisionType.getName() : "null");
        
        if (decisionType == null) {
            LOGGER.error("activateDecisionType called with null decisionType");
            throw new IllegalArgumentException("Decision type cannot be null");
        }
        
        decisionType.activate();
        return repository.saveAndFlush(decisionType);
    }

    /**
     * Deactivates a decision type.
     * @param decisionType the decision type to deactivate - must not be null
     * @return the updated decision type
     */
    @Transactional
    public CDecisionType deactivateDecisionType(final CDecisionType decisionType) {
        LOGGER.info("deactivateDecisionType called with decisionType: {}", 
                   decisionType != null ? decisionType.getName() : "null");
        
        if (decisionType == null) {
            LOGGER.error("deactivateDecisionType called with null decisionType");
            throw new IllegalArgumentException("Decision type cannot be null");
        }
        
        decisionType.deactivate();
        return repository.saveAndFlush(decisionType);
    }

    /**
     * Updates the sort order of a decision type.
     * @param decisionType the decision type to update - must not be null
     * @param sortOrder the new sort order
     * @return the updated decision type
     */
    @Transactional
    public CDecisionType updateSortOrder(final CDecisionType decisionType, final Integer sortOrder) {
        LOGGER.info("updateSortOrder called with decisionType: {}, sortOrder: {}", 
                   decisionType != null ? decisionType.getName() : "null", sortOrder);
        
        if (decisionType == null) {
            LOGGER.error("updateSortOrder called with null decisionType");
            throw new IllegalArgumentException("Decision type cannot be null");
        }
        
        decisionType.setSortOrder(sortOrder);
        return repository.saveAndFlush(decisionType);
    }

    /**
     * Checks if a decision type is available for use.
     * @param decisionType the decision type to check
     * @return true if the decision type is active and available
     */
    @Transactional(readOnly = true)
    public boolean isDecisionTypeAvailable(final CDecisionType decisionType) {
        LOGGER.info("isDecisionTypeAvailable called with decisionType: {}", 
                   decisionType != null ? decisionType.getName() : "null");
        
        if (decisionType == null) {
            LOGGER.warn("isDecisionTypeAvailable called with null decisionType");
            return false;
        }
        
        return decisionType.isAvailable();
    }
}