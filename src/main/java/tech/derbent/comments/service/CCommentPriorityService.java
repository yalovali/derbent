package tech.derbent.comments.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.comments.domain.CCommentPriority;
import tech.derbent.session.service.ISessionService;

/** CCommentPriorityService - Service class for CCommentPriority entities. Layer: Service (MVC) Provides business logic operations for comment
 * priority management including: - CRUD operations - Priority level management - Default priority handling - Data provider functionality for UI
 * components */
@Service
@PreAuthorize ("isAuthenticated()")
public class CCommentPriorityService extends CEntityOfProjectService<CCommentPriority> {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CCommentPriorityService.class);

	CCommentPriorityService(final ICommentPriorityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CCommentPriority> getEntityClass() { return CCommentPriority.class; }

	@Override
	public String checkDependencies(final CCommentPriority commentPriority) {
		final String superCheck = super.checkDependencies(commentPriority);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final CCommentPriority entity) {
		super.initializeNewEntity(entity);
		tech.derbent.api.utils.Check.notNull(entity, "Comment priority cannot be null");
		tech.derbent.api.utils.Check.notNull(sessionService, "Session service is required for comment priority initialization");
		try {
			java.util.Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
			tech.derbent.api.utils.Check.isTrue(activeProject.isPresent(),
					"No active project in session - project context is required to create comment priorities");
			tech.derbent.projects.domain.CProject currentProject = activeProject.get();
			entity.setProject(currentProject);
			java.util.Optional<tech.derbent.users.domain.CUser> currentUser = sessionService.getActiveUser();
			if (currentUser.isPresent()) {
				entity.setCreatedBy(currentUser.get());
			}
			long priorityCount = ((ICommentPriorityRepository) repository).countByProject(currentProject);
			String autoName = String.format("CommentPriority%02d", priorityCount + 1);
			entity.setName(autoName);
			entity.setDescription("");
			entity.setColor(tech.derbent.comments.domain.CCommentPriority.DEFAULT_COLOR);
			entity.setSortOrder(100);
			entity.setAttributeNonDeletable(false);
			entity.setDefault(false);
			entity.setPriorityLevel(3);
			LOGGER.debug("Initialized new comment priority with auto-generated name: {}", autoName);
		} catch (final Exception e) {
			LOGGER.error("Error initializing new comment priority", e);
			throw new IllegalStateException("Failed to initialize comment priority: " + e.getMessage(), e);
		}
	}
}
