package tech.derbent.activities.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.session.service.CSessionService;

/** CActivityTypeService - Service layer for CActivityType entity. Layer: Service (MVC) Handles business logic for project-aware activity type
 * operations. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CActivityTypeService extends CEntityOfProjectService<CActivityType> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityTypeService.class);

	public CActivityTypeService(final IActivityTypeRepository repository, final Clock clock, final CSessionService sessionService,
			final IActivityRepository activityRepository) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CActivityType> getEntityClass() { return CActivityType.class; }
}
