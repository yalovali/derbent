package tech.derbent.users.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUserType;

/** CUserTypeService - Service layer for CUserType entity. Layer: Service (MVC) Handles business logic for project-aware user type operations. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserTypeService extends CEntityOfProjectService<CUserType> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserTypeService.class);

	/** Constructor for CUserTypeService.
	 * @param repository the CUserTypeRepository to use for data access
	 * @param clock      the Clock instance for time-related operations */
	public CUserTypeService(final CUserTypeRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CUserType> getEntityClass() { return CUserType.class; }
}
