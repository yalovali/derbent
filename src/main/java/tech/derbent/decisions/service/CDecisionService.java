package tech.derbent.decisions.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.decisions.domain.CDecision;

/** CDecisionService - Service class for CDecision entities. Layer: Service (MVC) Provides business logic operations for decision management including
 * validation, creation, approval workflow management, and project-based queries. */
@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionService extends CEntityOfProjectService<CDecision> {

	public CDecisionService(final CDecisionRepository repository, final Clock clock) {
		super(repository, clock);
	}

	/** Gets a decision by ID with all relationships eagerly loaded. This prevents LazyInitializationException when accessing decision details.
	 * @param id the decision ID
	 * @return optional decision with loaded relationships */
	@Override
	@Transactional (readOnly = true)
	public java.util.Optional<CDecision> getById(final Long id) {
		Check.notNull(id, "ID must not be null");
		final java.util.Optional<CDecision> entity = ((CDecisionRepository) repository).findByIdWithAllRelationships(id);
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}

	@Override
	protected Class<CDecision> getEntityClass() { return CDecision.class; }

	/** Enhanced initialization of lazy-loaded fields specific to Decision entities. Uses improved null-safe patterns.
	 * @param entity the decision entity to initialize */
	@Override
	public void initializeLazyFields(final CDecision entity) {
		Check.notNull(entity, "Entity cannot be null");
		try {
			super.initializeLazyFields(entity); // Handles CEntityOfProject relationships automatically
			initializeLazyRelationship(entity.getDecisionType(), "decisionType");
			initializeLazyRelationship(entity.getDecisionStatus(), "decisionStatus");
			initializeLazyRelationship(entity.getAccountableUser(), "accountableUser");
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for Decision with ID: {}", entity.getId(), e);
		}
	}
}
