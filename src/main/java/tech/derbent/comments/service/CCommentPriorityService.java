package tech.derbent.comments.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.CTypeEntityService;
import tech.derbent.comments.domain.CCommentPriority;
import tech.derbent.session.service.ISessionService;

/** CCommentPriorityService - Service class for CCommentPriority entities. Layer: Service (MVC) Provides business logic operations for comment
 * priority management including: - CRUD operations - Priority level management - Default priority handling - Data provider functionality for UI
 * components */
@Service
@PreAuthorize ("isAuthenticated()")
public class CCommentPriorityService extends CTypeEntityService<CCommentPriority> {
	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CCommentPriorityService.class);

	CCommentPriorityService(final ICommentPriorityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing comment priority deletion. Always calls super.checkDeleteAllowed() first to ensure all parent-level checks
	 * (null validation, non-deletable flag) are performed.
	 * @param entity the comment priority entity to check
	 * @return null if priority can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CCommentPriority entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	protected Class<CCommentPriority> getEntityClass() { return CCommentPriority.class; }

	@Override
	public void initializeNewEntity(final CCommentPriority entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Comment Priority");
	}
}
