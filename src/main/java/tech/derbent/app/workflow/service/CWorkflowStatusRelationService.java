package tech.derbent.app.workflow.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfProject.service.CAbstractEntityRelationService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.utils.Check;
import tech.derbent.app.roles.domain.CUserProjectRole;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.base.session.service.ISessionService;

/** Service class for managing workflow-status relationships. Handles CRUD operations for CWorkflowStatusRelation entities. */
@Service
@Transactional (readOnly = true)
public class CWorkflowStatusRelationService extends CAbstractEntityRelationService<CWorkflowStatusRelation> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CWorkflowStatusRelationService.class);
	// private final IWorkflowStatusRelationRepository repository;

	@Autowired
	public CWorkflowStatusRelationService(final IWorkflowStatusRelationRepository repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
		// this.repository = repository;
	}

	/** Add status transition to workflow with specific roles */
	@Transactional
	public CWorkflowStatusRelation addStatusTransition(final CWorkflowEntity workflowEntity, final CProjectItemStatus fromStatus,
			final CProjectItemStatus toStatus, final List<CUserProjectRole> roles) {
		LOGGER.debug("Adding status transition to workflow {} from {} to {} for roles {}", workflowEntity, fromStatus, toStatus, roles);
		Check.notNull(workflowEntity, "Workflow must not be null");
		Check.notNull(fromStatus, "From status must not be null");
		Check.notNull(toStatus, "To status must not be null");
		if (workflowEntity.getId() == null || fromStatus.getId() == null || toStatus.getId() == null) {
			throw new IllegalArgumentException("Workflow and statuses must have valid IDs");
		}
		if (relationshipExists(workflowEntity.getId(), fromStatus.getId(), toStatus.getId())) {
			throw new IllegalArgumentException("This status transition is already defined for this workflow");
		}
		final CWorkflowStatusRelation relation = new CWorkflowStatusRelation();
		relation.setWorkflowEntity(workflowEntity);
		relation.setFromStatus(fromStatus);
		relation.setToStatus(toStatus);
		relation.setRoles(roles);
		validateRelationship(relation);
		return save(relation);
	}

	@Override
	public String checkDeleteAllowed(final CWorkflowStatusRelation entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public String checkSaveAllowed(final CWorkflowStatusRelation entity) {
		final String result = super.checkSaveAllowed(entity);
		if (result != null) {
			return result;
		}
		// Additional checks can be added here if needed
		if (entity.getFromStatus() == entity.getToStatus()) {
			final String string =
					"From status and To status cannot be the same. " + entity.getFromStatus().getName() + " -> " + entity.getToStatus().getName();
			return string;
		}
		return null;
	}

	@Override
	protected CWorkflowStatusRelation createRelationshipInstance(final Long workflowId, final Long statusId) {
		// Note: In a real implementation, you would fetch the actual entities from their
		// services This method should not be used directly - instead use the service
		// methods that accept entities
		throw new UnsupportedOperationException(
				"Use addStatusTransition(CWorkflowEntity, CProjectItemStatus, CProjectItemStatus, CUserProjectRole) method instead");
	}

	/** Remove status transition from workflow */
	@Transactional
	public void deleteByWorkflowAndStatuses(final CWorkflowEntity workflow, final CProjectItemStatus fromStatus, final CProjectItemStatus toStatus) {
		Check.notNull(workflow, "Workflow cannot be null");
		Check.notNull(fromStatus, "From status cannot be null");
		Check.notNull(toStatus, "To status cannot be null");
		Check.notNull(workflow.getId(), "Workflow must have a valid ID");
		Check.notNull(fromStatus.getId(), "From status must have a valid ID");
		Check.notNull(toStatus.getId(), "To status must have a valid ID");
		((IWorkflowStatusRelationRepository) repository).deleteByWorkflowIdAndFromStatusIdAndToStatusId(workflow.getId(), fromStatus.getId(),
				toStatus.getId());
		LOGGER.debug("Successfully removed status transition from workflow {} from status {} to status {}", workflow.getId(), fromStatus.getId(),
				toStatus.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public List<CWorkflowStatusRelation> findByChildEntityId(final Long statusId) {
		return ((IWorkflowStatusRelationRepository) repository).findByFromStatusId(statusId);
	}

	/** Find workflow status relations by from status */
	@Transactional (readOnly = true)
	public List<CWorkflowStatusRelation> findByFromStatus(final CProjectItemStatus fromStatus) {
		Check.notNull(fromStatus, "From status cannot be null");
		return findByChildEntityId(fromStatus.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public List<CWorkflowStatusRelation> findByParentEntityId(final Long workflowId) {
		return ((IWorkflowStatusRelationRepository) repository).findByWorkflowId(workflowId);
	}

	/** Find workflow status relations by role */
	@Transactional (readOnly = true)
	public List<CWorkflowStatusRelation> findByRole(final CUserProjectRole role) {
		Check.notNull(role, "Role cannot be null");
		return ((IWorkflowStatusRelationRepository) repository).findByRoleId(role.getId());
	}

	/** Find workflow status relations by to status */
	@Transactional (readOnly = true)
	public List<CWorkflowStatusRelation> findByToStatus(final CProjectItemStatus toStatus) {
		Check.notNull(toStatus, "To status cannot be null");
		return ((IWorkflowStatusRelationRepository) repository).findByToStatusId(toStatus.getId());
	}

	/** Find workflow status relations by workflow */
	@Transactional (readOnly = true)
	public List<CWorkflowStatusRelation> findByWorkflow(final CWorkflowEntity workflow) {
		Check.notNull(workflow, "Workflow cannot be null");
		return ((IWorkflowStatusRelationRepository) repository).findByWorkflowId(workflow.getId());
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<CWorkflowStatusRelation> findRelationship(final Long workflowId, final Long fromStatusId) {
		// This method is part of the abstract interface but doesn't fully capture our needs
		// Use findRelationshipByStatuses instead
		throw new UnsupportedOperationException("Use findRelationshipByStatuses(Long workflowId, Long fromStatusId, Long toStatusId) method instead");
	}

	/** Find a specific workflow status relation by workflow, from status, and to status */
	@Transactional (readOnly = true)
	public Optional<CWorkflowStatusRelation> findRelationshipByStatuses(final Long workflowId, final Long fromStatusId, final Long toStatusId) {
		return ((IWorkflowStatusRelationRepository) repository).findByWorkflowIdAndFromStatusIdAndToStatusId(workflowId, fromStatusId, toStatusId);
	}

	@Override
	public Class<CWorkflowStatusRelation> getEntityClass() { return CWorkflowStatusRelation.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceWorkflowStatusRelation.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Initialize lazy fields for a CWorkflowStatusRelation entity within a transaction context. This method should be called when you need to access
	 * lazy-loaded fields outside of the original Hibernate session. The repository queries already eagerly fetch common fields (workflow, statuses,
	 * role), but this method can be used for additional fields if needed.
	 * @param relation the relation entity to initialize
	 * @return the initialized relation entity */
	@Override
	@Transactional (readOnly = true)
	public CWorkflowStatusRelation initializeLazyFields(final CWorkflowStatusRelation relation) {
		Check.notNull(relation, "Relation cannot be null");
		if (relation.getId() == null) {
			LOGGER.warn("Cannot initialize lazy fields for unsaved entity");
			return relation;
		}
		// Fetch the entity from database to ensure all lazy fields are available
		final CWorkflowStatusRelation managed = repository.findById(relation.getId()).orElse(relation);
		// Access lazy fields to trigger loading within transaction
		managed.initializeAllFields();
		return managed;
	}

	@Override
	public void initializeNewEntity(final CWorkflowStatusRelation entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}

	@Override
	@Transactional (readOnly = true)
	public boolean relationshipExists(final Long workflowId, final Long fromStatusId) {
		// This method is part of the abstract interface but doesn't fully capture our needs
		// Use relationshipExists(Long workflowId, Long fromStatusId, Long toStatusId) method instead
		throw new UnsupportedOperationException("Use relationshipExists(Long workflowId, Long fromStatusId, Long toStatusId) method instead");
	}

	/** Check if a relationship exists between workflow and statuses */
	@Transactional (readOnly = true)
	public boolean relationshipExists(final Long workflowId, final Long fromStatusId, final Long toStatusId) {
		return ((IWorkflowStatusRelationRepository) repository).existsByWorkflowIdAndFromStatusIdAndToStatusId(workflowId, fromStatusId, toStatusId);
	}

	/** Update workflow status relation */
	@Transactional
	public CWorkflowStatusRelation updateStatusTransition(final CWorkflowEntity workflow, final CProjectItemStatus fromStatus,
			final CProjectItemStatus toStatus, final List<CUserProjectRole> newRoles) {
		LOGGER.debug("Updating workflow {} status transition from {} to {} roles to {}", workflow, fromStatus, toStatus, newRoles);
		final Optional<CWorkflowStatusRelation> relationOpt = findRelationshipByStatuses(workflow.getId(), fromStatus.getId(), toStatus.getId());
		if (relationOpt.isEmpty()) {
			throw new IllegalArgumentException("This status transition does not exist for this workflow");
		}
		final CWorkflowStatusRelation relation = relationOpt.get();
		relation.setRoles(newRoles);
		return updateRelationship(relation);
	}

        @Override
        protected void validateRelationship(final CWorkflowStatusRelation relationship) {
                super.validateRelationship(relationship);
                Check.notNull(relationship, "Relationship cannot be null");
                Check.notNull(relationship.getWorkflowEntity(), "Workflow cannot be null");
                Check.notNull(relationship.getWorkflowEntity().getProject(), "Workflow must belong to a project");
                Check.notNull(relationship.getFromStatus(), "From status cannot be null");
                Check.notNull(relationship.getToStatus(), "To status cannot be null");
                Check.isSameCompany(relationship.getWorkflowEntity().getProject(), relationship.getFromStatus());
                Check.isSameCompany(relationship.getWorkflowEntity().getProject(), relationship.getToStatus());
        }
}
