package tech.derbent.meetings.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.meetings.domain.CMeetingType;

/** CMeetingTypeService - Service layer for CMeetingType entity. Layer: Service (MVC) Handles business logic for project-aware meeting type
 * operations. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CMeetingTypeService extends CEntityOfProjectService<CMeetingType> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingTypeService.class);

	/** Constructor for CMeetingTypeService.
	 * @param repository the CMeetingTypeRepository to use for data access
	 * @param clock      the Clock instance for time-related operations */
	CMeetingTypeService(final CMeetingTypeRepository repository, final Clock clock) {
		super(repository, clock);
	}

	@Override
	protected Class<CMeetingType> getEntityClass() { return CMeetingType.class; }
}
