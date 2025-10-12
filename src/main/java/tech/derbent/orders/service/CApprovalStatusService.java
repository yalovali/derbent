package tech.derbent.orders.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.orders.domain.CApprovalStatus;
import tech.derbent.session.service.ISessionService;

/** CApprovalStatusService - Service layer for CApprovalStatus entity. Layer: Service (MVC) Handles business logic for approval status operations
 * including creation, validation, and management of approval status entities. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CApprovalStatusService extends CEntityOfProjectService<CApprovalStatus> {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CApprovalStatusService.class);

	CApprovalStatusService(final IApprovalStatusRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CApprovalStatus> getEntityClass() { return CApprovalStatus.class; }

	@Override
	public String checkDependencies(final CApprovalStatus approvalStatus) {
		final String superCheck = super.checkDependencies(approvalStatus);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final CApprovalStatus entity) {
		super.initializeNewEntity(entity);
		tech.derbent.api.utils.Check.notNull(entity, "Approval status cannot be null");
		tech.derbent.api.utils.Check.notNull(sessionService, "Session service is required for approval status initialization");
		try {
			java.util.Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
			tech.derbent.api.utils.Check.isTrue(activeProject.isPresent(),
					"No active project in session - project context is required to create approval statuses");
			tech.derbent.projects.domain.CProject currentProject = activeProject.get();
			entity.setProject(currentProject);
			java.util.Optional<tech.derbent.users.domain.CUser> currentUser = sessionService.getActiveUser();
			if (currentUser.isPresent()) {
				entity.setCreatedBy(currentUser.get());
			}
			long statusCount = ((IApprovalStatusRepository) repository).countByProject(currentProject);
			String autoName = String.format("ApprovalStatus%02d", statusCount + 1);
			entity.setName(autoName);
			entity.setDescription("");
			entity.setColor(tech.derbent.orders.domain.CApprovalStatus.DEFAULT_COLOR);
			entity.setSortOrder(100);
			entity.setAttributeNonDeletable(false);
			LOGGER.debug("Initialized new approval status with auto-generated name: {}", autoName);
		} catch (final Exception e) {
			LOGGER.error("Error initializing new approval status", e);
			throw new IllegalStateException("Failed to initialize approval status: " + e.getMessage(), e);
		}
	}
}
