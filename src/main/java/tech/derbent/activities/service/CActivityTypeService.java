package tech.derbent.activities.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.activities.domain.CActivityType;

/** CActivityTypeService - Service layer for CActivityType entity. Layer: Service (MVC) Handles business logic for project-aware activity type
 * operations. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CActivityTypeService extends CEntityOfProjectService<CActivityType> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityTypeService.class);

	public CActivityTypeService(final CActivityTypeRepository repository, final Clock clock) {
		super(repository, clock);
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<CActivityType> getById(final Long id) {
		Check.notNull(id, "ID must not be null");
		final Optional<CActivityType> entity = ((CActivityTypeRepository) repository).findByIdWithRelationships(id);
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}

	@Override
	protected Class<CActivityType> getEntityClass() { return CActivityType.class; }
}
