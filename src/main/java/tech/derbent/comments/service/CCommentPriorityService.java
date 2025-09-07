package tech.derbent.comments.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.comments.domain.CCommentPriority;
import tech.derbent.session.service.CSessionService;

/** CCommentPriorityService - Service class for CCommentPriority entities. Layer: Service (MVC) Provides business logic operations for comment
 * priority management including: - CRUD operations - Priority level management - Default priority handling - Data provider functionality for UI
 * components */
@Service
@PreAuthorize ("isAuthenticated()")
public class CCommentPriorityService extends CEntityOfProjectService<CCommentPriority> {

	CCommentPriorityService(final CCommentPriorityRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CCommentPriority> getEntityClass() { return CCommentPriority.class; }
}
