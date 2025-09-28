package tech.derbent.meetings.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.session.service.CSessionService;

/** CMeetingTypeService - Service layer for CMeetingType entity. Layer: Service (MVC) Handles business logic for project-aware meeting type
 * operations. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CMeetingTypeService extends CEntityOfProjectService<CMeetingType> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingTypeService.class);

	CMeetingTypeService(final IMeetingTypeRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CMeetingType> getEntityClass() { return CMeetingType.class; }
}
